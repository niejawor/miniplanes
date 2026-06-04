package view;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import viewmodel.GamePresenter;

public class MainMenuOverlay extends StackPane {
    public MainMenuOverlay(Window window, GamePresenter presenter) {
        this.prefWidthProperty().bind(window.widthProperty());
        this.prefHeightProperty().bind(window.heightProperty());
        this.setStyle("-fx-background-color: #2c3e50;");

        VBox menuBox = new VBox(25);
        menuBox.setAlignment(Pos.CENTER);

        Label titleLabel = new Label(presenter.getTitle() != null ? presenter.getTitle() : "AIRPORT MANAGER");
        titleLabel.setStyle("-fx-font-size: 42px; -fx-font-weight: bold; -fx-text-fill: white;");

        Button startGameButton = new Button("Start Game");
        startGameButton.setStyle("-fx-font-size: 22px; -fx-padding: 12 35 12 35; -fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");
        startGameButton.setOnAction(e -> {
            window.getChildren().remove(this);
            window.clearMainMenuOverlay();
            presenter.resumeGame();
            window.requestFocus();
        });

        menuBox.getChildren().addAll(titleLabel, startGameButton);
        this.getChildren().add(menuBox);
    }
}