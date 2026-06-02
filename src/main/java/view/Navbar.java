package view;

import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.util.ArrayList;
import java.util.List;

/**
 * Dolny pasek nawigacyjny pozwala wybrac kolor linii,
 * ktora chcemy narysowac oraz wlaczyc tryb dodawania samolotu do istniejacej linii.
 */
public class Navbar extends HBox {

    public interface Listener {
        void onColorSelected(model.Color color);
        void onAddAirplaneToggled(boolean active);
    }

    private final Listener listener;
    private List<model.Color> palette;
    private final List<Button> colorButtons = new ArrayList<>();
    private Button addAirplaneButton;

    private model.Color selectedColor;
    private boolean addAirplaneActive = false;
    private boolean airplaneLineSelected = false;
    private int availableAirplanes = 0;

    public Navbar(List<model.Color> palette, model.Color initialColor, int availableAirplanes, Listener listener) {
        this.palette = new ArrayList<>(palette);
        this.listener = listener;
        this.selectedColor = initialColor;
        this.availableAirplanes = availableAirplanes;
        build();

        addEventHandler(MouseEvent.MOUSE_PRESSED, Event::consume);
        addEventHandler(MouseEvent.MOUSE_RELEASED, Event::consume);
        addEventHandler(MouseEvent.MOUSE_CLICKED, Event::consume);
        addEventHandler(MouseEvent.MOUSE_DRAGGED, Event::consume);
        addEventHandler(ScrollEvent.SCROLL, Event::consume);
    }

    private void build() {
        getChildren().clear();
        colorButtons.clear();

        setAlignment(Pos.CENTER_LEFT);
        setSpacing(14);
        setPadding(new Insets(10, 20, 10, 20));
        setStyle("-fx-background-color: rgba(25,25,30,0.88); -fx-background-radius: 18;");
        setPickOnBounds(true);

        for (model.Color c : palette) {
            Button b = new Button();
            b.setPrefSize(42, 42);
            b.setMinSize(42, 42);
            b.setMaxSize(42, 42);
            b.setFocusTraversable(false);
            b.setOnAction(e -> selectColor(c));
            colorButtons.add(b);
            getChildren().add(b);
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        spacer.setMinWidth(20);
        getChildren().add(spacer);

        addAirplaneButton = new Button("\u2708 +");
        addAirplaneButton.setPrefHeight(42);
        addAirplaneButton.setFocusTraversable(false);
        addAirplaneButton.setOnAction(e -> toggleAddAirplane());
        getChildren().add(addAirplaneButton);

        refreshStyles();
    }

    private String cssColor(model.Color c) {
        switch (c) {
            case Red:   return "#cc3333";
            case Green: return "#33aa44";
            case Blue:  return "#3366dd";
            case Yellow: return "#e6bf26";
            case Orange: return "#e67326";
            case Purple: return "#8c40d9";
            case Cyan: return "#26bfd9";
            default:    return "#222222";
        }
    }

    private void selectColor(model.Color c) {
        selectedColor = c;
        if (addAirplaneActive) {
            addAirplaneActive = false;
            listener.onAddAirplaneToggled(false);
        }
        refreshStyles();
        listener.onColorSelected(c);
    }

    private void toggleAddAirplane() {
        addAirplaneActive = !addAirplaneActive;
        refreshStyles();
        listener.onAddAirplaneToggled(addAirplaneActive);
    }

    /** Pozwala oknu zsynchronizowac wyglad przycisku (np. po dodaniu samolotu do linii). */
    public void setAddAirplaneActive(boolean active) {
        if (addAirplaneActive == active) return;
        addAirplaneActive = active;
        if (!active) airplaneLineSelected = false;
        refreshStyles();
    }

    public void setAirplaneLineSelected(boolean selected) {
        if (airplaneLineSelected == selected) return;
        airplaneLineSelected = selected;
        refreshStyles();
    }

    public void refresh(List<model.Color> updatedPalette, int updatedAvailableAirplanes) {
        boolean paletteChanged = palette.size() != updatedPalette.size() || !palette.containsAll(updatedPalette);
        palette = new ArrayList<>(updatedPalette);
        availableAirplanes = updatedAvailableAirplanes;
        if (!palette.contains(selectedColor) && !palette.isEmpty()) {
            selectedColor = palette.get(0);
            listener.onColorSelected(selectedColor);
        }
        if (paletteChanged) {
            build();
        } else {
            refreshStyles();
        }
    }

    public model.Color getSelectedColor() { return selectedColor; }
    public boolean isAddAirplaneActive() { return addAirplaneActive; }

    private void refreshStyles() {
        for (int i = 0; i < palette.size(); i++) {
            model.Color c = palette.get(i);
            Button b = colorButtons.get(i);
            boolean selected = (c == selectedColor) && !addAirplaneActive;
            String border = selected
                    ? "-fx-border-color: white; -fx-border-width: 3;"
                    : "-fx-border-color: rgba(255,255,255,0.25); -fx-border-width: 2;";
            b.setStyle("-fx-background-color: " + cssColor(c) + ";"
                    + "-fx-background-radius: 21; -fx-border-radius: 21;" + border);
        }

        if (addAirplaneButton != null) {
            String base = "-fx-font-size: 16px; -fx-font-weight: bold;"
                    + "-fx-background-radius: 12; -fx-padding: 6 18 6 18;";
            if (addAirplaneActive && airplaneLineSelected) {
                addAirplaneButton.setText("Kliknij lotnisko (" + availableAirplanes + ")");
            } else if (addAirplaneActive) {
                addAirplaneButton.setText("Wybierz linie (" + availableAirplanes + ")");
            } else {
                addAirplaneButton.setText("\u2708 + (" + availableAirplanes + ")");
            }
            if (addAirplaneActive) {
                addAirplaneButton.setStyle(base + "-fx-background-color: #f0a000; -fx-text-fill: #1a1a1a;");
            } else {
                addAirplaneButton.setStyle(base + "-fx-background-color: rgba(255,255,255,0.18); -fx-text-fill: white;");
            }
            addAirplaneButton.setDisable(availableAirplanes <= 0);
        }
    }
}
