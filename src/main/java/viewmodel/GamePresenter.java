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

                // to zle: patrz na paused
                time.addTime(now - lastTime);

                Updater.Result result = updater.update(now - lastTime);
                lastTime = now;

                score += result.getPassengersTransported();

                if(result.isItOver()){
                    gameOver = true;
                }

                if(lastPathsUpdate.getCurrentTime() == 0 || time.getInSeconds() - lastPathsUpdate.getInSeconds() > 10){
                    lastPathsUpdate.setCurrentTime(time.getCurrentTime());
                    triggerPathRefresh();
                }

            }
        };
        animationTimer.start();
    }

    public void triggerPathRefresh(){
        Map<Integer, Airport> deepClonedAirportsMap = gameData.getAirports().stream()
                .collect(Collectors.toMap(
                        Airport::getIndex,
                        airport -> new Airport(airport)
                ));

        List<Airport> airportsCopy = deepClonedAirportsMap.values().stream()
                .toList();

       List<ArrayList<Airport>> safeLinePaths = gameData.getLines().stream()
                .map(line -> line.getPath().stream()
                        .map(airport -> deepClonedAirportsMap.get(airport.getIndex()))
                        .collect(Collectors.toCollection(ArrayList::new))
                ).toList();

       HashMap<Pair<Integer, Airport>, Pair<Integer, Integer>> statsCopy = gameData.getStats().entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> new Pair<>(
                                entry.getKey().getKey(),
                                deepClonedAirportsMap.get(entry.getKey().getValue().getIndex())
                        ),
                        entry -> new Pair<>(
                                entry.getValue().getKey(),
                                entry.getValue().getValue()
                        ),
                        (staraWartosc, nowaWartosc) -> staraWartosc,
                        HashMap::new
                ));

       backgroundExecutor.submit(() -> {
            HashMap<Integer, List<Integer>> result = performPathFinder(statsCopy,safeLinePaths,airportsCopy);

            Platform.runLater(()->{
                gameData.setBestNextStop(result);
            });
       });
    }

    public HashMap<Integer, List<Integer>> performPathFinder(HashMap<Pair<Integer, Airport>, Pair<Integer, Integer>> stats, List<ArrayList<Airport>> lines, List<Airport> airports){
        HashMap<Integer, List<Pair<Airport, Float>>> graph = new HashMap<>();
        HashMap<Integer, List<Integer>> result = new HashMap<>();

        for(Airport airport : airports){
            graph.putIfAbsent(airport.getIndex(), new ArrayList<>());
        }

        HashMap<Pair<Integer, Airport>, Boolean> isInLines = new HashMap<>();

        for(ArrayList<Airport> linePath : lines){

            // co z pustymi ? 
            Airport previousAirport = linePath.get(0);
            for(Airport airport : linePath){
                if(previousAirport == airport){
                    continue;
                }

                isInLines.put(new Pair<>(previousAirport.getIndex(), airport), true);
                previousAirport = airport;
            }

            previousAirport = linePath.get(linePath.size()-1);
            for (Airport airport : linePath.reversed()) {
                if(previousAirport == airport){
                    continue;
                }

                isInLines.put(new Pair<>(previousAirport.getIndex(), airport), true);
                previousAirport = airport;
            }

        }


        for(Map.Entry<Pair<Integer, Airport>, Pair<Integer, Integer>> info : stats.entrySet()){
            if(!isInLines.containsKey(info.getKey())){
                continue;
            }

            graph.get(info.getKey().getKey()).add(new Pair<>(
                    info.getKey().getValue(), (float)info.getValue().getKey()/(float)info.getValue().getValue()
            ));
        }

        for(Airport airport : airports){
            result.put(airport.getIndex(), DijkstraShortestPath(airport, graph, airports));
        }

        return result;
    }

    List<Integer> DijkstraShortestPath(Airport start, HashMap<Integer, List<Pair<Airport, Float>>> graph, List<Airport> airports){
        List<Integer> result = new ArrayList<>();

        HashMap<Airport, Float> dist = new HashMap<>();
        HashMap<Airport, Airport> prev = new HashMap<>();

        for(Airport airport : airports){
            dist.put(airport, 1000_000_000_000f);
            prev.put(airport, airport);
        }

        dist.put(start, 0.0f);

        // pary lotnisko , odleglosc
        PriorityQueue<Pair<Airport,Float>> priorityQueue = new PriorityQueue<>(Comparator.comparingDouble(Pair::getValue));
        priorityQueue.add(new Pair<>(start,0.0f));


        while(!priorityQueue.isEmpty()){
            Pair<Airport,Float> pair = priorityQueue.poll();

            Float sugestedDistance = pair.getValue();
            Float distance = dist.get(pair.getKey());
            if(sugestedDistance > distance){
                continue;
            }

            for(Pair<Airport, Float> edge : graph.get(pair.getKey().getIndex())){
                Float newDistance = dist.get(pair.getKey()) + edge.getValue();
                if(newDistance < dist.get(edge.getKey())){
                    dist.put(edge.getKey(), newDistance);
                    prev.put(edge.getKey(), pair.getKey());
                    priorityQueue.add(new Pair<>(edge.getKey(), newDistance));
                }
            }

        }

        Airport min = null;
        for(int i=0;i<8;i++){

            for(Map.Entry<Airport, Float> entry : dist.entrySet()){
                if(entry.getKey().getShape() != Shape.values()[i]){
                    continue;
                }

                if(min == null){
                    min = entry.getKey();
                }
                else if(dist.get(min) > entry.getValue()){
                    min = entry.getKey();
                }

            }

            Airport prevAirport = min;
            while(start.getIndex() != prev.get(prevAirport).getIndex()){
                if(prevAirport == prev.get(prevAirport)){
                    break;
                }
                prevAirport = prev.get(prevAirport);
            }
            if(min == null){min = start;}

            result.add(prevAirport.getIndex());
        }

        return  result;
    }

    public void processEvents(){
        if(eventsQueue.isEmpty()){return;}

        while(!eventsQueue.isEmpty()){
            Event event = eventsQueue.poll();
            if(event == null){
                return;
            }

            event.handleEvent();
        }
    }

    public String getTitle() { return basicGameData.getTitle(); }
    public List<Airport> getAirports() { return gameData.getAirports(); }
    public List<Airplane> getAirplanes() { return gameData.getAirplanes(); }
    public List<Line> getLines() { return gameData.getLines(); }

    public void createConfirmedRoute(List<Airport> routeAirports) {
        eventsQueue.add(new Event.AddLineEvent(gameData, routeAirports));
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

    public boolean addAirportToLineEdge(int lineId, int airportId, int edgeAirportId) {
        if (lineId < 0 || lineId >= gameData.getLines().size()) return false;

        eventsQueue.add(
          new Event.EditLineAddToEdgeEvent(gameData, lineId, airportId, edgeAirportId)
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
}