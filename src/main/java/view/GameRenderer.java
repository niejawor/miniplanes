package view;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
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
        this.lineRenderer = new LineRenderer(presenter);
        this.backgroundTexture = new Image(new File("src/assets/mapa.png").toURI().toString());
        this.airplaneTexture = new Image(new File("src/assets/airplane2.png").toURI().toString());
        this.airportRenderer = new AirportRenderer(presenter, shapePainter, airplaneTexture);
        this.airplaneRenderer = new AirplaneRenderer(presenter, airplaneTexture, shapePainter);
    }

    public void render(double zoom, double panX, double panY) {
        double width = canvas.getWidth();
        double height = canvas.getHeight();

        gc.clearRect(0, 0, width, height);

        gc.save();
        gc.translate(panX, panY);
        gc.scale(zoom, zoom);

        gc.drawImage(backgroundTexture, 0, 0, width, height);

        routeBuilder.drawTempRoute(gc, canvas);
        lineRenderer.drawLines(gc, canvas);
        airportRenderer.drawAirports(gc, width, height, zoom);
        lineEditor.drawPreview(gc, canvas, presenter);
        airplaneRenderer.drawAirplanes(gc, width, height, zoom);

        gc.restore();

        uiRenderer.drawUI(gc, width, height, presenter.getMinutes(), presenter.getDay(), presenter.getResult());
    }
}

