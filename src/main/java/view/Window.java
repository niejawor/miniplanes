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

    private double zoom = 1.0;
    private double panX = 0.0;
    private double panY = 0.0;
    private static final double MIN_ZOOM = 1.0;
    private static final double MAX_ZOOM = 20.0;

    private double lastMouseX;
    private double lastMouseY;

    public Window(GamePresenter presenter) {
        this.presenter = presenter;
        this.canvas = new Canvas();
        this.routeBuilder = new RouteBuilder();
        this.lineEditor = new LineEditor();
        this.renderer = new GameRenderer(presenter, canvas, routeBuilder, lineEditor);

        this.setPrefSize(1440, 810);
        canvas.widthProperty().bind(this.widthProperty());
        canvas.heightProperty().bind(this.heightProperty());

        this.widthProperty().addListener((obs, oldV, newV) -> clampPan());
        this.heightProperty().addListener((obs, oldV, newV) -> clampPan());

        getChildren().add(canvas);
        setFocusTraversable(true);

        setOnMousePressed(e -> {
            lastMouseX = e.getX();
            lastMouseY = e.getY();

            float x = getNormalizedX(e.getX());
            float y = getNormalizedY(e.getY());
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
                float x = getNormalizedX(e.getX());
                float y = getNormalizedY(e.getY());
                lineEditor.updateMousePosition(x, y, presenter);
                lineEditor.commit(presenter);
            }
        });

        setOnMouseMoved(e -> {
            float x = getNormalizedX(e.getX());
            float y = getNormalizedY(e.getY());
            routeBuilder.updateMousePosition(x, y);
            if (lineEditor.isEditing()) {
                lineEditor.updateMousePosition(x, y, presenter);
            }
        });

        setOnMouseDragged(e -> {
            double dx = e.getX() - lastMouseX;
            double dy = e.getY() - lastMouseY;

            if (e.isMiddleButtonDown() || e.isSecondaryButtonDown() || (e.isPrimaryButtonDown() && !lineEditor.isEditing())) {
                panX += dx;
                panY += dy;
                clampPan();
            }

            float x = getNormalizedX(e.getX());
            float y = getNormalizedY(e.getY());
            routeBuilder.updateMousePosition(x, y);

            if (lineEditor.isEditing()) {
                lineEditor.updateMousePosition(x, y, presenter);
            }

            lastMouseX = e.getX();
            lastMouseY = e.getY();
        });

        setOnScroll(e -> {
            double zoomFactor = e.getDeltaY() > 0 ? 1.1 : 0.9;
            applyZoom(zoomFactor, e.getX(), e.getY());
        });

        setOnKeyPressed(e -> {
            if (e.isControlDown() || e.isShortcutDown()) {
                if (e.getCode() == KeyCode.PLUS || e.getCode() == KeyCode.EQUALS) {
                    applyZoom(1.1, canvas.getWidth() / 2, canvas.getHeight() / 2);
                } else if (e.getCode() == KeyCode.MINUS) {
                    applyZoom(0.9, canvas.getWidth() / 2, canvas.getHeight() / 2);
                }
            } else if (e.getCode() == KeyCode.ENTER) {
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
                renderer.render(zoom, panX, panY);
            }
        };
        timer.start();
    }

    private void applyZoom(double factor, double x, double y) {
        double oldZoom = zoom;
        zoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, zoom * factor));

        double f = (zoom / oldZoom) - 1;
        panX -= (x - panX) * f;
        panY -= (y - panY) * f;

        clampPan();
    }

    private void clampPan() {
        double maxPanX = 0;
        double maxPanY = 0;
        double minPanX = canvas.getWidth() - (canvas.getWidth() * zoom);
        double minPanY = canvas.getHeight() - (canvas.getHeight() * zoom);

        if (panX > maxPanX) panX = maxPanX;
        if (panY > maxPanY) panY = maxPanY;
        if (panX < minPanX) panX = minPanX;
        if (panY < minPanY) panY = minPanY;
    }

    private float getNormalizedX(double x) {
        return (float) ((x - panX) / (zoom * canvas.getWidth()));
    }

    private float getNormalizedY(double y) {
        return (float) ((y - panY) / (zoom * canvas.getHeight()));
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