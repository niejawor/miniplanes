package view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import model.GameEngine;
import model.Weekdays;
import viewmodel.GamePresenter;

public class GameRenderer {
    private final GamePresenter presenter;
    private final Image backgroundTexture;
    private final InputHandler inputHandler;

    private final AirportRenderer airportRenderer = new AirportRenderer();
    private final AirplaneRenderer airplaneRenderer;
    private final LineRenderer lineRenderer = new LineRenderer();
    private final UIRenderer uiRenderer = new UIRenderer();

    public GameRenderer(GamePresenter presenter, Image background, Image airplane, InputHandler inputHandler) {
        this.presenter = presenter;
        this.backgroundTexture = background;
        this.airplaneRenderer = new AirplaneRenderer(airplane);
        this.inputHandler = inputHandler;
    }

    public void render(GraphicsContext gc, double w, double h) {
        gc.clearRect(0, 0, w, h);

        gc.drawImage(backgroundTexture, 0, 0, w, h);

        lineRenderer.drawTempRoute(gc, inputHandler.getCurrentRoute(), w, h, inputHandler.getMouseX(), inputHandler.getMouseY());
        lineRenderer.drawLines(gc, presenter.getLines(), w, h);
        airportRenderer.drawAirports(gc, presenter.getAirports(), w, h);
        airplaneRenderer.drawAirplanes(gc, presenter.getAirplanes(), w, h);

        int minutes = presenter.getMinutes();
        Weekdays day = presenter.getDay();
        int score = presenter.getResult();

        uiRenderer.drawUI(gc, w, h, minutes, day, score);
    }
}
