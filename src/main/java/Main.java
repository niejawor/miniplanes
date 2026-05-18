import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import model.GameEngine;
import view.Window;
import viewmodel.GamePresenter;

public class Main {
    public static void main(String[] args) {
        GameEngine engine = new GameEngine();
        GamePresenter presenter = new GamePresenter(engine);

        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle(presenter.getTitle());
        config.setWindowedMode(2304, 1296);
        config.setBackBufferConfig(8, 8, 8, 8, 16, 0, 4);

        new Lwjgl3Application(new Window(presenter), config);
    }
}