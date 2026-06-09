package viewmodel;

import java.util.List;

public record GameStatsSnapshot(
        int score,
        int minutes,
        int airportsCount,
        int linesCount,
        int airplanesCount,
        int availableAirplanes,
        int waitingPassengers,
        int onboardPassengers,
        int overcrowdedAirports,
        double averageTravelTimeSeconds,
        List<LineStatsSnapshot> lineStats
) {
}
