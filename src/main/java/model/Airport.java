package model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Airport {
    public static class AirplaneEntry {
        int index;          // Terminal dla zaparkowanych, numer pasa startowego dla startujących i numer pasa lądowania dla lądujących
        Airplane airplane;

        public AirplaneEntry(Airplane airplane, int index) {
            this.airplane = airplane;
            this.index = index;
        }
    };

    private final Shape shape;
    private final Point position;
    private final AirportType type;
    private final GameEngine gameEngine;

    private final List<Passenger> passengers = new CopyOnWriteArrayList<>();

    private final List<Airplane> queuedAirplanes = new CopyOnWriteArrayList<>();

    private final List<AirplaneEntry> parkedAirplanes = new CopyOnWriteArrayList<>();
    private final List<AirplaneEntry> landingAirplanes = new CopyOnWriteArrayList<>();
    private final List<AirplaneEntry> startingAirplanes = new CopyOnWriteArrayList<>();

    private int currentlyFreeTerminal = 0;
    private int currentlyFreeLandingRunway = 0;
    private int currentlyFreeStartingRunway = 0;

    public Airport(Shape shape, Point position, AirportType type, GameEngine engine) {
        this.shape = shape;
        this.position = position;
        this.type = type;
        gameEngine = engine;
    }

    public Shape getShape() {
        return shape;
    }

    public AirportType getAirportType() {
        return type;
    }

    public Point getPosition() {
        return new Point(position);
    }

    public List<Passenger> getPassengers() {
        return passengers;
    }

    public List<AirplaneEntry> getParkedAirplanes() {
        return parkedAirplanes;
    }

    public List<AirplaneEntry> getLandingAirplanes() {
        return landingAirplanes;
    }

    public List<AirplaneEntry> getStartingAirplanes() {
        return startingAirplanes;
    }

    public List<Airplane> getQueuedAirplanes() {
        return queuedAirplanes;
    }

    public void addPassenger(Passenger passenger) {
        passengers.add(passenger);
    }

    private boolean isOverCrowded() {
        return passengers.size() > type.passengerCapacity;
    }

    private float timeOverCrowded = 0;
    private void updateOverCrowded(float deltaTime) {
        if (isOverCrowded()) timeOverCrowded += deltaTime;
        else timeOverCrowded = 0;
    }

    public float howLongOverCrowded() {
        return timeOverCrowded;
    }

    private void unloadPassengersForAllPlanes() {
        for (AirplaneEntry entry : parkedAirplanes) {
            entry.airplane.unloadPassengers();
        }
    }

    private void loadPassengersForAllPlanes() {
        for (AirplaneEntry entry : parkedAirplanes) {
            entry.airplane.loadPassengers();
        }
    }

    private float lastNewPassenger = 0f;
    private final float newPassengerThreshold = 5f;
    private void generateNewPassengers(float deltaTime) {
        lastNewPassenger += deltaTime;
        if (lastNewPassenger >= newPassengerThreshold) {
            lastNewPassenger = 0;
            gameEngine.generatePassenger(this);
        }
    }

    public void update(float deltaTime) {
        finishTakeOffs();
        finishLandings();

        unloadPassengersForAllPlanes();
        loadPassengersForAllPlanes();

        processNewTakeOffs();
        processNewLandings();

        updateOverCrowded(deltaTime);
        generateNewPassengers(deltaTime);
    }

    public void finishTakeOffs() {
        Iterator<AirplaneEntry> it = startingAirplanes.iterator();
        AirplaneEntry entry;
        while (it.hasNext()) {
            if ((entry=it.next()).airplane.getTimeSpent() >= type.timeSpentTakingOff) {
                if (entry.airplane.isValid()) entry.airplane.startNextJourney();
                else gameEngine.getAirplanes().remove(entry.airplane);
                startingAirplanes.remove(entry);
                //it.remove();
            }
        }
    }

    public void finishLandings() {
        Iterator<AirplaneEntry> it = landingAirplanes.iterator();
        AirplaneEntry entry;
        while (it.hasNext()) {
            if ((entry=it.next()).airplane.getTimeSpent() >= type.timeSpentLanding) {
                parkedAirplanes.add(new AirplaneEntry(entry.airplane, currentlyFreeTerminal));
                currentlyFreeTerminal += 1;
                currentlyFreeTerminal %= type.terminals;
                entry.airplane.startDockingProcedure();
                landingAirplanes.remove(entry);
                //it.remove();
            }
        }
    }

    private boolean canLand() {
        if (queuedAirplanes.isEmpty()) return false;
        if (parkedAirplanes.size() + landingAirplanes.size() >= type.capacity) return false;
        return landingAirplanes.size() < type.landingRunways;
    }

    public void airplaneReportsToLanding(Airplane airplane) {
        queuedAirplanes.add(airplane);
    }

    public void processNewLandings() {
        while (canLand()) {
            Airplane a = queuedAirplanes.get(0);
            queuedAirplanes.remove(0);
            landingAirplanes.add(new AirplaneEntry(a, currentlyFreeLandingRunway));
            currentlyFreeLandingRunway++;
            currentlyFreeLandingRunway %= type.landingRunways;
            a.startLandingProcedure();
        }
    }

    public boolean canTakeOff() {
        if (parkedAirplanes.isEmpty()) return false;
        return startingAirplanes.size() < type.takeoffRunways;
    }

    public void processNewTakeOffs() {
        while (canTakeOff()) {
            Airplane a = parkedAirplanes.get(0).airplane;
            parkedAirplanes.remove(0);
            startingAirplanes.add(new AirplaneEntry(a, currentlyFreeStartingRunway));
            currentlyFreeStartingRunway++;
            currentlyFreeStartingRunway %= type.takeoffRunways;
            a.startTakeOffProcedure();
        }
    }

    public float distance(Airport airport){
        return airport.getPosition().distance(position);
    }
}
