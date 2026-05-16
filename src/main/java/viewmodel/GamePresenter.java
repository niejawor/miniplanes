package viewmodel;

import model.Airplane;
import model.Airport;
import model.BasicGameData;
import model.GameEngine;
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
}