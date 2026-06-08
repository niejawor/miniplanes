package mainapplication;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
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
        // set application icon (uses assets/MiniPlanes.png)
        try {
            Image icon = new Image("file:src/assets/MiniPlanes.png");
            primaryStage.getIcons().add(icon);
        } catch (Exception ignored) {}
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();


    }

    public static void main(String[] args) {
        launch(args);
    }
}