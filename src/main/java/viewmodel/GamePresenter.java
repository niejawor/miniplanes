package viewmodel;

import model.*;

import java.util.List;
import view.Window;

public class GamePresenter {
    private final BasicGameData data;
    private final GameEngine engine;
    private final Window window;

    public GamePresenter(GameEngine engine) {
        this.data = new BasicGameData();
        this.engine = engine;
        this.window = new Window(this);
    }

    public void gameLoop() {
        Thread engineThread = new Thread(() -> engine.Simulate());
        engineThread.start();

        while (!window.shouldClose() && engine.is_running())
            window.render();
        window.terminate();
    }

    public String getTitle() { return data.getTitle(); }
    public List<Airport> getAirports() { return engine.get_airports(); }
    public List<Airplane> getAirplanes() { return engine.get_airplanes(); }
    public List<Line> getLines() { return engine.get_lines(); }

    public void createConfirmedRoute(List<Airport> routeAirports) {
        Line newLine = new Line(routeAirports.get(0), routeAirports.get(1));

        for (int i = 2; i < routeAirports.size(); i++)
            newLine.addAirportToEdge(routeAirports.get(i - 1), routeAirports.get(i));

        engine.get_lines().add(newLine);
    }
}