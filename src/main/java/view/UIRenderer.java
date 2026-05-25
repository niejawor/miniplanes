package view;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import model.Weekdays;

public class UIRenderer {
    public static final double CLOCK_CENTER_X = 0.96;
    public static final double CLOCK_CENTER_Y = 0.06;
    public static final double CLOCK_RADIUS = 0.016;

    private static final Color TEXT_MAIN_COLOR = Color.color(0.18, 0.18, 0.18);
    private static final Color TEXT_SECONDARY_COLOR = Color.color(0.5, 0.5, 0.5);
    private static final Color CLOCK_DARK_BG = Color.color(0.18, 0.18, 0.18);
    private static final Color CLOCK_LIGHT_DETAILS = Color.color(0.85, 0.85, 0.85);
    private static final Color CLOCK_OUTER_BORDER = Color.color(0.10, 0.10, 0.10);

    public void drawUI(GraphicsContext gc, double w, double h, int minutes, Weekdays day, int score) {
        gc.save();

        double cx = CLOCK_CENTER_X * w;
        double cy = CLOCK_CENTER_Y * h;
        double radius = CLOCK_RADIUS * w;

        drawClock(gc, cx, cy, radius, minutes);
        drawText(gc, w, h, cx, cy, radius, day, score);

        gc.restore();
    }

    private void drawClock(GraphicsContext gc, double cx, double cy, double radius, int minutes) {
        gc.setFill(CLOCK_DARK_BG);
        gc.fillOval(cx - radius, cy - radius, radius * 2, radius * 2);

        gc.setStroke(CLOCK_LIGHT_DETAILS);
        for (int i = 0; i < 12; i++) {
            double tickAngleRad = Math.toRadians(i * 30.0 - 90.0);
            double cos = Math.cos(tickAngleRad);
            double sin = Math.sin(tickAngleRad);

            double xOuter = cx + cos * radius;
            double yOuter = cy + sin * radius;

            double tickLength;
            if (i % 3 == 0) {
                gc.setLineWidth(2.0);
                tickLength = radius * 0.25;
            } else {
                gc.setLineWidth(1.0);
                tickLength = radius * 0.15;
            }

            double xInner = cx + cos * (radius - tickLength);
            double yInner = cy + sin * (radius - tickLength);

            gc.strokeLine(xOuter, yOuter, xInner, yInner);
        }

        double angle = (minutes / (12.0 * 60.0)) * 360.0 - 90.0;
        double angleRad = Math.toRadians(angle);

        double handX = cx + Math.cos(angleRad) * radius * 0.7;
        double handY = cy + Math.sin(angleRad) * radius * 0.7;

        gc.setStroke(CLOCK_LIGHT_DETAILS);
        gc.setLineWidth(2.0);
        gc.strokeLine(cx, cy, handX, handY);

        gc.setStroke(CLOCK_OUTER_BORDER);
        gc.setLineWidth(2.5);
        gc.strokeOval(cx - radius, cy - radius, radius * 2, radius * 2);
    }

    private void drawText(GraphicsContext gc, double w, double h, double cx, double cy, double radius, Weekdays day, int score) {
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.setTextBaseline(VPos.CENTER);

        gc.setFont(Font.font("SansSerif", FontWeight.BOLD, h * 0.023));
        gc.setFill(TEXT_MAIN_COLOR);

        double textX = cx - radius - (w * 0.01);

        double dayY = cy - (radius * 0.5);
        gc.fillText(day.toString(), textX, dayY);

        gc.setFont(Font.font("SansSerif", FontWeight.NORMAL, h * 0.019));
        gc.setFill(TEXT_SECONDARY_COLOR);

        double scoreY = cy + (radius * 0.5);
        gc.fillText(String.valueOf(score), textX, scoreY);

        gc.restore();
    }
}
