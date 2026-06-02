package viewmodel;

import javafx.animation.AnimationTimer;
import model.*;
import view.Window;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

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

            }
        };
        animationTimer.start();
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