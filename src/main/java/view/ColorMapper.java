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
            case Yellow:
                return javafx.scene.paint.Color.color(0.9, 0.75, 0.15);
            case Orange:
                return javafx.scene.paint.Color.color(0.9, 0.45, 0.15);
            case Purple:
                return javafx.scene.paint.Color.color(0.55, 0.25, 0.85);
            case Cyan:
                return javafx.scene.paint.Color.color(0.15, 0.75, 0.85);
            case Black:
            default:
                return javafx.scene.paint.Color.BLACK;
        }
    }
}
