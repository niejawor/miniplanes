package view;
import model.GameEngine;
import model.Weekdays;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import model.Airplane;
import model.AirplaneType;
import viewmodel.GamePresenter;


import java.io.File;

public class GameRenderer {
    private final GamePresenter presenter;
    private final Canvas canvas;
    private final GraphicsContext gc;
    private final Image backgroundTexture;
    private final Image airplaneTexture;
    private final ShapePainter shapePainter;
    private final RouteBuilder routeBuilder;
    private final LineEditor lineEditor;
    private final AirportRenderer airportRenderer;
    private final LineRenderer lineRenderer;
    private final UIRenderer uiRenderer = new UIRenderer();
    private final AirplaneRenderer airplaneRenderer;

    public GameRenderer(GamePresenter presenter, Canvas canvas, RouteBuilder routeBuilder, LineEditor lineEditor) {
        this.presenter = presenter;
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
        this.shapePainter = new ShapePainter(gc, canvas);
        this.routeBuilder = routeBuilder;
        this.lineEditor = lineEditor;
        this.airportRenderer = new AirportRenderer(presenter, shapePainter);
        this.lineRenderer = new LineRenderer(presenter);
        this.backgroundTexture = new Image(new File("src/assets/mapa.png").toURI().toString());
        this.airplaneTexture = new Image(new File("src/assets/airplane2.png").toURI().toString());
        this.airplaneRenderer = new AirplaneRenderer(presenter, airplaneTexture);
        // UI is drawn during each render call where canvas size is known
    }

    public void render() {
        double width = canvas.getWidth();
        double height = canvas.getHeight();

        gc.clearRect(0, 0, width, height);
        gc.drawImage(backgroundTexture, 0, 0, width, height);

        routeBuilder.drawTempRoute(gc, canvas);
        lineRenderer.drawLines(gc, canvas);
        airportRenderer.drawAirports();
        lineEditor.drawPreview(gc, canvas, presenter);
        airplaneRenderer.drawAirplanes(gc, width, height);
        // draw UI overlay (clock, day, score)
        uiRenderer.drawUI(gc, width, height, presenter.getMinutes(), presenter.getDay(), presenter.getResult());
    }
}

