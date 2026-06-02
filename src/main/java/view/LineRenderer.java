package view;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import model.Airport;
import model.Line;
import viewmodel.GamePresenter;

import java.util.HashMap;
import java.util.Map;

public class LineRenderer {
    private final GamePresenter presenter;
    private static final double LINE_WIDTH = 5.0;
    private static final double PARALLEL_LINE_SPACING = 7.0;

    public LineRenderer(GamePresenter presenter) {
        this.presenter = presenter;
    }

    public void drawLines(GraphicsContext gc, Canvas canvas) {
        if (presenter.getLines() == null || presenter.getLines().isEmpty()) {
            return;
        }

        gc.setLineWidth(LINE_WIDTH);
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setLineJoin(StrokeLineJoin.ROUND);

        double w = canvas.getWidth();
        double h = canvas.getHeight();
        Map<EdgeKey, Integer> segmentCounts = countSegmentsBetweenAirports();
        Map<EdgeKey, Integer> drawnSegments = new HashMap<>();

        for (Line line : presenter.getLines()) {
            if (line.size() < 2) {
                continue;
            }

            gc.setStroke(ColorMapper.mapModelColor(line.color));

            for (int i = 0; i + 1 < line.size(); i++) {
                drawSegment(gc, line.get(i), line.get(i + 1), w, h, segmentCounts, drawnSegments);
            }
        }

        gc.setLineWidth(1.0);
        gc.setLineCap(StrokeLineCap.SQUARE);
        gc.setLineJoin(StrokeLineJoin.MITER);
    }

    private Map<EdgeKey, Integer> countSegmentsBetweenAirports() {
        Map<EdgeKey, Integer> counts = new HashMap<>();
        for (Line line : presenter.getLines()) {
            for (int i = 0; i + 1 < line.size(); i++) {
                EdgeKey key = new EdgeKey(line.get(i), line.get(i + 1));
                counts.put(key, counts.getOrDefault(key, 0) + 1);
            }
        }
        return counts;
    }

    private void drawSegment(
            GraphicsContext gc,
            Airport from,
            Airport to,
            double w,
            double h,
            Map<EdgeKey, Integer> segmentCounts,
            Map<EdgeKey, Integer> drawnSegments
    ) {
        EdgeKey key = new EdgeKey(from, to);
        int total = segmentCounts.getOrDefault(key, 1);
        int ordinal = drawnSegments.getOrDefault(key, 0);
        drawnSegments.put(key, ordinal + 1);

        double x1 = from.getPosition().getX() * w;
        double y1 = from.getPosition().getY() * h;
        double x2 = to.getPosition().getX() * w;
        double y2 = to.getPosition().getY() * h;

        if (total > 1) {
            double cx1 = key.first.getPosition().getX() * w;
            double cy1 = key.first.getPosition().getY() * h;
            double cx2 = key.second.getPosition().getX() * w;
            double cy2 = key.second.getPosition().getY() * h;
            double dx = cx2 - cx1;
            double dy = cy2 - cy1;
            double len = Math.hypot(dx, dy);
            if (len > 0) {
                double offset = (ordinal - ((total - 1) / 2.0)) * PARALLEL_LINE_SPACING;
                double nx = -dy / len;
                double ny = dx / len;
                x1 += nx * offset;
                y1 += ny * offset;
                x2 += nx * offset;
                y2 += ny * offset;
            }
        }

        gc.beginPath();
        gc.moveTo(x1, y1);
        gc.lineTo(x2, y2);
        gc.stroke();
    }

    private static final class EdgeKey {
        private final Airport first;
        private final Airport second;

        private EdgeKey(Airport a, Airport b) {
            if (System.identityHashCode(a) <= System.identityHashCode(b)) {
                this.first = a;
                this.second = b;
            } else {
                this.first = b;
                this.second = a;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof EdgeKey)) return false;
            EdgeKey other = (EdgeKey) o;
            return first == other.first && second == other.second;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(first) * 31 + System.identityHashCode(second);
        }
    }
}
