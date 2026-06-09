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
    private GameRenderer renderer;
    private final RouteBuilder routeBuilder;
    private final LineEditor lineEditor;
    private final Navbar navbar;
    private StackPane rewardOverlay;
    private StackPane addAirplaneColorOverlay;

    private MainMenuOverlay mainMenuOverlay;
    private PauseOverlay pauseOverlay;
    private GameOverOverlay gameOverOverlay;
    private StatsOverlay statsOverlay;

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
                    showAddAirplaneColorPopup();
                }
            }

            
        });
        navbar.prefWidthProperty().bind(this.widthProperty().multiply(0.5));

        this.widthProperty().addListener((obs, oldV, newV) -> { clampPan(); positionUIElements(); });
        this.heightProperty().addListener((obs, oldV, newV) -> { clampPan(); positionUIElements(); });
        navbar.heightProperty().addListener((obs, oldV, newV) -> positionUIElements());
        navbar.widthProperty().addListener((obs, oldV, newV) -> positionUIElements());

        getChildren().add(canvas);
        getChildren().add(navbar);
        setFocusTraversable(true);

        setOnMousePressed(e -> {
            if (mainMenuOverlay != null || pauseOverlay != null || gameOverOverlay != null) return;

            double mouseX = e.getX();
            double mouseY = e.getY();
            double w = getWidth();
            double h = getHeight();
            double cx = UIRenderer.CLOCK_CENTER_X * w;
            double radius = UIRenderer.CLOCK_RADIUS * w;
            double cy = UIRenderer.CLOCK_CENTER_Y * h;
            double bx = cx + radius + 15;
            double by = cy - 15;
            double bw = 60;
            double bh = 30;

            if (e.getButton() == MouseButton.PRIMARY && mouseX >= bx && mouseX <= bx + bw && mouseY >= by && mouseY <= by + bh) {
                showPauseOverlay();
                return;
            }

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
            if (mainMenuOverlay != null || pauseOverlay != null || gameOverOverlay != null) return;
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
            if (mainMenuOverlay != null || pauseOverlay != null || gameOverOverlay != null) return;
            double zoomFactor = e.getDeltaY() > 0 ? 1.1 : 0.9;
            applyZoom(zoomFactor, e.getX(), e.getY());
        });

        setOnKeyPressed(e -> {
            if (mainMenuOverlay != null || pauseOverlay != null || gameOverOverlay != null) return;
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
                    // if not in any special mode, ESC should open pause
                    showPauseOverlay();
                }
            }
        });

        showMainMenuOverlay();
    }

    public void render() {
        renderer.render(zoom, panX, panY);
    }

    public void refreshNavbar() {
        navbar.refresh(presenter.getPalette(), presenter.getAvailableAirplanes());
    }

    public void clearMainMenuOverlay() { this.mainMenuOverlay = null; }
    public void clearPauseOverlay() { this.pauseOverlay = null; }
    public void clearGameOverOverlay() { this.gameOverOverlay = null; }
    public void clearStatsOverlay() { this.statsOverlay = null; }

    public void showMainMenuOverlay() {
        if (mainMenuOverlay != null) return;
        presenter.pauseGame();

        mainMenuOverlay = new MainMenuOverlay(this, presenter);
        getChildren().add(mainMenuOverlay);
    }

    public void showPauseOverlay() {
        if (pauseOverlay != null || presenter.isGameOver() || mainMenuOverlay != null) return;
        presenter.pauseGame();

        pauseOverlay = new PauseOverlay(this, presenter);
        getChildren().add(pauseOverlay);
    }

    public void showGameOverOverlay() {
        if (gameOverOverlay != null) return;

        gameOverOverlay = new GameOverOverlay(this, presenter);
        getChildren().add(gameOverOverlay);
    }

    public void showStatsOverlay() {
        if (statsOverlay != null) return;

        statsOverlay = new StatsOverlay(this, presenter.getGameStatsSnapshot());
        getChildren().add(statsOverlay);
    }

    public void showRewardPopup(model.Color nextColor) {
        if (rewardOverlay != null) return;
        // ensure game is paused when reward popup is shown
        presenter.pauseGame();
        rewardOverlay = new StackPane();
        rewardOverlay.prefWidthProperty().bind(widthProperty());
        rewardOverlay.prefHeightProperty().bind(heightProperty());
        rewardOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.55);");
        rewardOverlay.setPickOnBounds(true);

        VBox panel = new VBox(20);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(30));
        panel.setMaxWidth(600);
        panel.setMaxHeight(320);
        panel.setStyle("-fx-background-color: rgba(255,255,255,0.95); -fx-background-radius: 15; -fx-border-color: #bdc3c7; -fx-border-radius: 15; -fx-border-width: 2;");

        Label title = new Label("Nowa nagroda");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #34495e;");

        Label text = new Label(nextColor == null
                ? "Wszystkie kolory sa juz odblokowane. Mozesz dobrac samolot."
                : "Wybierz: nowa linia z kolorem " + nextColor + " albo dodatkowy samolot.");
        text.setWrapText(true);
        text.setStyle("-fx-font-size: 15px; -fx-text-fill: #7f8c8d;");

        HBox buttons = new HBox(15);
        buttons.setAlignment(Pos.CENTER);

        if (nextColor != null) {
            Button lineButton = new Button("Linia: " + nextColor);
            lineButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20 10 20; -fx-background-radius: 5;");
            lineButton.setFocusTraversable(false);
            lineButton.setOnAction(e -> {
                closeRewardPopup();
                presenter.chooseLineReward();
            });
            buttons.getChildren().add(lineButton);
        }

        Button airplaneButton = new Button("Samolot +1");
        airplaneButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20 10 20; -fx-background-radius: 5;");
        airplaneButton.setFocusTraversable(false);
        airplaneButton.setOnAction(e -> {
            closeRewardPopup();
            presenter.chooseAirplaneReward();
        });

        Button skipButton = new Button("Pomiń");
        skipButton.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #2c3e50; -fx-font-size: 14px; -fx-padding: 10 20 10 20; -fx-background-radius: 5;");
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

    public void showAddAirplaneColorPopup() {
        if (addAirplaneColorOverlay != null) return;

        // pause game while selecting color/line to add airplane
        presenter.pauseGame();

        addAirplaneColorOverlay = new StackPane();
        addAirplaneColorOverlay.prefWidthProperty().bind(widthProperty());
        addAirplaneColorOverlay.prefHeightProperty().bind(heightProperty());
        addAirplaneColorOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.55);");
        addAirplaneColorOverlay.setPickOnBounds(true);

        VBox panel = new VBox(12);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(20));
        panel.setMaxWidth(420);
        panel.setMaxHeight(320);
        panel.setStyle("-fx-background-color: rgba(255,255,255,0.95); -fx-background-radius: 15; -fx-border-color: #bdc3c7; -fx-border-radius: 15; -fx-border-width: 2;");

        Label title = new Label("Wybierz kolor linii");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #34495e;");

        HBox colors = new HBox(12);
        colors.setAlignment(Pos.CENTER);

        // gather distinct colors used by existing lines
        java.util.Set<model.Color> usedColors = new java.util.LinkedHashSet<>();
        for (model.Line l : presenter.getLines()) {
            if (l != null && l.color != null) usedColors.add(l.color);
        }

        if (usedColors.isEmpty()) {
            Label none = new Label("Brak istniejących linii.");
            none.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
            panel.getChildren().addAll(title, none);
        } else {
            for (model.Color c : usedColors) {
                VBox item = new VBox(6);
                item.setAlignment(Pos.CENTER);
                Button b = new Button();
                b.setPrefSize(44, 44);
                b.setStyle("-fx-background-color: " + colorToCss(c) + "; -fx-background-radius: 22; -fx-border-radius: 22; -fx-border-color: rgba(0,0,0,0.12); -fx-border-width: 2;");
                b.setFocusTraversable(false);
                Label name = new Label(c.toString());
                name.setStyle("-fx-font-size: 12px; -fx-text-fill: #34495e;");
                b.setOnAction(e -> {
                    // select color and enter add-airplane mode; next click will pick airport on a line of this color
                    selectedColor = c;
                    navbar.setAddAirplaneActive(true);
                    addAirplaneMode = true;
                    selectedLineForAirplane = -2; // special: waiting for airport click to add airplane directly
                    navbar.setAirplaneLineSelected(true);
                    closeAddAirplaneColorPopup();
                    requestFocus();
                });
                item.getChildren().addAll(b, name);
                colors.getChildren().add(item);
            }
            panel.getChildren().addAll(title, colors);
        }

        Button cancel = new Button("Anuluj");
        cancel.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #2c3e50; -fx-font-size: 14px; -fx-padding: 8 14 8 14; -fx-background-radius: 6;");
        cancel.setOnAction(e -> {
            // ensure we exit add-airplane mode when cancelling
            addAirplaneMode = false;
            navbar.setAddAirplaneActive(false);
            navbar.setAirplaneLineSelected(false);
            closeAddAirplaneColorPopup();
        });

        panel.getChildren().add(cancel);
        addAirplaneColorOverlay.getChildren().add(panel);
        getChildren().add(addAirplaneColorOverlay);
    }

    private void closeAddAirplaneColorPopup() {
        if (addAirplaneColorOverlay == null) return;
        getChildren().remove(addAirplaneColorOverlay);
        addAirplaneColorOverlay = null;
        // resume game only if no other overlays that should pause are present
        if (mainMenuOverlay == null && pauseOverlay == null && gameOverOverlay == null && rewardOverlay == null) {
            presenter.resumeGame();
        }
        requestFocus();
    }

    private String colorToCss(model.Color c) {
        switch (c) {
            case Red: return "#cc3333";
            case Green: return "#33aa44";
            case Blue: return "#3366dd";
            case Yellow: return "#e6bf26";
            case Orange: return "#e67326";
            case Purple: return "#8c40d9";
            case Cyan: return "#26bfd9";
            default: return "#222222";
        }
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
        if (selectedLineForAirplane == -2) {
            // special mode: waiting for airport click, then add airplane to any line of selectedColor that contains that airport
            int airportId = findAirportNear(x, y);
            if (airportId >= 0) {
                // find a line with matching color that contains this airport
                var lines = presenter.getLines();
                for (int i = 0; i < lines.size(); i++) {
                    model.Line line = lines.get(i);
                    if (line.color == selectedColor && line.contains(presenter.getAirports().get(airportId))) {
                        presenter.addAirplaneToLine(i, airportId);
                        addAirplaneMode = false;
                        selectedLineForAirplane = -1;
                        navbar.setAddAirplaneActive(false);
                        navbar.setAirplaneLineSelected(false);
                        return;
                    }
                }
                // no matching colored line found for this airport: fall back to selecting a line
            }
            selectedLineForAirplane = findLineIndexNear(x, y);
            navbar.setAirplaneLineSelected(selectedLineForAirplane >= 0);
            return;
        }

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

    private int findAirportNear(float x, float y) {
        var airports = presenter.getAirports();
        for (int i = 0; i < airports.size(); i++) {
            model.Airport airport = airports.get(i);
            float dx = airport.getPosition().getX() - x;
            float dy = airport.getPosition().getY() - y;
            if (Math.hypot(dx, dy) <= 0.03) {
                return i;
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

    private void positionUIElements() {
        double barWidth = navbar.getWidth();
        if (barWidth <= 0) {
            barWidth = this.getWidth() * 0.5;
        }
        double barHeight = navbar.getHeight();
        navbar.setLayoutX((this.getWidth() - barWidth) / 2.0);
        navbar.setLayoutY(this.getHeight() - barHeight - 18);
    }

    @Deprecated
    private void positionNavbar() {
        positionUIElements();
    }

    /** Reset window-local state after a game restart. */
    public void resetAfterRestart() {
        this.zoom = 1.0;
        this.panX = 0.0;
        this.panY = 0.0;

        this.renderer = new GameRenderer(presenter, canvas, routeBuilder, lineEditor);

        routeBuilder.clear();
        try {
            if (lineEditor.isEditing()) lineEditor.cancel(presenter);
        } catch (Exception ignored) {}
        addAirplaneMode = false;
        selectedLineForAirplane = -1;

        if (presenter.getPalette() != null && !presenter.getPalette().isEmpty()) {
            this.selectedColor = presenter.getPalette().get(0);
        }

        if (navbar != null) {
            navbar.setAddAirplaneActive(false);
            navbar.setAirplaneLineSelected(false);
        }

        closeAddAirplaneColorPopup();
        closeRewardPopup();

        if (mainMenuOverlay != null) {
            getChildren().remove(mainMenuOverlay);
            mainMenuOverlay = null;
        }
        if (pauseOverlay != null) {
            getChildren().remove(pauseOverlay);
            pauseOverlay = null;
        }
        if (gameOverOverlay != null) {
            getChildren().remove(gameOverOverlay);
            gameOverOverlay = null;
        }

        if (statsOverlay != null) {
            getChildren().remove(statsOverlay);
            statsOverlay = null;
        }
        requestFocus();
    }
}