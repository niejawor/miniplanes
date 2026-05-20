package view;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import model.*;
import viewmodel.GamePresenter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Window extends Pane {
    private final GamePresenter presenter;
    private final Canvas canvas;
    private final GraphicsContext gc;
    private final Image backgroundTexture;
    private final Image airplaneTexture;

    private final List<Airport> currentRoute = new ArrayList<>();
    private double mouseX = 0;
    private double mouseY = 0;

    public Window(GamePresenter presenter) {
        this.presenter = presenter;
        this.canvas = new Canvas(1440, 810);
        this.gc = canvas.getGraphicsContext2D();

        getChildren().add(canvas);

        this.backgroundTexture = new Image(new File("src/assets/mapa.png").toURI().toString());
        this.airplaneTexture = new Image(new File("src/assets/airplane2.png").toURI().toString());

        setFocusTraversable(true);

        setOnMousePressed(e -> {
            mouseX = e.getX() / canvas.getWidth();
            mouseY = e.getY() / canvas.getHeight();

            if (e.getButton() == MouseButton.PRIMARY) {
                handleMouseClick((float) mouseX, (float) mouseY);
            } else if (e.getButton() == MouseButton.SECONDARY) {
                currentRoute.clear();
            }
        });

        setOnMouseMoved(e -> {
            mouseX = e.getX() / canvas.getWidth();
            mouseY = e.getY() / canvas.getHeight();
        });

        setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                handleEnterPress();
            } else if (e.getCode() == KeyCode.ESCAPE) {
                currentRoute.clear();
            }
        });

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                render();
            }
        };
        timer.start();
    }

    public void render() {
        double width = canvas.getWidth();
        double height = canvas.getHeight();

        gc.clearRect(0, 0, width, height);

        gc.drawImage(backgroundTexture, 0, 0, width, height);

        drawTempRoute();
        drawLines();
        drawAirports();
        drawAirplanes();
    }

    private void drawAirports() {
        float size = 0.008f;
        model.Color color = model.Color.Red;
        for (Airport airport : presenter.getAirports()) {
            float x = airport.getPosition().getX();
            float y = airport.getPosition().getY();
            drawSingleShape(airport.getShape(), x, y, size, color);
            drawAirportDetails(airport);
        }
    }

    private void drawAirplanes() {
        if (presenter.getAirplanes() == null) return;

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

    private void drawAirportDetails(Airport airport) {
        float x = airport.getPosition().getX();
        float y = airport.getPosition().getY();

        int passCount = 0;
        int maxPassengersToShow = 10;

        model.Color normalColor = model.Color.Blue;
        model.Color overloadColor = model.Color.Black;
        model.Color color = normalColor;

        for (Passenger p : airport.getPassengers()) {
            if (passCount >= maxPassengersToShow) break;
            if (passCount == airport.getAirportType().passengerCapacity)
                color = overloadColor;
            float px = x + ((passCount % 5) * 0.008f) + 0.004f;
            float py = y + ((passCount / 5) * 0.015f) + 0.028f;
            drawSingleShape(p.getDestination(), px, py, 0.003f, color);
            passCount++;
        }
    }

    private void drawTempRoute() {
        if (currentRoute.isEmpty()) return;

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
            Airport a = currentRoute.get(i);
            gc.lineTo(a.getPosition().getX() * w, a.getPosition().getY() * h);
        }
        gc.lineTo(mouseX * w, mouseY * h);
        gc.stroke();
        gc.setLineWidth(1.0);
        gc.setLineCap(StrokeLineCap.SQUARE);
        gc.setLineJoin(StrokeLineJoin.MITER);
    }

    private void drawLines() {
        if (presenter.getLines() == null || presenter.getLines().isEmpty()) return;

        gc.setLineWidth(5.0);
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setLineJoin(StrokeLineJoin.ROUND);

        double w = canvas.getWidth();
        double h = canvas.getHeight();

        for (Line line : presenter.getLines()) {
            if (line.size() < 2) continue;

            gc.setStroke(mapModelColor(line.color));
            gc.beginPath();

            Airport first = line.get(0);
            gc.moveTo(first.getPosition().getX() * w, first.getPosition().getY() * h);

            for (int i = 1; i < line.size(); i++) {
                Airport a = line.get(i);
                gc.lineTo(a.getPosition().getX() * w, a.getPosition().getY() * h);
            }
            gc.stroke();
        }
        gc.setLineWidth(1.0);
        gc.setLineCap(StrokeLineCap.SQUARE);
        gc.setLineJoin(StrokeLineJoin.MITER);
    }

    private void drawSingleShape(Shape shape, float x, float y, float size, model.Color color) {
        double w = canvas.getWidth();
        double h = canvas.getHeight();

        gc.save();

        gc.translate(x * w, y * h);
        gc.scale(w, w);

        float s;

        gc.setFill(mapModelColor(color));
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1.8 / w);

        switch (shape) {
            case Triangle:
                s = size * 1.1f;
                gc.fillPolygon(new double[]{0, -s, s}, new double[]{-s, s, s}, 3);
                gc.strokePolygon(new double[]{0, -s, s}, new double[]{-s, s, s}, 3);
                break;
            case Circle:
                s = size * 1.05f;
                gc.fillOval(-s, -s, s * 2, s * 2);
                gc.strokeOval(-s, -s, s * 2, s * 2);
                break;
            case Diamond:
                s = size * 1.2f;
                gc.fillPolygon(new double[]{0, s, 0, -s}, new double[]{-s, 0, s, 0}, 4);
                gc.strokePolygon(new double[]{0, s, 0, -s}, new double[]{-s, 0, s, 0}, 4);
                break;
            case Pentagon:
                s = size * 1.15f;
                drawRegularPolygon(5, s);
                break;
            case Hexagon:
                s = size * 1.15f;
                drawRegularPolygon(6, s);
                break;
            case Cross:
                s = size * 1.1f;
                double third = s / 3.0;
                double[] xCross = {-third, third, third, s, s, third, third, -third, -third, -s, -s, -third};
                double[] yCross = {-s, -s, -third, -third, third, third, s, s, third, third, -third, -third};
                gc.fillPolygon(xCross, yCross, 12);
                gc.strokePolygon(xCross, yCross, 12);
                break;
            case Star:
                s = size * 1.35f;
                drawStar(10, s, s / 2.4f);
                break;
            case Square:
            default:
                s = size * 0.95f;
                gc.fillRect(-s, -s, s * 2, s * 2);
                gc.strokeRect(-s, -s, s * 2, s * 2);
                break;
        }

        gc.restore();
    }

    private void drawRegularPolygon(int sides, float radius) {
        double[] xPoints = new double[sides];
        double[] yPoints = new double[sides];
        for (int i = 0; i < sides; i++) {
            double rad = Math.toRadians(i * (360.0 / sides) - 90);
            xPoints[i] = Math.cos(rad) * radius;
            yPoints[i] = Math.sin(rad) * radius;
        }
        gc.fillPolygon(xPoints, yPoints, sides);
        gc.strokePolygon(xPoints, yPoints, sides);
    }

    private void drawStar(int points, float outerRadius, float innerRadius) {
        double[] xPoints = new double[points];
        double[] yPoints = new double[points];
        for (int i = 0; i < points; i++) {
            double rad = Math.toRadians(i * (360.0 / points) - 90);
            double r = (i % 2 == 0) ? outerRadius : innerRadius;
            xPoints[i] = Math.cos(rad) * r;
            yPoints[i] = Math.sin(rad) * r;
        }
        gc.fillPolygon(xPoints, yPoints, points);
        gc.strokePolygon(xPoints, yPoints, points);
    }

    private void handleMouseClick(float x, float y) {
        Airport clicked = null;
        float minDistance = 0.02f;

        for (Airport airport : presenter.getAirports()) {
            float dx = airport.getPosition().getX() - x;
            float dy = airport.getPosition().getY() - y;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);

            if (dist < minDistance) {
                clicked = airport;
                break;
            }
        }

        if (clicked != null && (currentRoute.isEmpty() || currentRoute.get(currentRoute.size() - 1) != clicked))
            currentRoute.add(clicked);
    }

    private void handleEnterPress() {
        if (currentRoute.size() >= 2)
            presenter.createConfirmedRoute(new ArrayList<>(currentRoute));
        currentRoute.clear();
    }

    private Color mapModelColor(model.Color modelColor) {
        switch (modelColor) {
            case Red: return Color.color(0.8, 0.2, 0.2);
            case Green: return Color.color(0.2, 0.8, 0.2);
            case Blue: return Color.color(0.2, 0.2, 0.8);
            case Black:
            default: return Color.BLACK;
        }
    }
}