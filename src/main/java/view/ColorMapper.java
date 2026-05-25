package view;

public final class ColorMapper {
    private ColorMapper() {
    }

    public static javafx.scene.paint.Color mapModelColor(model.Color modelColor) {
        switch (modelColor) {
            case Red:
                return javafx.scene.paint.Color.color(0.8, 0.2, 0.2);
            case Green:
                return javafx.scene.paint.Color.color(0.2, 0.8, 0.2);
            case Blue:
                return javafx.scene.paint.Color.color(0.2, 0.2, 0.8);
            case Black:
            default:
                return javafx.scene.paint.Color.BLACK;
        }
    }
}
