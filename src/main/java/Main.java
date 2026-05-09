import view.Window;
import viewmodel.GamePresenter;

public class Main {
    public static void main(String[] args) {
        GamePresenter presenter = new GamePresenter();
        Window window = new Window();

        window.open(presenter);
    }
}