package view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import viewmodel.GamePresenter;

public class PauseOverlay extends StackPane {
    public PauseOverlay(Window window, GamePresenter presenter) {
        this.prefWidthProperty().bind(window.widthProperty());
        this.prefHeightProperty().bind(window.heightProperty());
        this.setStyle("-fx-background-color: rgba(0, 0, 0, 0.55);");

        VBox panel = new VBox(20);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(30));
        panel.setMaxWidth(400);
        panel.setMaxHeight(300);
        panel.setStyle("-fx-background-color: rgba(255,255,255,0.95); -fx-background-radius: 15; -fx-border-color: #bdc3c7; -fx-border-radius: 15; -fx-border-width: 2;");

        Label title = new Label("Game Paused");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #34495e;");

        Label resultLabel = new Label("Result: " + presenter.getResult());
        resultLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        int minutes = presenter.getMinutes();
        Label timeLabel = new Label(
                String.format(
                        "Day %s, %02d:%02d",
                        1 + ((minutes / 60) / 24),
                        (minutes / 60) % 24,
                        minutes % 60
                )
        );

        timeLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d;");

        HBox buttons = new HBox(15);
        buttons.setAlignment(Pos.CENTER);

        Button resumeButton = new Button("Resume Game");
        resumeButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20 10 20; -fx-background-radius: 5;");
        resumeButton.setOnAction(e -> {
            window.getChildren().remove(this);
            window.clearPauseOverlay();
            presenter.resumeGame();
            window.requestFocus();
        });

        Button startOverButton = new Button("Start Over");
        startOverButton.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20 10 20; -fx-background-radius: 5;");
        startOverButton.setOnAction(e -> {
            window.getChildren().remove(this);
            window.clearPauseOverlay();
            presenter.restartGame();
        });

        buttons.getChildren().addAll(resumeButton, startOverButton);
        panel.getChildren().addAll(title, resultLabel, timeLabel, buttons);
        this.getChildren().add(panel);
    }
}