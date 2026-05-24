package view;

import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import model.Airport;
import viewmodel.GamePresenter;

import java.util.ArrayList;
import java.util.List;

public class InputHandler {
    private final GamePresenter presenter;
    private final Canvas canvas;
    private final List<Airport> currentRoute = new ArrayList<>();
    private double mouseX = 0;
    private double mouseY = 0;

    public InputHandler(Window window, GamePresenter presenter, Canvas canvas) {
        this.presenter = presenter;
        this.canvas = canvas;

        window.setOnMousePressed(e -> {
            updateMousePosition(e.getX(), e.getY());
            if (e.getButton() == MouseButton.PRIMARY) {
                handleMouseClick((float) mouseX, (float) mouseY);
            } else if (e.getButton() == MouseButton.SECONDARY) {
                currentRoute.clear();
            }
        });

        window.setOnMouseMoved(e -> {
            mouseX = e.getX() / canvas.getWidth();
            mouseY = e.getY() / canvas.getHeight();
        });

        window.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                handleEnterPress();
            } else if (e.getCode() == KeyCode.ESCAPE) {
                currentRoute.clear();
            }
        });
    }

    private void updateMousePosition(double x, double y) {
        this.mouseX = x / canvas.getWidth();
        this.mouseY = y / canvas.getHeight();
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

    public List<Airport> getCurrentRoute() { return currentRoute; }
    public double getMouseX() { return mouseX; }
    public double getMouseY() { return mouseY; }
}
