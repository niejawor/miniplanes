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
    public List<Airport> getAirports() { return engine.get_airports(); }
    public List<Airplane> getAirplanes() { return engine.get_airplanes(); }
    public List<Line> getLines() { return engine.get_lines(); }

    public void createConfirmedRoute(List<Airport> routeAirports) {
        engine.add_event(new Event.AddLineEvent(engine,routeAirports));
    }
}