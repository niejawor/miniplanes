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
    private GameData gameData;
    private Updater updater;

    private final Queue<Event> eventsQueue = new LinkedList<>();

    private boolean gameOver = false;
    private boolean paused = true;

    Window window;

    int score = 0;

    Time time = new Time(0);
    private final Time timeAfterLastRewardPopup = new Time(0);
    private boolean rewardPopupOpen = false;

    private final ExecutorService backgroundExecutor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable);
        thread.setDaemon(true);
        return thread;
    });


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

                maybeShowRewardPopup(now, now - lastTime);

                time.addTime(now - lastTime);

                Updater.Result result = updater.update(now - lastTime);
                lastTime = now;

                score += result.getPassengersTransported();

                if(result.isItOver()){
                    gameOver = true;
                    // Wywołanie okna Game Over w wątku JavaFX
                    Platform.runLater(() -> {
                        if (window != null) window.showGameOverOverlay();
                    });
                }

                if(lastPathsUpdate.getCurrentTime() == 0 || time.getInSeconds() - lastPathsUpdate.getInSeconds() > 1){
                    lastPathsUpdate.setCurrentTime(time.getCurrentTime());
                    if(!dijkstraRunning){
                        triggerPathRefresh();
                    }
                }

            }
        };
        animationTimer.start();
    }

    public void restartGame() {
        this.score = 0;
        this.time = new Time(0);
        this.gameOver = false;
        this.paused = false;
        this.rewardPopupOpen = false;
        this.gameData = new GameData();
        this.updater = new Updater(gameData);
        this.gameData.setUpdater(updater);
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void triggerPathRefresh(){
        dijkstraRunning = true;

        HashMap<Pair<Integer,Integer>,Pair<Long,Integer>> statsCopy = new HashMap<>(gameData.getStats());
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
            HashMap<Integer, List<List<Integer>>> result = updater.performPathFinder(statsCopy,copyLinePaths,airportsCopy,airportShapes);

            Platform.runLater(()->{
                gameData.setBestNextStop(result);
                dijkstraRunning = false;
            });
        });
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

    public GameStatsSnapshot getGameStatsSnapshot() {
        int waitingPassengers = 0;
        int overcrowdedAirports = 0;
        for (Airport airport : gameData.getAirports()) {
            waitingPassengers += airport.getPassengers().size();
            if (airport.isOverCrowded()) {
                overcrowdedAirports++;
            }
        }

        int onboardPassengers = 0;
        for (Airplane airplane : gameData.getAirplanes()) {
            onboardPassengers += airplane.getPassengersOnBoard().size();
        }

        List<LineStatsSnapshot> lineStats = new ArrayList<>();
        for (Line line : gameData.getLines()) {
            int airplanesOnLine = 0;
            for (Airplane airplane : gameData.getAirplanes()) {
                if (airplane.line == line) {
                    airplanesOnLine++;
                }
            }

            lineStats.add(new LineStatsSnapshot(
                    line.color,
                    line.size(),
                    airplanesOnLine,
                    line.getTransportedPassengers()
            ));
        }

        long totalTravelTime = 0;
        long travelSamples = 0;
        for (Pair<Long, Integer> stat : gameData.getStats().values()) {
            totalTravelTime += stat.getKey();
            travelSamples += stat.getValue();
        }
        double averageTravelTimeSeconds = travelSamples == 0
                ? 0.0
                : (totalTravelTime / (double) travelSamples) / 1_000_000_000.0;

        return new GameStatsSnapshot(
                score,
                getMinutes(),
                gameData.getAirports().size(),
                gameData.getLines().size(),
                gameData.getAirplanes().size(),
                gameData.getNumberOfAvailableAirplanes(),
                waitingPassengers,
                onboardPassengers,
                overcrowdedAirports,
                averageTravelTimeSeconds,
                List.copyOf(lineStats)
        );
    }

    public int getMaxOvercrowdedTime() {
        return gameData.getMaxOvercrowdedTime();
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

    private void maybeShowRewardPopup(long now, long deltaTime) {
        if (rewardPopupOpen || window == null) {
            return;
        }
        timeAfterLastRewardPopup.addTime(deltaTime);

        if (timeAfterLastRewardPopup.getInGameHours() < 60) {
            return;
        } // zmienic co np 4 dni // chyba pomija czas jak jest pazua w senise tez go liczy - do poprawy - naprawione 

        timeAfterLastRewardPopup.setCurrentTime(0);
        rewardPopupOpen = true;
        pauseGame();
        window.showRewardPopup(gameData.getNextLockedLineColor());
    }
}