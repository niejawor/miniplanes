package model;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Airport {
    private static class AirplaneEntry {
        Instant arrivalTime;
        Airplane airplane;

        AirplaneEntry(Airplane airplane) {
            this.airplane = airplane;
            arrivalTime = Instant.now();
        }
    };

    private final Shape shape;
    private final Color color;
    private final float[] position;
    private final AirportType type;

    private final ArrayList<AirplaneEntry> parkedAirplanes = new ArrayList<>();

    private int activeTakeoffs = 0;
    private int activeLandings = 0;

    private final List<Passenger> passengers = new ArrayList<>();

    public Airport(Shape shape, Color color, float[] position, AirportType type) {
        this.shape = shape;
        this.color = color;
        this.position = position;
        this.type = type;
    }

    public Shape getShape() {
        return shape;
    }

    public Color getColor() {
        return color;
    }

    public AirportType getAirportType() {
        return type;
    }

    public float[] getPosition() {
        return position;
    }

    public List<Passenger> getPassengers() {
        return passengers;
    }

    public void addPassenger(Passenger passenger) {
        passengers.add(passenger);
    }

    public boolean isOverCrowded() {
        return passengers.size() > type.passengerCapacity;
    }

    public List<Airplane> getParkedAirplanes() {
        return parkedAirplanes.stream()
                .map(entry -> entry.airplane)
                .collect(Collectors.toList());
    }

    public boolean canLand() {
        if (parkedAirplanes.size() + activeLandings >= type.capacity) return false;
        return activeLandings < type.landingRunways;
    }

    public void processLanding(Airplane airplane) {
        if (!canLand()) return;
        activeLandings++;
        parkedAirplanes.add(new AirplaneEntry(airplane));
        activeLandings--; // Tutaj później dodamy wątek żeby za np. 1 sekundę kończył lądowanie
    }

    public void update(float deltaTime) {
        tryStartAirplane();
    }

    public void unloadPassengers(Airplane airplane) {
        // TODO
    }

    public void loadPassengers(Airplane airplane) {
        // TODO
    }

    public void tryStartAirplane() {
        if (parkedAirplanes.isEmpty() || activeTakeoffs >= type.takeoffRunways) return;
        AirplaneEntry entry = parkedAirplanes.get(0);
        if (Duration.between(entry.arrivalTime, Instant.now()).getSeconds() < 5) return;

        activeTakeoffs++;
        parkedAirplanes.remove(entry);
        entry.airplane.startJourney();
        activeTakeoffs--; // Tutaj później dodamy wątek żeby za np. 1 sekundę kończył star
    }
}
