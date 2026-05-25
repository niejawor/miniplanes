package view;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import model.Airport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RouteBuilder {
    private final List<Airport> currentRoute = new ArrayList<>();
    private float mouseX;
    private float mouseY;

    public void updateMousePosition(float x, float y) {
        this.mouseX = x;
        this.mouseY = y;
    }

    public void handleMouseClick(float x, float y, List<Airport> airports) {
        Airport clicked = null;
        float minDistance = 0.02f;

        for (Airport airport : airports) {
            float dx = airport.getPosition().getX() - x;
            float dy = airport.getPosition().getY() - y;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);

            if (dist < minDistance) {
                clicked = airport;
                break;
            }
        }

        if (clicked != null && (currentRoute.isEmpty() || currentRoute.get(currentRoute.size() - 1) != clicked)) {
            currentRoute.add(clicked);
        }
    }

    public boolean hasConfirmedRoute() {
        return currentRoute.size() >= 2;
    }

    public List<Airport> buildConfirmedRoute() {
        if (!hasConfirmedRoute()) {
            return Collections.emptyList();
        }

        List<Airport> confirmedRoute = new ArrayList<>(currentRoute);
        clear();
        return confirmedRoute;
    }

    public void clear() {
        currentRoute.clear();
    }

    public void drawTempRoute(GraphicsContext gc, Canvas canvas) {
        if (currentRoute.isEmpty()) {
            return;
        }

        gc.setLineWidth(4.0);
        gc.setStroke(Color.color(0.3, 0.3, 0.3, 0.8));
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setLineJoin(StrokeLineJoin.ROUND);

        double w = canvas.getWidth();
        double h = canvas.getHeight();

        gc.beginPath();
        Airport first = currentRoute.get(0);
        gc.moveTo(first.getPosition().getX() * w, first.getPosition().getY() * h);

        for (int i = 1; i < currentRoute.size(); i++) {
            Airport airport = currentRoute.get(i);
            gc.lineTo(airport.getPosition().getX() * w, airport.getPosition().getY() * h);
        }

        gc.lineTo(mouseX * w, mouseY * h);
        gc.stroke();
        gc.setLineWidth(1.0);
        gc.setLineCap(StrokeLineCap.SQUARE);
        gc.setLineJoin(StrokeLineJoin.MITER);
    }
}
