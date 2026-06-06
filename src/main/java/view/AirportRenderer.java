package view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import model.Airport;
import model.AirportCrowdingLevel;
import model.Passenger;
import viewmodel.GamePresenter;

public class AirportRenderer {
    private final GamePresenter presenter;
    private final ShapePainter shapePainter;
    private final Image airplaneTexture;

    public AirportRenderer(GamePresenter presenter, ShapePainter shapePainter, Image airplaneTexture) {
        this.presenter = presenter;
        this.shapePainter = shapePainter;
        this.airplaneTexture = airplaneTexture;
    }

    public void drawAirports(GraphicsContext gc, double w, double h, double zoom) {
        float size = 0.008f;

        for (Airport airport : presenter.getAirports()) {
            float x = airport.getPosition().getX();
            float y = airport.getPosition().getY();

            float s;
            float b;

            if (zoom <= 1.0) {
                s = size;
                b = 1;
            } else if (zoom >= 15.0) {
                s = 2 * size;
                b = 2;
            } else {
                float t = (float) ((zoom - 1.0) / (15.0 - 1.0));
                s = size + t * size;
                b  = 1 + t;
            }

            AirportCrowdingLevel crowdingLevel = airport.getCrowdingLevel(presenter.getMaxOvercrowdedTime());
            Color airportColor = AirportCrowdingColorMapper.mapCrowdingLevel(crowdingLevel);
            shapePainter.drawSingleShape(airport.getShape(), x, y, s, airportColor);

            drawAirportDetails(airport, x, y, b);

            if (zoom >= 15.0) {
                float infraS = 0.0016f;
                drawAirportInfrastructure(gc, airport, infraS, x, y, w, h);

                for (Airport.AirplaneEntry entry : airport.getLandingAirplanes()) {
                    double progress = entry.airplane.getTimeSpent().getCurrentTime() / airport.getAirportType().timeSpentLanding;
                    drawDynamicAirplane(gc, infraS, x, y, w, h, progress, true, entry.terminalIndex, airport.getAirportType().terminals);
                }

                for (Airport.AirplaneEntry entry : airport.getStartingAirplanes()) {
                    double progress = entry.airplane.getTimeSpent().getCurrentTime() / airport.getAirportType().timeSpentTakingOff;
                    drawDynamicAirplane(gc, infraS, x, y, w, h, progress, false, entry.terminalIndex, airport.getAirportType().terminals);
                }

                for (Airport.AirplaneEntry entry : airport.getParkedAirplanes()) {
                    drawDynamicAirplane(gc, infraS, x, y, w, h, 0.0, false, entry.terminalIndex, airport.getAirportType().terminals);
                }
            }
        }
    }

    private void drawAirportInfrastructure(GraphicsContext gc, Airport airport, float s, float x, float y, double w, double h) {
        gc.save();

        gc.translate(x * w, y * h);
        gc.scale(w, w);

        double leftX = -3.5 * s;
        double rightX = 3.5 * s;
        double topY = -2.5 * s;
        double bottomY = 4.5 * s;
        double curve = 1.5 * s;

        Runnable iPath = () -> {
            gc.moveTo(leftX, bottomY);
            gc.lineTo(leftX, topY + curve);
            gc.quadraticCurveTo(leftX, topY, leftX + curve, topY);
            gc.lineTo(rightX - curve, topY);
            gc.quadraticCurveTo(rightX, topY, rightX, topY + curve);
            gc.lineTo(rightX, bottomY);
        };

        gc.setStroke(javafx.scene.paint.Color.web("#3a3a3a"));
        gc.setLineWidth(1.5 * s);
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setLineJoin(StrokeLineJoin.ROUND);
        gc.beginPath();
        iPath.run();
        gc.stroke();

        gc.setStroke(javafx.scene.paint.Color.WHITE);
        gc.setLineWidth(0.75 / w);
        gc.setLineDashes(1.0 * s, 2.0 * s);
        gc.setLineDashOffset(0);
        gc.beginPath();
        iPath.run();
        gc.stroke();
        gc.setLineDashes((double[]) null);

        int terminals = airport.getAirportType().terminals;
        if (terminals > 0) {
            double startX = -1.5 * s;
            double endX = 1.5 * s;
            double step = (terminals > 1) ? (endX - startX) / (terminals - 1) : 0;

            gc.setStroke(javafx.scene.paint.Color.web("#3a3a3a"));
            gc.setLineWidth(s);
            gc.setLineCap(StrokeLineCap.ROUND);

            for (int i = 0; i < terminals; i++) {
                double tx = (terminals > 1) ? startX + step * i : 0;

                gc.beginPath();
                gc.moveTo(tx + 0.15 * s, topY - 0.6*s);

                gc.lineTo(tx + 0.4 * s, topY - 1.9 * s);
                gc.stroke();
            }
        }

        gc.restore();
    }

