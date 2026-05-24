package view;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import model.Shape;

public class ShapePainter {
    private final GraphicsContext gc;
    private final Canvas canvas;

    public ShapePainter(GraphicsContext gc, Canvas canvas) {
        this.gc = gc;
        this.canvas = canvas;
    }

    public void drawSingleShape(Shape shape, float x, float y, float size, model.Color color) {
        double w = canvas.getWidth();
        gc.save();

        gc.translate(x * w, y * canvas.getHeight());
        gc.scale(w, w);

        float s;
        gc.setFill(ColorMapper.mapModelColor(color));
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1.8 / w);

        switch (shape) {
            case Triangle:
                s = size * 1.1f;
                gc.fillPolygon(new double[]{0, -s, s}, new double[]{-s, s, s}, 3);
                gc.strokePolygon(new double[]{0, -s, s}, new double[]{-s, s, s}, 3);
                break;
            case Circle:
                s = size * 1.05f;
                gc.fillOval(-s, -s, s * 2, s * 2);
                gc.strokeOval(-s, -s, s * 2, s * 2);
                break;
            case Diamond:
                s = size * 1.2f;
                gc.fillPolygon(new double[]{0, s, 0, -s}, new double[]{-s, 0, s, 0}, 4);
                gc.strokePolygon(new double[]{0, s, 0, -s}, new double[]{-s, 0, s, 0}, 4);
                break;
            case Pentagon:
                s = size * 1.15f;
                drawRegularPolygon(5, s);
                break;
            case Hexagon:
                s = size * 1.15f;
                drawRegularPolygon(6, s);
                break;
            case Cross:
                s = size * 1.1f;
                double third = s / 3.0;
                double[] xCross = {-third, third, third, s, s, third, third, -third, -third, -s, -s, -third};
                double[] yCross = {-s, -s, -third, -third, third, third, s, s, third, third, -third, -third};
                gc.fillPolygon(xCross, yCross, 12);
                gc.strokePolygon(xCross, yCross, 12);
                break;
            case Star:
                s = size * 1.35f;
                drawStar(10, s, s / 2.4f);
                break;
            case Square:
            default:
                s = size * 0.95f;
                gc.fillRect(-s, -s, s * 2, s * 2);
                gc.strokeRect(-s, -s, s * 2, s * 2);
                break;
        }

        gc.restore();
    }

    private void drawRegularPolygon(int sides, float radius) {
        double[] xPoints = new double[sides];
        double[] yPoints = new double[sides];
        for (int i = 0; i < sides; i++) {
            double rad = Math.toRadians(i * (360.0 / sides) - 90);
            xPoints[i] = Math.cos(rad) * radius;
            yPoints[i] = Math.sin(rad) * radius;
        }
        gc.fillPolygon(xPoints, yPoints, sides);
        gc.strokePolygon(xPoints, yPoints, sides);
    }

    private void drawStar(int points, float outerRadius, float innerRadius) {
        double[] xPoints = new double[points];
        double[] yPoints = new double[points];
        for (int i = 0; i < points; i++) {
            double rad = Math.toRadians(i * (360.0 / points) - 90);
            double r = (i % 2 == 0) ? outerRadius : innerRadius;
            xPoints[i] = Math.cos(rad) * r;
            yPoints[i] = Math.sin(rad) * r;
        }
        gc.fillPolygon(xPoints, yPoints, points);
        gc.strokePolygon(xPoints, yPoints, points);
    }
}
