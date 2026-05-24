package view;

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
        drawAirplanes();
    }

    private void drawAirplanes() {
        if (presenter.getAirplanes() == null) {
            return;
        }

        double width = canvas.getWidth();
        double height = canvas.getHeight();
        double screenAspect = width / height;

        double texWidth = airplaneTexture.getWidth();
        double texHeight = airplaneTexture.getHeight();
        double textureAspect = texWidth / texHeight;

        for (Airplane plane : presenter.getAirplanes()) {
            float x = plane.getPosition().getX();
            float y = plane.getPosition().getY();
            float scale = plane.getType() == AirplaneType.SmallAirplane ? 0.02f : 0.015f;

            float angle = 0f;
            if (plane.isCurrentlyFlying()) {
                float destX = plane.getDestination().getPosition().getX();
                float destY = plane.getDestination().getPosition().getY();
                float dx = destX - x;
                float dy = destY - y;
                angle = (float) Math.toDegrees(Math.atan2(dy * (height / width), dx));
            }

            double planeHeight = (scale * 2) / screenAspect;
            double planeWidth = planeHeight * textureAspect;

            gc.save();
            gc.translate(x * width, y * height);
            gc.rotate(angle);

            double drawWidth = planeWidth * width;
            double drawHeight = planeHeight * width;
            gc.drawImage(airplaneTexture, -drawWidth / 2, -drawHeight / 2, drawWidth, drawHeight);
            gc.restore();
        }
    }

}

