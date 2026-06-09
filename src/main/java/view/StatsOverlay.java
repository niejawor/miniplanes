package view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import viewmodel.GameStatsSnapshot;
import viewmodel.LineStatsSnapshot;

public class StatsOverlay extends StackPane {
    public StatsOverlay(Window window, GameStatsSnapshot stats) {
        prefWidthProperty().bind(window.widthProperty());
        prefHeightProperty().bind(window.heightProperty());
        setStyle("-fx-background-color: rgba(0, 0, 0, 0.45);");
        setPickOnBounds(true);

        VBox panel = new VBox(16);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(28));
        panel.setMaxWidth(760);
        panel.setMaxHeight(640);
        panel.setStyle("-fx-background-color: rgba(255,255,255,0.97); -fx-background-radius: 15; "
                + "-fx-border-color: #bdc3c7; -fx-border-radius: 15; -fx-border-width: 2;");

        Label title = new Label("Statystyki rozgrywki");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #34495e;");

        GridPane summary = new GridPane();
        summary.setHgap(28);
        summary.setVgap(8);
        summary.setAlignment(Pos.CENTER);
        addSummaryRow(summary, 0, "Wynik", String.valueOf(stats.score()));
        addSummaryRow(summary, 1, "Czas", formatTime(stats.minutes()));
        addSummaryRow(summary, 2, "Lotniska", String.valueOf(stats.airportsCount()));
        addSummaryRow(summary, 3, "Linie", String.valueOf(stats.linesCount()));
        addSummaryRow(summary, 4, "Samoloty", stats.airplanesCount() + " (+" + stats.availableAirplanes() + " dostępne)");
        addSummaryRow(summary, 5, "Pasażerowie oczekujący", String.valueOf(stats.waitingPassengers()));
        addSummaryRow(summary, 6, "Pasażerowie w samolotach", String.valueOf(stats.onboardPassengers()));
        addSummaryRow(summary, 7, "Przepełnione lotniska", String.valueOf(stats.overcrowdedAirports()));
        addSummaryRow(summary, 8, "Średni czas podróży", formatSeconds(stats.averageTravelTimeSeconds()));

        Label linesTitle = new Label("Statystyki linii");
        linesTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #34495e;");

        VBox linesBox = new VBox(6);
        linesBox.setPadding(new Insets(4));
        if (stats.lineStats().isEmpty()) {
            Label empty = new Label("Brak utworzonych linii.");
            empty.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
            linesBox.getChildren().add(empty);
        } else {
            int lineNumber = 1;
            for (LineStatsSnapshot line : stats.lineStats()) {
                linesBox.getChildren().add(createLineRow(lineNumber++, line));
            }
        }

        ScrollPane lineScroll = new ScrollPane(linesBox);
        lineScroll.setFitToWidth(true);
        lineScroll.setMaxHeight(220);
        lineScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        Button backButton = new Button("Wróć");
        backButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px; "
                + "-fx-padding: 10 24 10 24; -fx-background-radius: 5;");
        backButton.setOnAction(e -> {
            window.getChildren().remove(this);
            window.clearStatsOverlay();
            window.requestFocus();
        });

        panel.getChildren().addAll(title, summary, linesTitle, lineScroll, backButton);
        getChildren().add(panel);
    }

    private void addSummaryRow(GridPane grid, int row, String label, String value) {
        Label labelNode = new Label(label + ":");
        labelNode.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        Label valueNode = new Label(value);
        valueNode.setStyle("-fx-font-size: 14px; -fx-text-fill: #34495e;");
        grid.add(labelNode, 0, row);
        grid.add(valueNode, 1, row);
    }

    private HBox createLineRow(int lineNumber, LineStatsSnapshot line) {
        HBox row = new HBox(18);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 12, 8, 12));
        row.setStyle("-fx-background-color: rgba(236,240,241,0.9); -fx-background-radius: 8;");

        Label colorDot = new Label("●");
        colorDot.setStyle("-fx-font-size: 22px; -fx-text-fill: " + colorToCss(line.color()) + ";");

        Label text = new Label(String.format(
                "Linia %d (%s): %d pasażerów, %d lotnisk, %d samolotów",
                lineNumber,
                line.color(),
                line.transportedPassengers(),
                line.airportsCount(),
                line.airplanesCount()
        ));
        text.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50;");

        row.getChildren().addAll(colorDot, text);
        return row;
    }

    private String formatTime(int minutes) {
        int days = 1 + (minutes / 60) / 24;
        int hours = (minutes / 60) % 24;
        int mins = minutes % 60;
        return String.format("Dzień %d, %02d:%02d", days, hours, mins);
    }

    private String formatSeconds(double seconds) {
        if (seconds <= 0.0) {
            return "brak danych";
        }
        return String.format("%.1f s", seconds);
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
}
