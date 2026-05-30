package mainapplication;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import view.Window;
import viewmodel.GamePresenter;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        GamePresenter presenter = new GamePresenter();

        Window window = new Window(presenter);
        Scene scene = new Scene(window, 1440, 810);

        presenter.setWindow(window);

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