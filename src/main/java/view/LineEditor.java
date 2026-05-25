package view;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import model.Airport;
import model.Line;
import viewmodel.GamePresenter;

public class LineEditor {
    private static final float SEGMENT_SELECT_DISTANCE = 0.02f;
    private static final float AIRPORT_HOVER_DISTANCE = 0.03f;

    private boolean editing = false;
    private int selectedLineIndex = -1;
    private int selectedSegmentStart = -1;
    private int selectedSegmentEnd = -1;
    private int hoverAirportIndex = -1;
    private float mouseX;
    private float mouseY;

    public boolean tryStartEditing(float x, float y, GamePresenter presenter) {
        float bestDistance = Float.MAX_VALUE;
        int bestLine = -1;
        int bestStart = -1;
        int bestEnd = -1;

        for (int lineIndex = 0; lineIndex < presenter.getLines().size(); lineIndex++) {
            Line line = presenter.getLines().get(lineIndex);
            for (int segmentIndex = 0; segmentIndex < line.size() - 1; segmentIndex++) {
                Airport a = line.get(segmentIndex);
                Airport b = line.get(segmentIndex + 1);
                float distance = distanceToSegment(x, y, a.getPosition().getX(), a.getPosition().getY(), b.getPosition().getX(), b.getPosition().getY());
                if (distance < bestDistance) {
                    bestDistance = distance;
                    bestLine = lineIndex;
                    bestStart = segmentIndex;
                    bestEnd = segmentIndex + 1;
                }
            }
        }

        if (bestLine >= 0 && bestDistance <= SEGMENT_SELECT_DISTANCE) {
            selectedLineIndex = bestLine;
            selectedSegmentStart = bestStart;
            selectedSegmentEnd = bestEnd;
            editing = true;
            hoverAirportIndex = -1;
            presenter.pauseSimulation();
            return true;
        }
        return false;
    }

    public void updateMousePosition(float x, float y, GamePresenter presenter) {
        mouseX = x;
        mouseY = y;

        if (!editing) {
            return;
        }

        hoverAirportIndex = -1;
        Line line = presenter.getLines().get(selectedLineIndex);
        for (int airportIndex = 0; airportIndex < presenter.getAirports().size(); airportIndex++) {
            Airport airport = presenter.getAirports().get(airportIndex);
            if (airportOnLine(line, airport)) {
                continue;
            }
            float dx = airport.getPosition().getX() - x;
            float dy = airport.getPosition().getY() - y;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);
            if (distance <= AIRPORT_HOVER_DISTANCE) {
                hoverAirportIndex = airportIndex;
                break;
            }
        }
    }

    public void commit(GamePresenter presenter) {
        if (!editing) {
            return;
        }

        if (hoverAirportIndex != -1) {
            presenter.insertAirportIntoLine(selectedLineIndex, hoverAirportIndex,
                    presenter.getAirports().indexOf(presenter.getLines().get(selectedLineIndex).get(selectedSegmentStart)),
                    presenter.getAirports().indexOf(presenter.getLines().get(selectedLineIndex).get(selectedSegmentEnd)));
        }

        stopEditing(presenter);
    }

    public void cancel(GamePresenter presenter) {
        if (!editing) {
            return;
        }
        stopEditing(presenter);
    }

    public boolean isEditing() {
        return editing;
    }

    public void drawPreview(GraphicsContext gc, Canvas canvas, GamePresenter presenter) {
        if (!editing) {
            return;
        }

        Line line = presenter.getLines().get(selectedLineIndex);
        Airport start = line.get(selectedSegmentStart);
        Airport end = line.get(selectedSegmentEnd);

        double width = canvas.getWidth();
        double height = canvas.getHeight();
        double startX = start.getPosition().getX() * width;
        double startY = start.getPosition().getY() * height;
        double endX = end.getPosition().getX() * width;
        double endY = end.getPosition().getY() * height;
        double controlX = mouseX * width;
        double controlY = mouseY * height;

        gc.save();
        gc.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        gc.setLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);

        gc.setStroke(Color.LIGHTGREEN);
        gc.setLineWidth(4.0);
        gc.setLineDashes(10.0, 8.0);
        gc.beginPath();
        gc.moveTo(startX, startY);
        gc.lineTo(controlX, controlY);
        gc.lineTo(endX, endY);
        gc.stroke();
        gc.setLineDashes(null);

        gc.setFill(Color.color(0.2, 0.9, 0.2, 0.4));
        gc.fillOval(controlX - 10, controlY - 10, 20, 20);
        gc.setStroke(Color.GREENYELLOW);
        gc.setLineWidth(2.0);
        gc.strokeOval(controlX - 10, controlY - 10, 20, 20);

        gc.restore();
    }

    private void stopEditing(GamePresenter presenter) {
        editing = false;
        selectedLineIndex = -1;
        selectedSegmentStart = -1;
        selectedSegmentEnd = -1;
        hoverAirportIndex = -1;
        presenter.resumeSimulation();
    }

    private boolean airportOnLine(Line line, Airport airport) {
        for (int i = 0; i < line.size(); i++) {
            if (line.get(i) == airport) {
                return true;
            }
        }
        return false;
    }

    private float distanceToSegment(float px, float py, float ax, float ay, float bx, float by) {
        float dx = bx - ax;
        float dy = by - ay;
        float lengthSquared = dx * dx + dy * dy;
        if (lengthSquared == 0) {
            float diffx = px - ax;
            float diffy = py - ay;
            return (float) Math.sqrt(diffx * diffx + diffy * diffy);
        }

        float t = ((px - ax) * dx + (py - ay) * dy) / lengthSquared;
        t = Math.max(0, Math.min(1, t));
        float projX = ax + t * dx;
        float projY = ay + t * dy;
        float diffx = px - projX;
        float diffy = py - projY;
        return (float) Math.sqrt(diffx * diffx + diffy * diffy);
    }
}
