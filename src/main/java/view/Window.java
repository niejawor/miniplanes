package view;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import viewmodel.GamePresenter;

public class Window extends Pane {
    private final GamePresenter presenter;
    private final Canvas canvas;
    private final GameRenderer renderer;
    private final RouteBuilder routeBuilder;
    private final LineEditor lineEditor;

    public Window(GamePresenter presenter) {
        this.presenter = presenter;
        this.canvas = new Canvas(1440, 810);
        this.routeBuilder = new RouteBuilder();
        this.lineEditor = new LineEditor();
        this.renderer = new GameRenderer(presenter, canvas, routeBuilder, lineEditor);

        getChildren().add(canvas);
        setFocusTraversable(true);

        setOnMousePressed(e -> {
            float x = (float) (e.getX() / canvas.getWidth());
            float y = (float) (e.getY() / canvas.getHeight());
            routeBuilder.updateMousePosition(x, y);

            if (e.getButton() == MouseButton.PRIMARY) {
                if (!lineEditor.tryStartEditing(x, y, presenter)) {
                    handleMouseClick(x, y);
                }
            } else if (e.getButton() == MouseButton.SECONDARY) {
                if (lineEditor.isEditing()) {
                    lineEditor.cancel(presenter);
                } else {
                    routeBuilder.clear();
                }
            }
        });

        setOnMouseReleased(e -> {
            if (lineEditor.isEditing()) {
                float x = (float) (e.getX() / canvas.getWidth());
                float y = (float) (e.getY() / canvas.getHeight());
                lineEditor.updateMousePosition(x, y, presenter);
                lineEditor.commit(presenter);
            }
        });

        setOnMouseMoved(e -> {
            float x = (float) (e.getX() / canvas.getWidth());
            float y = (float) (e.getY() / canvas.getHeight());
            routeBuilder.updateMousePosition(x, y);
            if (lineEditor.isEditing()) {
                lineEditor.updateMousePosition(x, y, presenter);
            }
        });

        setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                handleEnterPress();
            } else if (e.getCode() == KeyCode.ESCAPE) {
                if (lineEditor.isEditing()) {
                    lineEditor.cancel(presenter);
                } else {
                    routeBuilder.clear();
                }
            }
        });

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                renderer.render();
            }
        };
        timer.start();
    }

    private void handleEnterPress() {
        if (routeBuilder.hasConfirmedRoute()) {
            presenter.createConfirmedRoute(routeBuilder.buildConfirmedRoute());
        }
    }

    private void handleMouseClick(float x, float y) {
        routeBuilder.handleMouseClick(x, y, presenter.getAirports());
    }
}
