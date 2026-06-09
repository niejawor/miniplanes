package viewmodel;

import model.Color;

public record LineStatsSnapshot(
        Color color,
        int airportsCount,
        int airplanesCount,
        int transportedPassengers
) {
}
