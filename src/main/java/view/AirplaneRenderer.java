package view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import model.Airplane;
import model.AirplaneType;

import java.util.List;

public class AirplaneRenderer {
    private final Image airplaneTexture;

    public AirplaneRenderer(Image airplaneTexture) {
        this.airplaneTexture = airplaneTexture;
    }

    public void drawAirplanes(GraphicsContext gc, List<Airplane> airplanes, double w, double h) {
        double screenAspect = w / h;

        double texWidth = airplaneTexture.getWidth();
        double texHeight = airplaneTexture.getHeight();
        double textureAspect = texWidth / texHeight;

        for (Airplane plane : airplanes) {
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
        }
    }
}
