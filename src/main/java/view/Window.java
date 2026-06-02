package view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import viewmodel.GamePresenter;

public class Window extends Pane {
    private final GamePresenter presenter;
    private final Canvas canvas;
    private final GameRenderer renderer;
    private final RouteBuilder routeBuilder;
    private final LineEditor lineEditor;
    private final Navbar navbar;
    private StackPane rewardOverlay;

    private model.Color selectedColor;
    private boolean addAirplaneMode = false;
    private int selectedLineForAirplane = -1;

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

        this.selectedColor = presenter.getPalette().get(0);
        this.navbar = new Navbar(presenter.getPalette(), selectedColor, presenter.getAvailableAirplanes(), new Navbar.Listener() {
            @Override
            public void onColorSelected(model.Color color) {
                selectedColor = color;
            }

            @Override
            public void onAddAirplaneToggled(boolean active) {
                addAirplaneMode = active;
                selectedLineForAirplane = -1;
                navbar.setAirplaneLineSelected(false);
                if (active) {
                    routeBuilder.clear();
                }
            }
        });
        navbar.prefWidthProperty().bind(this.widthProperty().multiply(0.5));

        this.widthProperty().addListener((obs, oldV, newV) -> { clampPan(); positionNavbar(); });
        this.heightProperty().addListener((obs, oldV, newV) -> { clampPan(); positionNavbar(); });
        navbar.heightProperty().addListener((obs, oldV, newV) -> positionNavbar());
        navbar.widthProperty().addListener((obs, oldV, newV) -> positionNavbar());

        getChildren().add(canvas);
        getChildren().add(navbar);
        setFocusTraversable(true);

        setOnMousePressed(e -> {
            lastMouseX = e.getX();
            lastMouseY = e.getY();

            float x = getNormalizedX(e.getX());
            float y = getNormalizedY(e.getY());
            routeBuilder.updateMousePosition(x, y);

            if (e.getButton() == MouseButton.PRIMARY) {
                if (addAirplaneMode) {
                    handleAddAirplaneClick(x, y);
                } else if (!lineEditor.tryStartEditing(x, y, presenter, selectedColor)) {
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
                if (addAirplaneMode) {
                    addAirplaneMode = false;
                    selectedLineForAirplane = -1;
                    navbar.setAddAirplaneActive(false);
                    navbar.setAirplaneLineSelected(false);
                } else if (lineEditor.isEditing()) {
                    lineEditor.cancel(presenter);
                } else {
                    routeBuilder.clear();
                }
            }
        });

//        AnimationTimer timer = new AnimationTimer() {
//            @Override
//            public void handle(long now) {
//                renderer.render(zoom, panX, panY);
//            }
//        };
//        timer.start();
    }

    public void render() {
        renderer.render(zoom, panX, panY);
    }

    public void refreshNavbar() {
        navbar.refresh(presenter.getPalette(), presenter.getAvailableAirplanes());
    }

    public void showRewardPopup(model.Color nextColor) {
        if (rewardOverlay != null) return;

        rewardOverlay = new StackPane();
        rewardOverlay.prefWidthProperty().bind(widthProperty());
        rewardOverlay.prefHeightProperty().bind(heightProperty());
        rewardOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.35);");
        rewardOverlay.setPickOnBounds(true);

