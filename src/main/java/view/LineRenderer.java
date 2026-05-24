package view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import model.Airport;
import model.Line;

import java.util.List;

public class LineRenderer {
    public void drawTempRoute(GraphicsContext gc, List<Airport> currentRoute, double w, double h, double mouseX, double mouseY) {
        if (currentRoute.isEmpty()) return;

        gc.setLineWidth(4.0);
        gc.setStroke(Color.color(0.3, 0.3, 0.3, 0.8));

        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setLineJoin(StrokeLineJoin.ROUND);

        gc.beginPath();
        Airport first = currentRoute.get(0);
        gc.moveTo(first.getPosition().getX() * w, first.getPosition().getY() * h);

        for (int i = 1; i < currentRoute.size(); i++) {
            Airport a = currentRoute.get(i);
            gc.lineTo(a.getPosition().getX() * w, a.getPosition().getY() * h);
        }
        gc.lineTo(mouseX * w, mouseY * h);
        gc.stroke();
        gc.setLineWidth(1.0);
        gc.setLineCap(StrokeLineCap.SQUARE);
        gc.setLineJoin(StrokeLineJoin.MITER);
    }

    public void drawLines(GraphicsContext gc, List<Line> lines, double w, double h) {
        gc.setLineWidth(5.0);
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setLineJoin(StrokeLineJoin.ROUND);

        for (Line line : lines) {
            if (line.size() < 2) continue;

            gc.setStroke(JavaFxColorMapper.mapModelColor(line.color));
            gc.beginPath();

            Airport first = line.get(0);
            gc.moveTo(first.getPosition().getX() * w, first.getPosition().getY() * h);

            for (int i = 1; i < line.size(); i++) {
                Airport a = line.get(i);
                gc.lineTo(a.getPosition().getX() * w, a.getPosition().getY() * h);
            }
            gc.stroke();
        }
        gc.setLineWidth(1.0);
        gc.setLineCap(StrokeLineCap.SQUARE);
        gc.setLineJoin(StrokeLineJoin.MITER);
    }
}