    // Poniższą funkcję wygenerowałem korzystając ze sztucznej inteligencji (mimo to straciłem dużo godzin na to). Jeśli wystarczy czasu, to później postaram się ją napisać w jakiś sposób sam.
    private void drawDynamicAirplane(GraphicsContext gc, float s, float x, float y, double w, double h, double progress, boolean isLanding, int terminalIndex, int totalTerminals) {
        progress = Math.max(0.0, Math.min(1.0, progress));

        // Zmienne korespondujące z geometrią lotniska
        double leftX = -3.5 * s;
        double rightX = 3.5 * s;
        double topY = -2.5 * s;
        double bottomY = 4.5 * s;
        double curve = 1.5 * s;

        // Geometria terminali
        double startX = -1.5 * s;
        double endX = 1.5 * s;
        double step = (totalTerminals > 1) ? (endX - startX) / (totalTerminals - 1) : 0;
        double tx = (totalTerminals > 1) ? startX + step * terminalIndex : 0;

        double termBaseX = tx;
        double termBaseY = topY;
        double termTipX = tx + 0.4 * s;
        double termTipY = topY - 1.9 * s;

        double targetX = 0, targetY = 0, angle = 0;

        if (isLanding) {
            // --- PROCES LĄDOWANIA ---
            // Zamiast sztywnych progów, obliczamy je dynamicznie, by zachować płynną prędkość.
            // Definiujemy wagi dla długości każdego etapu (przybliżone).
            double distRunway = (bottomY - (topY + curve)); // Długość prostej pasa
            double distCurve1 = (Math.PI * curve / 2);      // Długość łuku
            double distTaxi = Math.abs(termBaseX - (leftX + curve)); // Długość do przejechania po prostej
            double distTerminal = Math.hypot(termTipX - termBaseX, termTipY - termBaseY); // Wjazd na stanowisko

            double totalDist = distRunway + distCurve1 + distTaxi + distTerminal;

            // Proporcje czasu względem przebytej drogi
            double p1 = distRunway / totalDist;
            double p2 = p1 + (distCurve1 / totalDist);
            double p3 = p2 + (distTaxi / totalDist);
            // p4 = 1.0

            if (progress < p1) {
                // Faza 1: Wjazd lewym pasem
                double t = progress / p1;
                targetX = leftX;
                targetY = bottomY + t * ((topY + curve) - bottomY);
                angle = -90;
            } else if (progress < p2) {
                // Faza 2: Skręt w prawo na drogę kołowania
                double t = (progress - p1) / (p2 - p1);
                double p0x = leftX, p0y = topY + curve;
                double p1x = leftX, p1y = topY;
                double p2x = leftX + curve, p2y = topY;

                targetX = Math.pow(1 - t, 2) * p0x + 2 * (1 - t) * t * p1x + Math.pow(t, 2) * p2x;
                targetY = Math.pow(1 - t, 2) * p0y + 2 * (1 - t) * t * p1y + Math.pow(t, 2) * p2y;

                double dx = 2 * (1 - t) * (p1x - p0x) + 2 * t * (p2x - p1x);
                double dy = 2 * (1 - t) * (p1y - p0y) + 2 * t * (p2y - p1y);
                angle = Math.toDegrees(Math.atan2(dy, dx));
            } else if (progress < p3) {
                // Faza 3: Kołowanie do bazy terminala
                double t = (progress - p2) / (p3 - p2);
                targetX = (leftX + curve) + t * (termBaseX - (leftX + curve));
                targetY = topY;
                angle = 0;
            } else {
                // Faza 4: Wjazd na pole terminala
                double t = (progress - p3) / (1.0 - p3);
                targetX = termBaseX + t * (termTipX - termBaseX);
                targetY = termBaseY + t * (termTipY - termBaseY);
                angle = Math.toDegrees(Math.atan2(termTipY - termBaseY, termTipX - termBaseX));
            }
        } else {
            // --- POSTÓJ I START ---
            if (progress == 0.0) {
                targetX = termTipX;
                targetY = termTipY;
                angle = Math.toDegrees(Math.atan2(termTipY - termBaseY, termTipX - termBaseX));
            } else {
                // Analogiczne wagi dla startu
                double distTerminalOut = Math.hypot(termBaseX - termTipX, termBaseY - termTipY);
                double distTaxiOut = Math.abs((rightX - curve) - termBaseX);
                double distCurve2 = (Math.PI * curve / 2);
                double distRunwayOut = (bottomY - (topY + curve));

                double totalDistOut = distTerminalOut + distTaxiOut + distCurve2 + distRunwayOut;

                double p1 = distTerminalOut / totalDistOut;
                double p2 = p1 + (distTaxiOut / totalDistOut);
                double p3 = p2 + (distCurve2 / totalDistOut);

                if (progress < p1) {
                    // Faza 1: Wypychanie z terminala
                    double t = progress / p1;
                    targetX = termTipX + t * (termBaseX - termTipX);
                    targetY = termTipY + t * (termBaseY - termTipY);
                    angle = Math.toDegrees(Math.atan2(termTipY - termBaseY, termTipX - termBaseX));
                } else if (progress < p2) {
                    // Faza 2: Kołowanie do prawego zakrętu
                    double t = (progress - p1) / (p2 - p1);
                    targetX = termBaseX + t * ((rightX - curve) - termBaseX);
                    targetY = topY;
                    angle = 0;
                } else if (progress < p3) {
                    // Faza 3: Skręt prawym łukiem
                    double t = (progress - p2) / (p3 - p2);
                    double p0x = rightX - curve, p0y = topY;
                    double p1x = rightX, p1y = topY;
                    double p2x = rightX, p2y = topY + curve;

                    targetX = Math.pow(1 - t, 2) * p0x + 2 * (1 - t) * t * p1x + Math.pow(t, 2) * p2x;
                    targetY = Math.pow(1 - t, 2) * p0y + 2 * (1 - t) * t * p1y + Math.pow(t, 2) * p2y;

                    double dx = 2 * (1 - t) * (p1x - p0x) + 2 * t * (p2x - p1x);
                    double dy = 2 * (1 - t) * (p1y - p0y) + 2 * t * (p2y - p1y);
                    angle = Math.toDegrees(Math.atan2(dy, dx));
                } else {
                    // Faza 4: Rozbieg i start z pasa
                    double t = (progress - p3) / (1.0 - p3);
                    targetX = rightX;
                    targetY = (topY + curve) + t * (bottomY - (topY + curve));
                    angle = 90;
                }
            }
        }

        gc.save();
        gc.translate(x * w, y * h);
        gc.scale(w, w);
        gc.translate(targetX, targetY);
        gc.rotate(angle);

        double planeSize = 3.5 * s;
        gc.drawImage(airplaneTexture, -planeSize / 2, -planeSize / 2, planeSize, planeSize);
        gc.restore();
    }

    private void drawAirportDetails(Airport airport, float x, float y, float s) {
        int passCount = 0;
        int maxPassengersToShow = 10;

        model.Color normalColor = model.Color.Blue;
        model.Color overloadColor = model.Color.Black;
        model.Color color = normalColor;

        float offsetX = -0.003f + 0.008f * s;
        float offsetY = -0.003f + 0.008f * s;

        for (Passenger p : airport.getPassengers()) {
            if (passCount >= maxPassengersToShow) break;

            if (passCount == airport.getAirportType().passengerCapacity)
                color = overloadColor;

            float px = x + offsetX + ((passCount % 5) * 0.008f) + 0.004f;
            float py = y + offsetY + ((passCount / 5) * 0.015f) + 0.028f;

            shapePainter.drawSingleShape(p.getDestination(), px, py, 0.003f, color);
            passCount++;
        }
    }
}