        VBox panel = new VBox(16);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(26, 34, 26, 34));
        panel.setMaxWidth(460);
        panel.setStyle("-fx-background-color: rgba(245,245,245,0.96); -fx-background-radius: 18;"
                + "-fx-border-color: rgba(0,0,0,0.25); -fx-border-radius: 18;");

        Label title = new Label("Nowa nagroda");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #222;");

        Label text = new Label(nextColor == null
                ? "Wszystkie kolory sa juz odblokowane. Mozesz dobrac samolot."
                : "Wybierz: nowa linia z kolorem " + nextColor + " albo dodatkowy samolot.");
        text.setWrapText(true);
        text.setStyle("-fx-font-size: 15px; -fx-text-fill: #333;");

        HBox buttons = new HBox(12);
        buttons.setAlignment(Pos.CENTER);

        if (nextColor != null) {
            Button lineButton = new Button("Linia: " + nextColor);
            lineButton.setFocusTraversable(false);
            lineButton.setOnAction(e -> {
                closeRewardPopup();
                presenter.chooseLineReward();
            });
            buttons.getChildren().add(lineButton);
        }

        Button airplaneButton = new Button("Samolot +1");
        airplaneButton.setFocusTraversable(false);
        airplaneButton.setOnAction(e -> {
            closeRewardPopup();
            presenter.chooseAirplaneReward();
        });

        Button skipButton = new Button("Pomin");
        skipButton.setFocusTraversable(false);
        skipButton.setOnAction(e -> {
            closeRewardPopup();
            presenter.skipReward();
        });

        buttons.getChildren().addAll(airplaneButton, skipButton);
        panel.getChildren().addAll(title, text, buttons);
        rewardOverlay.getChildren().add(panel);
        getChildren().add(rewardOverlay);
    }

    private void closeRewardPopup() {
        if (rewardOverlay == null) return;
        getChildren().remove(rewardOverlay);
        rewardOverlay = null;
        requestFocus();
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
            presenter.createConfirmedRoute(routeBuilder.buildConfirmedRoute(), selectedColor);
        }
    }

    private void handleMouseClick(float x, float y) {
        routeBuilder.handleMouseClick(x, y, presenter.getAirports());
    }

    private void handleAddAirplaneClick(float x, float y) {
        if (selectedLineForAirplane < 0) {
            selectedLineForAirplane = findLineIndexNear(x, y);
            navbar.setAirplaneLineSelected(selectedLineForAirplane >= 0);
            return;
        }

        int airportId = findAirportOnLineNear(selectedLineForAirplane, x, y);
        if (airportId >= 0) {
            presenter.addAirplaneToLine(selectedLineForAirplane, airportId);
            addAirplaneMode = false;
            selectedLineForAirplane = -1;
            navbar.setAddAirplaneActive(false);
            navbar.setAirplaneLineSelected(false);
            return;
        }

        int otherLine = findLineIndexNear(x, y);
        if (otherLine >= 0) {
            selectedLineForAirplane = otherLine;
            navbar.setAirplaneLineSelected(true);
        }
    }

    private int findAirportOnLineNear(int lineIndex, float x, float y) {
        if (lineIndex < 0 || lineIndex >= presenter.getLines().size()) return -1;
        model.Line line = presenter.getLines().get(lineIndex);
        for (int i = 0; i < line.size(); i++) {
            model.Airport airport = line.get(i);
            float dx = airport.getPosition().getX() - x;
            float dy = airport.getPosition().getY() - y;
            if (Math.hypot(dx, dy) <= 0.03) {
                return presenter.getAirports().indexOf(airport);
            }
        }
        return -1;
    }

    private int findLineIndexNear(float x, float y) {
        double bestDistance = 0.02;
        int bestLine = -1;
        var lines = presenter.getLines();
        for (int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
            model.Line line = lines.get(lineIndex);
            if (line.color != selectedColor) {
                continue;
            }
            for (int segment = 0; segment + 1 < line.size(); segment++) {
                model.Point a = line.get(segment).getPosition();
                model.Point b = line.get(segment + 1).getPosition();
                double distance = distanceToSegment(x, y, a.getX(), a.getY(), b.getX(), b.getY());
                if (distance < bestDistance) {
                    bestDistance = distance;
                    bestLine = lineIndex;
                }
            }
        }
        return bestLine;
    }

    private double distanceToSegment(float px, float py, float ax, float ay, float bx, float by) {
        float dx = bx - ax;
        float dy = by - ay;
        float lengthSquared = dx * dx + dy * dy;
        if (lengthSquared == 0) {
            return Math.hypot(px - ax, py - ay);
        }
        float t = ((px - ax) * dx + (py - ay) * dy) / lengthSquared;
        t = Math.max(0, Math.min(1, t));
        float projX = ax + t * dx;
        float projY = ay + t * dy;
        return Math.hypot(px - projX, py - projY);
    }

    private void positionNavbar() {
        double barWidth = navbar.getWidth();
        if (barWidth <= 0) {
            barWidth = this.getWidth() * 0.5;
        }
        double barHeight = navbar.getHeight();
        navbar.setLayoutX((this.getWidth() - barWidth) / 2.0);
        navbar.setLayoutY(this.getHeight() - barHeight - 18);
    }
}