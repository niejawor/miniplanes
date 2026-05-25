package viewmodel;

import model.*;
import java.util.List;

public class GamePresenter {
    private final BasicGameData data;
    private final GameEngine engine;

    public GamePresenter(GameEngine engine) {
        this.data = new BasicGameData();
        this.engine = engine;

        Thread engineThread = new Thread(() -> this.engine.Simulate());
        engineThread.setDaemon(true);
        engineThread.start();
    }

    public String getTitle() { return data.getTitle(); }
    public List<Airport> getAirports() { return engine.getAirports(); }
    public List<Airplane> getAirplanes() { return engine.getAirplanes(); }
    public List<Line> getLines() { return engine.getLines(); }

    public void createConfirmedRoute(List<Airport> routeAirports) {
        engine.addEvent(new Event.AddLineEvent(engine, routeAirports));
    }

    public int getMinutes() {
        return engine.getMinutes();
    }

    public Weekdays getDay() {
        return engine.getDay();
    }

    public int getResult() {
        return engine.getResult();
    }
    
    public void pauseSimulation() {
        engine.pause();
    }

    public void resumeSimulation() {
        engine.resume();
    }

    public boolean insertAirportIntoLine(int lineId, int airportToAddId, int beforeAirportId, int afterAirportId) {
        if (lineId < 0 || lineId >= engine.getLines().size()) return false;
        if (airportToAddId < 0 || airportToAddId >= engine.getAirports().size()) return false;
        engine.addEvent(new Event.EditLineAddEvent(beforeAirportId, airportToAddId, afterAirportId, engine, lineId));
        return true;
    }

    public boolean addAirportToLineEdge(int lineId, int airportId, int edgeAirportId) {
        if (lineId < 0 || lineId >= engine.getLines().size()) return false;
        engine.addEvent(new Event.EditLineAddToEdgeEvent(engine, lineId, edgeAirportId, airportId));
        return true;
    }

    public int getMinutes() {
        return engine.getMinutes();
    }

    public Weekdays getDay() {
        return engine.getDay();
    }

    public int getResult() {
        return engine.getResult();
    }
}