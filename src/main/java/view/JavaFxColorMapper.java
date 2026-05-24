package view;

import javafx.scene.paint.Color;

public class JavaFxColorMapper {
    public static Color mapModelColor(model.Color modelColor) {
        switch (modelColor) {
            case Red: return Color.color(0.8, 0.2, 0.2);
            case Green: return Color.color(0.2, 0.8, 0.2);
            case Blue: return Color.color(0.2, 0.2, 0.8);
            case Black:
            default: return Color.BLACK;
        }
    }
}
