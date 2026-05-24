package view;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import model.Airport;
import model.Line;
import viewmodel.GamePresenter;

public class LineRenderer {
    private final GamePresenter presenter;

    public LineRenderer(GamePresenter presenter) {
        this.presenter = presenter;
    }

    public void drawLines(GraphicsContext gc, Canvas canvas) {
        if (presenter.getLines() == null || presenter.getLines().isEmpty()) {
            return;
        }

        gc.setLineWidth(5.0);
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setLineJoin(StrokeLineJoin.ROUND);

        double w = canvas.getWidth();
        double h = canvas.getHeight();

        for (Line line : presenter.getLines()) {
            if (line.size() < 2) {
                continue;
            }

            gc.setStroke(ColorMapper.mapModelColor(line.color));
            gc.beginPath();

            Airport first = line.get(0);
            gc.moveTo(first.getPosition().getX() * w, first.getPosition().getY() * h);

            for (int i = 1; i < line.size(); i++) {
                Airport airport = line.get(i);
                gc.lineTo(airport.getPosition().getX() * w, airport.getPosition().getY() * h);
            }
            gc.stroke();
        }

        gc.setLineWidth(1.0);
        gc.setLineCap(StrokeLineCap.SQUARE);
        gc.setLineJoin(StrokeLineJoin.MITER);
    }
}
