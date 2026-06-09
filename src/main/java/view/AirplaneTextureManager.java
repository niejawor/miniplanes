package view;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import java.util.HashMap;
import java.util.Map;

public class AirplaneTextureManager {
    private final Image baseTexture;
    private final Map<model.Color, Image> tintedCache = new HashMap<>();

    public AirplaneTextureManager(Image baseTexture) {
        this.baseTexture = baseTexture;
    }

    public Image getTexture(model.Color modelColor) {
        if (modelColor == null) {
            return baseTexture;
        }
        return tintedCache.computeIfAbsent(modelColor, this::createTintedImage);
    }

    public double getTextureAspect() {
        return baseTexture.getWidth() / baseTexture.getHeight();
    }

    private Image createTintedImage(model.Color mc) {
        PixelReader pr = baseTexture.getPixelReader();
        int iw = (int) baseTexture.getWidth();
        int ih = (int) baseTexture.getHeight();
        WritableImage out = new WritableImage(iw, ih);
        PixelWriter pw = out.getPixelWriter();

        Color tint = ColorMapper.mapModelColor(mc);
        double blend = 0.6;

        for (int y = 0; y < ih; y++) {
            for (int x = 0; x < iw; x++) {
                Color oc = pr.getColor(x, y);
                double a = oc.getOpacity();
                if (a == 0) {
                    pw.setColor(x, y, new Color(0,0,0,0));
                } else {
                    double r = oc.getRed() * (1 - blend) + tint.getRed() * blend;
                    double g = oc.getGreen() * (1 - blend) + tint.getGreen() * blend;
                    double b = oc.getBlue() * (1 - blend) + tint.getBlue() * blend;
                    pw.setColor(x, y, new Color(clamp(r), clamp(g), clamp(b), a));
                }
            }
        }
        return out;
    }

    private double clamp(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }
}