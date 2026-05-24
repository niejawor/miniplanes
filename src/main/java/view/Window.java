package view;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import model.*;
import viewmodel.GamePresenter;

import java.io.File;

public class Window extends Pane {
    private final Canvas canvas;
    private final GameRenderer gameRenderer;

    public Window(GamePresenter presenter) {
        this.canvas = new Canvas(1440, 810);
        getChildren().add(canvas);

        Image backgroundTexture = new Image(new File("src/assets/mapa.png").toURI().toString());
        Image airplaneTexture = new Image(new File("src/assets/airplane2.png").toURI().toString());
        InputHandler inputHandler = new InputHandler(this, presenter, canvas);
        this.gameRenderer = new GameRenderer(presenter, backgroundTexture, airplaneTexture, inputHandler);

        setFocusTraversable(true);

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                gameRenderer.render(canvas.getGraphicsContext2D(), canvas.getWidth(), canvas.getHeight());
            }
        };
        timer.start();
    }
}