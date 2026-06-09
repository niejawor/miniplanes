package view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import viewmodel.GamePresenter;

public class GameOverOverlay extends StackPane {
    public GameOverOverlay(Window window, GamePresenter presenter) {
        this.prefWidthProperty().bind(window.widthProperty());
        this.prefHeightProperty().bind(window.heightProperty());
        this.setStyle("-fx-background-color: rgba(0, 0, 0, 0.75);");

        VBox panel = new VBox(20);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(30));
        panel.setMaxWidth(400);
        panel.setMaxHeight(300);
        panel.setStyle("-fx-background-color: rgba(255,255,255,0.95); -fx-background-radius: 15; -fx-border-color: #c0392b; -fx-border-radius: 15; -fx-border-width: 3;");

        Label title = new Label("GAME OVER");
        title.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #c0392b;");

        Label resultLabel = new Label("Result: " + presenter.getResult());
        resultLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

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

        Button startOverButton = new Button("Start Over");
        startOverButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 12 30 12 30; -fx-background-radius: 6;");
        startOverButton.setOnAction(e -> {
            window.getChildren().remove(this);
            window.clearGameOverOverlay();
            presenter.restartGame();
            window.requestFocus();
        });

        panel.getChildren().addAll(title, resultLabel, timeLabel, startOverButton);
        this.getChildren().add(panel);
    }
}