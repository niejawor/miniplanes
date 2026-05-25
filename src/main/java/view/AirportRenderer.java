package view;

import model.Airport;
import model.Passenger;
import viewmodel.GamePresenter;

public class AirportRenderer {
    private final GamePresenter presenter;
    private final ShapePainter shapePainter;

    public AirportRenderer(GamePresenter presenter, ShapePainter shapePainter) {
        this.presenter = presenter;
        this.shapePainter = shapePainter;
    }

    public void drawAirports() {
        float size = 0.008f;
        model.Color color = model.Color.Red;

        for (Airport airport : presenter.getAirports()) {
            float x = airport.getPosition().getX();
            float y = airport.getPosition().getY();
            shapePainter.drawSingleShape(airport.getShape(), x, y, size, color);
            drawAirportDetails(airport);
        }
    }

    private void drawAirportDetails(Airport airport) {
        float x = airport.getPosition().getX();
        float y = airport.getPosition().getY();
        int passCount = 0;
        int maxPassengersToShow = 10;
        model.Color normalColor = model.Color.Blue;
        model.Color overloadColor = model.Color.Black;
        model.Color color = normalColor;

        for (Passenger p : airport.getPassengers()) {
            if (passCount >= maxPassengersToShow) {
                break;
            }
            if (passCount == airport.getAirportType().passengerCapacity) {
                color = overloadColor;
            }
            float px = x + ((passCount % 5) * 0.008f) + 0.004f;
            float py = y + ((passCount / 5) * 0.015f) + 0.028f;
            shapePainter.drawSingleShape(p.getDestination(), px, py, 0.003f, color);
            passCount++;
        }
    }
}
