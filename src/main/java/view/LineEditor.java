package view;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import model.Airport;
import model.Line;
import viewmodel.GamePresenter;

/**
 * Tryb edycji linii. Obslugiwane operacje (chwyt lewym przyciskiem na linii):
 *  - chwyt segmentu i przeciagniecie na wolne lotnisko -> wstawienie lotniska pomiedzy dwa istniejace,
 *  - chwyt lotniska brzegowego (pierwszego/ostatniego) i przeciagniecie na wolne lotnisko -> dodanie na koncu/poczatku,
 *  - klikniecie lotniska nalezacego do linii (bez przeciagania) -> usuniecie go z linii.
 */
public class LineEditor {
    private static final float SEGMENT_SELECT_DISTANCE = 0.02f;
    private static final float AIRPORT_HOVER_DISTANCE = 0.03f;
    private static final float NODE_GRAB_DISTANCE = 0.018f;
    private static final float CLICK_MOVE_THRESHOLD = 0.012f;

    private boolean editing = false;
    private int selectedLineIndex = -1;
    private int selectedSegmentStart = -1;
    private int selectedSegmentEnd = -1;

    private int grabbedNodeInLine = -1;
    private boolean grabbedNodeIsEndpoint = false;

    private int hoverAirportIndex = -1;
    private float pressX;
    private float pressY;
    private float mouseX;
    private float mouseY;

    public boolean tryStartEditing(float x, float y, GamePresenter presenter, model.Color selectedColor) {
        float bestDistance = Float.MAX_VALUE;
        int bestLine = -1;
        int bestStart = -1;
        int bestEnd = -1;

        for (int lineIndex = 0; lineIndex < presenter.getLines().size(); lineIndex++) {
            Line line = presenter.getLines().get(lineIndex);
            if (line.color != selectedColor) {
                continue;
            }
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
            pressX = x;
            pressY = y;
            detectGrabbedNode(x, y, presenter.getLines().get(bestLine));
            presenter.pauseGame();
            return true;
        }
        return false;
    }

    private void detectGrabbedNode(float x, float y, Line line) {
        grabbedNodeInLine = -1;
        grabbedNodeIsEndpoint = false;
        float best = NODE_GRAB_DISTANCE;
        for (int i = 0; i < line.size(); i++) {
            Airport airport = line.get(i);
            float dx = airport.getPosition().getX() - x;
            float dy = airport.getPosition().getY() - y;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);
            if (distance <= best) {
                best = distance;
                grabbedNodeInLine = i;
                grabbedNodeIsEndpoint = (i == 0 || i == line.size() - 1);
            }
        }
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
            if (line.contains(airport)) {
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

        Line line = presenter.getLines().get(selectedLineIndex);

        if (hoverAirportIndex != -1) {
            if (grabbedNodeIsEndpoint && grabbedNodeInLine != -1) {
                // Dodanie nowego lotniska na koncu/poczatku linii.
                int edgeAirportId = presenter.getAirports().indexOf(line.get(grabbedNodeInLine));
                presenter.addAirportToLineEdge(selectedLineIndex, hoverAirportIndex, edgeAirportId);
            } else {
                // Wstawienie nowego lotniska pomiedzy dwa istniejace na wybranym segmencie.
                int beforeId = presenter.getAirports().indexOf(line.get(selectedSegmentStart));
                int afterId = presenter.getAirports().indexOf(line.get(selectedSegmentEnd));
                presenter.insertAirportIntoLine(selectedLineIndex, hoverAirportIndex, beforeId, afterId);
            }
        } else if (grabbedNodeInLine != -1 && isClick()) {
            // Klikniecie lotniska nalezacego do linii (bez przeciagania) -> usuniecie z linii.
            int airportId = presenter.getAirports().indexOf(line.get(grabbedNodeInLine));
            presenter.removeAirportFromLine(selectedLineIndex, airportId);
        }

        stopEditing(presenter);
    }

    private boolean isClick() {
        return Math.hypot(mouseX - pressX, mouseY - pressY) < CLICK_MOVE_THRESHOLD;
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
        double width = canvas.getWidth();
        double height = canvas.getHeight();
        double mouseScreenX = mouseX * width;
        double mouseScreenY = mouseY * height;

        gc.save();
        gc.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        gc.setLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);
        Color selectedLineColor = ColorMapper.mapModelColor(line.color);

        boolean removeCandidate = grabbedNodeInLine != -1 && hoverAirportIndex == -1 && isClick();

        if (removeCandidate) {
            // Podswietl na czerwono lotnisko, ktore zostanie usuniete.
            Airport node = line.get(grabbedNodeInLine);
            double nx = node.getPosition().getX() * width;
            double ny = node.getPosition().getY() * height;
            gc.setStroke(Color.color(0.9, 0.2, 0.2));
            gc.setLineWidth(3.0);
            gc.strokeOval(nx - 14, ny - 14, 28, 28);
            gc.setStroke(Color.color(0.9, 0.2, 0.2));
            gc.setLineWidth(3.0);
            gc.strokeLine(nx - 8, ny - 8, nx + 8, ny + 8);
            gc.strokeLine(nx - 8, ny + 8, nx + 8, ny - 8);
        } else if (grabbedNodeIsEndpoint && grabbedNodeInLine != -1) {
            // Podglad doklejenia na brzegu - linia od brzegowego lotniska do kursora.
            Airport edge = line.get(grabbedNodeInLine);
            double ex = edge.getPosition().getX() * width;
            double ey = edge.getPosition().getY() * height;
            drawDashed(gc, ex, ey, mouseScreenX, mouseScreenY, selectedLineColor);
            drawTargetMarker(gc, mouseScreenX, mouseScreenY);
        } else {
            // Podglad wstawienia pomiedzy dwa lotniska wybranego segmentu.
            Airport start = line.get(selectedSegmentStart);
            Airport end = line.get(selectedSegmentEnd);
            double startX = start.getPosition().getX() * width;
            double startY = start.getPosition().getY() * height;
            double endX = end.getPosition().getX() * width;
            double endY = end.getPosition().getY() * height;

            gc.setStroke(selectedLineColor);
            gc.setLineWidth(4.0);
            gc.setLineDashes(10.0, 8.0);
            gc.beginPath();
            gc.moveTo(startX, startY);
            gc.lineTo(mouseScreenX, mouseScreenY);
            gc.lineTo(endX, endY);
            gc.stroke();
            gc.setLineDashes(null);
            drawTargetMarker(gc, mouseScreenX, mouseScreenY);
        }

        gc.restore();
    }

    private void drawDashed(GraphicsContext gc, double x1, double y1, double x2, double y2, Color color) {
        gc.setStroke(color);
        gc.setLineWidth(4.0);
        gc.setLineDashes(10.0, 8.0);
        gc.beginPath();
        gc.moveTo(x1, y1);
        gc.lineTo(x2, y2);
        gc.stroke();
        gc.setLineDashes(null);
    }

    private void drawTargetMarker(GraphicsContext gc, double x, double y) {
        gc.setFill(Color.color(0.2, 0.9, 0.2, 0.4));
        gc.fillOval(x - 10, y - 10, 20, 20);
        gc.setStroke(Color.GREENYELLOW);
        gc.setLineWidth(2.0);
        gc.strokeOval(x - 10, y - 10, 20, 20);
    }

    private void stopEditing(GamePresenter presenter) {
        editing = false;
        selectedLineIndex = -1;
        selectedSegmentStart = -1;
        selectedSegmentEnd = -1;
        grabbedNodeInLine = -1;
        grabbedNodeIsEndpoint = false;
        hoverAirportIndex = -1;
        presenter.resumeGame();
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
