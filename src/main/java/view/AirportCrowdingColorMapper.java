package view;

import javafx.scene.paint.Color;
import model.AirportCrowdingLevel;

public final class AirportCrowdingColorMapper {
    private AirportCrowdingColorMapper() {
    }

    public static Color mapCrowdingLevel(AirportCrowdingLevel level) {
        switch (level) {
            case NORMAL:
                return Color.web("#2ecc71");
            case OVERCROWDED_STARTED:
                return Color.web("#f1c40f");
            case OVERCROWDED_ONE_THIRD:
                return Color.web("#e67e22");
            case OVERCROWDED_TWO_THIRDS:
                return Color.web("#e74c3c");
            case GAME_OVER:
            default:
                return Color.web("#4a0f16");
        }
    }
}
