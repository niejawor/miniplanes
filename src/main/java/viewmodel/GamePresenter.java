package viewmodel;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.util.Pair;
import model.*;
import view.Window;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class GamePresenter {
    private final BasicGameData basicGameData;
    private final GameData gameData;
    private final Updater updater;

    private final Queue<Event> eventsQueue = new LinkedList<>();

    private boolean gameOver = false;
    private boolean paused = false;

    Window window;

    int score = 0;

    Time time = new Time(0);
    private static final long REWARD_POPUP_INTERVAL_NANOS = 30_000_000_000L;
    private long lastRewardPopupTime = 0;
    private boolean rewardPopupOpen = false;

    private final ExecutorService backgroundExecutor = Executors.newSingleThreadExecutor();


    public GamePresenter() {
        this.basicGameData = new BasicGameData();

        gameData = new GameData();
        updater = new Updater(gameData);
        gameData.setUpdater(updater);

        setupGameLoop();
    }

    public void setWindow(Window window) {
        this.window = window;
    }

    boolean dijkstraRunning = false;

    public void setupGameLoop(){
        AnimationTimer animationTimer = new AnimationTimer() {
            long lastTime = System.nanoTime();
            Time lastPathsUpdate = new Time(0);

            @Override
            public void handle(long now) {
                window.render();

                processEvents();

                if(gameOver || paused){
                    lastTime = now;
                    return;
                }

                maybeShowRewardPopup(now);

                time.addTime(now - lastTime);

                Updater.Result result = updater.update(now - lastTime);
                lastTime = now;

                score += result.getPassengersTransported();

                if(result.isItOver()){
                    gameOver = true;
                }

                if(lastPathsUpdate.getCurrentTime() == 0 || time.getInSeconds() - lastPathsUpdate.getInSeconds() > 5){
                    lastPathsUpdate.setCurrentTime(time.getCurrentTime());
                    if(!dijkstraRunning){
                        triggerPathRefresh();
                    }
                }

            }
        };
        animationTimer.start();
    }

    public void triggerPathRefresh(){
        dijkstraRunning = true;

        HashMap<Pair<Integer,Integer>,Pair<Integer,Integer>> statsCopy = new HashMap<>(gameData.getStats());
        List<ArrayList<Integer>> copyLinePaths =  new ArrayList<>();

        for(Line line: gameData.getLines()){
            if(line.getPath().size()<2){
                continue;
            }

            copyLinePaths.add(new ArrayList<>());
            for(Airport airport: line.getPath()){
                copyLinePaths.getLast().add(airport.getIndex());
            }

        }

        List<Integer> airportsCopy = new ArrayList<>();
        for (Airport airport: gameData.getAirports()){
            airportsCopy.add(airport.getIndex());
        }

        HashMap<Integer,Shape> airportShapes = new HashMap<>();
        for (Airport airport: gameData.getAirports()){
            airportShapes.put(airport.getIndex(), airport.getShape());
        }


        backgroundExecutor.submit(() -> {
            HashMap<Integer, List<List<Integer>>> result = performPathFinder(statsCopy,copyLinePaths,airportsCopy,airportShapes);

            Platform.runLater(()->{
                gameData.setBestNextStop(result);
                dijkstraRunning = false;
            });
        });
    }

    // to trzeba przeniesc gdzies do backendu - np do gameData?
    public HashMap<Integer, List<List<Integer>>> performPathFinder(HashMap<Pair<Integer, Integer>, Pair<Integer, Integer>> stats, List<ArrayList<Integer>> lines, List<Integer> airports, HashMap<Integer, Shape> airportShapes){
        HashMap<Integer, List<Pair<Integer, Float>>> graph = new HashMap<>(); // airport -> index
        HashMap<Integer, List<List<Integer>>> result = new HashMap<>();

        for(Integer airport : airports){ // indeksy lotnisk
            graph.putIfAbsent(airport, new ArrayList<>());
        }

        HashMap<Pair<Integer, Integer>, Boolean> isInLines = new HashMap<>();

        for(ArrayList<Integer> linePath : lines){

            // co z pustymi ?
            Integer previousAirport = linePath.getFirst(); // puste!, potrzebujemy tylko indeksy kolejnych krawedzi
            for(Integer airport : linePath){
                if(Objects.equals(previousAirport, airport)){
                    continue;
                }

                isInLines.put(new Pair<>(previousAirport, airport), true);
                previousAirport = airport;
            }

            previousAirport = linePath.getLast(); // znowu pusty?
            for (Integer airport : linePath.reversed()) {
                if(Objects.equals(previousAirport, airport)){
                    continue;
                }

                isInLines.put(new Pair<>(previousAirport, airport), true);
                previousAirport = airport;
            }

        }


        for(Map.Entry<Pair<Integer, Integer>, Pair<Integer, Integer>> info : stats.entrySet()){
            if(!isInLines.containsKey(info.getKey())){
                continue;
            }

            graph.get(info.getKey().getKey()).add(new Pair<>(
                    info.getKey().getValue(), (float)info.getValue().getKey()/(float)info.getValue().getValue()
            ));
        }

        for(Integer airport : airports){
            result.put(airport, DijkstraShortestPath(airport, graph, airports, airportShapes));
        }

        return result;
    }

    List<List<Integer>> DijkstraShortestPath(Integer start, HashMap<Integer, List<Pair<Integer, Float>>> graph, List<Integer> airports, HashMap<Integer, Shape> airportShapes){
        List<List<Integer>> result = new ArrayList<>();

        HashMap<Integer, Float> dist = new HashMap<>();
        HashMap<Integer, Integer> prev = new HashMap<>();

        for(Integer airport : airports){
            dist.put(airport, 1000_000_000_000f);
            prev.put(airport, airport);
        }

        dist.put(start, 0.0f);

        // pary lotnisko , odleglosc
        PriorityQueue<Pair<Integer,Float>> priorityQueue = new PriorityQueue<>(Comparator.comparingDouble(Pair::getValue));
        priorityQueue.add(new Pair<>(start,0.0f));


        while(!priorityQueue.isEmpty()){
            Pair<Integer,Float> pair = priorityQueue.poll();

            Float sugestedDistance = pair.getValue();
            Float distance = dist.get(pair.getKey());
            if(sugestedDistance > distance){
                continue;
            }

            for(Pair<Integer, Float> edge : graph.get(pair.getKey())){
                Float newDistance = dist.get(pair.getKey()) + edge.getValue();
                if(newDistance < dist.get(edge.getKey())){
                    dist.put(edge.getKey(), newDistance);
                    prev.put(edge.getKey(), pair.getKey());
                    priorityQueue.add(new Pair<>(edge.getKey(), newDistance));
                }
            }

        }

        Integer min;
        for(int i=0;i<Shape.values().length;i++){
            min = null;

            for(Map.Entry<Integer, Float> entry : dist.entrySet()){
                if(airportShapes.get(entry.getKey()) != Shape.values()[i]){
                    continue;
                }

                if(min == null){
                    min = entry.getKey();
                }
                else if(dist.get(min) > entry.getValue()){
                    min = entry.getKey();
                }

            }

            List<Integer> temp = new ArrayList<>();
            if(min == null) {
                temp.add(start);
                result.add(temp);
                continue;
            }

            Integer prevAirport = min;
            while(!Objects.equals(prev.get(prevAirport), start)){
                if(Objects.equals(prevAirport, prev.get(prevAirport))){
                    break;
                }
                prevAirport = prev.get(prevAirport);
            }
            temp.add(prevAirport);


            for(Map.Entry<Integer, Float> entry : dist.entrySet()){
                if(airportShapes.get(entry.getKey()) != Shape.values()[i]){
                    continue;
                }
                if(dist.get(min)*2.5 >= entry.getValue()){
                    temp.add(entry.getKey());
                }
            }

            result.add(temp);
        }

        return result;
    }

    public void processEvents(){
        if(eventsQueue.isEmpty()){return;}

        boolean processedAnyEvent = false;
        while(!eventsQueue.isEmpty()){
            Event event = eventsQueue.poll();
            if(event == null){
                return;
            }

            event.handleEvent();
            processedAnyEvent = true;
        }

        if (processedAnyEvent && window != null) {
            window.refreshNavbar();
        }
    }

    public String getTitle() { return basicGameData.getTitle(); }
    public List<Airport> getAirports() { return gameData.getAirports(); }
    public List<Airplane> getAirplanes() { return gameData.getAirplanes(); }
    public List<Line> getLines() { return gameData.getLines(); }

    public List<Color> getPalette() { return gameData.getPalette(); }
    public int getAvailableAirplanes() { return gameData.getNumberOfAvailableAirplanes(); }

    public void createConfirmedRoute(List<Airport> routeAirports, Color color) {
        eventsQueue.add(new Event.AddLineEvent(gameData, routeAirports, color));
    }

    public void addAirplaneToLine(int lineId, int airportId) {
        if (lineId < 0 || lineId >= gameData.getLines().size()) return;
        if (airportId < 0 || airportId >= gameData.getAirports().size()) return;
        eventsQueue.add(new Event.AddAirplaneToLineEvent(gameData, lineId, airportId));
    }

    public boolean removeAirportFromLine(int lineId, int airportId) {
        if (lineId < 0 || lineId >= gameData.getLines().size()) return false;
        if (airportId < 0 || airportId >= gameData.getAirports().size()) return false;
        eventsQueue.add(new Event.EditLineRemoveEvent(airportId, gameData, lineId));
        return true;
    }


    
    public void pauseGame() { //pauseSimulation
        paused = true;
    }

    public void resumeGame() {
        paused = false;
    }

    public boolean insertAirportIntoLine(int lineId, int airportToAddId, int beforeAirportId, int afterAirportId) {
        if (lineId < 0 || lineId >= gameData.getLines().size()) return false;
        if (airportToAddId < 0 || airportToAddId >= gameData.getAirports().size()) return false;
        eventsQueue.add(
                new Event.EditLineAddEvent(beforeAirportId, airportToAddId, afterAirportId, gameData, lineId)
        );

        return true;
    }

    public boolean addAirportToLineEdge(int lineId, int newAirportId, int edgeAirportId) {
        if (lineId < 0 || lineId >= gameData.getLines().size()) return false;
        if (newAirportId < 0 || newAirportId >= gameData.getAirports().size()) return false;

        eventsQueue.add(
          new Event.EditLineAddToEdgeEvent(gameData, lineId, edgeAirportId, newAirportId)
        );

        return true;
    }

    public int getMinutes() {
        return (int)time.getInGameMinutes();
    }


    public Weekdays getDay() {
        long day = time.getInGameDays();
        return Weekdays.values()[(int)day%7];
    }

    public int getResult() {
        return score;
    }

    public void chooseLineReward() {
        gameData.unlockNextLineColor();
        rewardPopupOpen = false;
        resumeGame();
        if (window != null) window.refreshNavbar();
    }

    public void chooseAirplaneReward() {
        gameData.addAvailableAirplane();
        rewardPopupOpen = false;
        resumeGame();
        if (window != null) window.refreshNavbar();
    }

    public void skipReward() {
        rewardPopupOpen = false;
        resumeGame();
    }

    private void maybeShowRewardPopup(long now) {
        if (rewardPopupOpen || window == null) {
            return;
        }
        if (lastRewardPopupTime == 0) {
            lastRewardPopupTime = now;
            return;
        }
        if (now - lastRewardPopupTime < REWARD_POPUP_INTERVAL_NANOS) {
            return;
        }

        lastRewardPopupTime = now;
        rewardPopupOpen = true;
        pauseGame();
        window.showRewardPopup(gameData.getNextLockedLineColor());
    }
}