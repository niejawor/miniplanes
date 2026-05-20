package mainapplication;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.GameEngine;
import view.Window;
import viewmodel.GamePresenter;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        GameEngine engine = new GameEngine();
        GamePresenter presenter = new GamePresenter(engine);

        Window window = new Window(presenter);
        Scene scene = new Scene(window, 1440, 810);

        window.requestFocus();

        primaryStage.setTitle(presenter.getTitle());
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}