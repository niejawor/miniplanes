package view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import model.Airplane;
import model.AirplaneType;
import model.Passenger;
import viewmodel.GamePresenter;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AirplaneRenderer {
    private final AirplaneTextureManager textureManager;
    private final GamePresenter presenter;
    private final ShapePainter shapePainter;
    private final Map<model.Color, Image> tintedCache = new HashMap<>();

    public AirplaneRenderer(GamePresenter presenter, AirplaneTextureManager textureManager, ShapePainter shapePainter) {
        this.textureManager = textureManager;
        this.presenter = presenter;
        this.shapePainter = shapePainter;
    }

    private double clamp(double v) {
        if (v < 0) return 0;
        if (v > 1) return 1;
        return v;
    }

    public void drawAirplanes(GraphicsContext gc, double w, double h, double zoom) {
        List<Airplane> airplanes = presenter.getAirplanes();
        double screenAspect = w / h;

        double textureAspect = textureManager.getTextureAspect();

        for (Airplane plane : airplanes) {
            if (!plane.isCurrentlyFlying() && zoom >= 15.0) continue;

            float x = plane.getPosition().getX();
            float y = plane.getPosition().getY();
            float scale = plane.getType() == AirplaneType.SmallAirplane ? 0.02f : 0.015f;

            float angle = 0f;
            if (plane.isCurrentlyFlying()) {
                float destX = plane.getDestination().getPosition().getX();
                float destY = plane.getDestination().getPosition().getY();

                float dx = destX - x;
                float dy = destY - y;

                angle = (float) Math.toDegrees(Math.atan2(dy * (h / w), dx));
            }

            double planeHeight = (scale * 2) / screenAspect;
            double planeWidth = planeHeight * textureAspect;

            gc.save();
            gc.translate(x * w, y * h);
            gc.rotate(angle);

            double drawWidth = planeWidth * w;
            double drawHeight = planeHeight * w;

            model.Color mc = (plane.line != null) ? plane.line.color : null;
            Image texToDraw = textureManager.getTexture(mc);

            gc.drawImage(texToDraw, -drawWidth / 2, -drawHeight / 2, drawWidth, drawHeight);
            gc.restore(); // Prawidłowe, pojedyncze przywrócenie stanu kontekstu

            if (zoom >= 8.0) {
                List<Passenger> passengers = plane.getPassengersOnBoard();

                if (!passengers.isEmpty()) {
                    double rad = Math.toRadians(angle);
                    float passSize = scale * 0.12f;
                    double maxSpread = planeWidth * 0.6;
                    double stepDist = passengers.size() > 1 ? maxSpread / (passengers.size() - 1) : 0;
                    double maxStepDist = scale * 0.25;

                    if (stepDist > maxStepDist) stepDist = maxStepDist;

                    double startDist = -((passengers.size() - 1) * stepDist) / 2.0;

                    int passCount = 0;
                    for (Passenger p : passengers) {
                        double dist = startDist + passCount * stepDist;

                        float px = x + (float) (dist * Math.cos(rad));
                        float py = y + (float) (dist * Math.sin(rad) * (w / h));

                        shapePainter.drawSingleShape(p.getDestination(), px, py, passSize, model.Color.Green);
                        passCount++;
                    }
                }
            }
        }
    }
}