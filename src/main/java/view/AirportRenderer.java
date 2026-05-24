package view;

import javafx.scene.canvas.GraphicsContext;
import model.Airport;
import model.Passenger;

import java.util.List;

public class AirportRenderer {
    public void drawAirports(GraphicsContext gc, List<Airport> airports, double w, double h) {
        float size = 0.008f;
        model.Color color = model.Color.Red;
        for (Airport airport : airports) {
            float x = airport.getPosition().getX();
            float y = airport.getPosition().getY();
            ShapeDrawer.drawShape(gc, w, h, airport.getShape(), x, y, size, color);
            drawAirportDetails(gc, airport, w, h);
        }
    }

    private void drawAirportDetails(GraphicsContext gc, Airport airport, double w, double h) {
        float x = airport.getPosition().getX();
        float y = airport.getPosition().getY();

        int passCount = 0;
        int maxPassengersToShow = 10;

        model.Color normalColor = model.Color.Blue;
        model.Color overloadColor = model.Color.Black;
        model.Color color = normalColor;

        for (Passenger p : airport.getPassengers()) {
            if (passCount >= maxPassengersToShow) break;
            if (passCount == airport.getAirportType().passengerCapacity)
                color = overloadColor;
            float px = x + ((passCount % 5) * 0.008f) + 0.004f;
            float py = y + ((passCount / 5) * 0.015f) + 0.028f;
            ShapeDrawer.drawShape(gc, w, h, p.getDestination(), px, py, 0.003f, color);
            passCount++;
        }
    }
}
