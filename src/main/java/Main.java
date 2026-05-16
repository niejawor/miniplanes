import model.GameEngine;
import viewmodel.GamePresenter;

public class Main {
    public static void main(String[] args) {
        GamePresenter presenter = new GamePresenter(new GameEngine());
        presenter.gameLoop();
    }
}