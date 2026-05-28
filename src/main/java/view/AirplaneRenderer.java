package view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import model.Airplane;
import model.AirplaneType;
import model.Passenger;
import viewmodel.GamePresenter;

import java.util.List;

public class AirplaneRenderer {
    private final Image airplaneTexture;
    private final GamePresenter presenter;
    private final ShapePainter shapePainter;

    public AirplaneRenderer(GamePresenter presenter, Image airplaneTexture, ShapePainter shapePainter) {
        this.airplaneTexture = airplaneTexture;
        this.presenter = presenter;
        this.shapePainter = shapePainter;
    }

    public void drawAirplanes(GraphicsContext gc, double w, double h, double zoom) {
        List<Airplane> airplanes = presenter.getAirplanes();
        double screenAspect = w / h;

        double texWidth = airplaneTexture.getWidth();
        double texHeight = airplaneTexture.getHeight();
        double textureAspect = texWidth / texHeight;

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

            gc.drawImage(airplaneTexture, -drawWidth / 2, -drawHeight / 2, drawWidth, drawHeight);
            gc.restore();

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
