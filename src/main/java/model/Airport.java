package model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javafx.util.Pair;

public class Airport {
    public static class AirplaneEntry {
        public int index;          // Terminal dla zaparkowanych, numer pasa startowego dla startujących i numer pasa lądowania dla lądujących
        public int terminalIndex;
        public Airplane airplane;

        public AirplaneEntry(Airplane airplane, int index, int terminalIndex) {
            this.airplane = airplane;
            this.index = index;
            this.terminalIndex = terminalIndex;
        }
    };

    private final Shape shape;
    private final Point position;
    private final AirportType type;
    final int index;

    static int nextIndex = 0;

    private final Updater updater;

    private final List<Passenger> passengers = new CopyOnWriteArrayList<>();

    private final List<Airplane> queuedAirplanes = new CopyOnWriteArrayList<>();

    private final List<AirplaneEntry> parkedAirplanes = new CopyOnWriteArrayList<>();
    private final List<AirplaneEntry> landingAirplanes = new CopyOnWriteArrayList<>();
    private final List<AirplaneEntry> startingAirplanes = new CopyOnWriteArrayList<>();

    private int currentlyFreeTerminal = 0;
    private int currentlyFreeLandingRunway = 0;
    private int currentlyFreeStartingRunway = 0;

    public Airport(Shape shape, Point position, AirportType type, Updater updater) {
        this.shape = shape;
        this.position = position;
        this.type = type;
        this.updater = updater;
        newPassengerThreshold.setInMinutes(300);
        index = nextIndex++;
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
        return Collections.unmodifiableList(passengers);
    }

    public List<AirplaneEntry> getParkedAirplanes() {
        return Collections.unmodifiableList(parkedAirplanes);
    }

    public List<AirplaneEntry> getLandingAirplanes() {
        return Collections.unmodifiableList(landingAirplanes);
    }

    public List<AirplaneEntry> getStartingAirplanes() {
        return Collections.unmodifiableList(startingAirplanes);
    }

    public List<Airplane> getQueuedAirplanes() {
        return Collections.unmodifiableList(queuedAirplanes);
    }

    public void addPassenger(Passenger passenger) {
        passengers.add(passenger);
    }

    private boolean isOverCrowded() {
        return passengers.size() > type.passengerCapacity;
    }

    private Time timeOverCrowded = new Time(0);
    private void updateOverCrowded(long deltaTime) {
        if (isOverCrowded()) timeOverCrowded.addTime(deltaTime);
        else timeOverCrowded.setCurrentTime(0);
    }

    public Time howLongOverCrowded() {
        return timeOverCrowded;
    }

    private int processParkedAirplanes(HashMap<Pair<Integer, Airport>, Pair<Long, Long>> stats) {
        int x = 0;
        for (AirplaneEntry entry : parkedAirplanes) {
            Airplane a = entry.airplane;

            if (!a.hasUnloaded() && a.getTimeSpent().getCurrentTime() >= 1.0f) {
                x += a.unloadPassengers(this, stats);
                a.setUnloaded(true);
            }

            if (!a.hasLoaded() && a.getTimeSpent().getCurrentTime() >= 2.0f) {
                passengers.removeIf(passenger -> {
                    if (passenger.wantsToBoard(a) && a.loadPassenger(passenger)) {
                        return true;
                    }
                    return false;
                });
                a.setLoaded(true);
            }
        }
        return x;
    }

    private Time lastNewPassenger = new Time(0);
    private Time newPassengerThreshold = new Time(0);
    private void generateNewPassengers(long deltaTime) {
        lastNewPassenger.addTime(deltaTime);
        if (lastNewPassenger.getInGameMinutes() >= newPassengerThreshold.getInGameMinutes()) {
            lastNewPassenger.setCurrentTime(0);
            updater.generatePassenger(this);
            //this.addPassenger(new Passenger(shapeHandler.getRandomUsed()));
        }
    }

    public int update(long deltaTime, HashMap<Pair<Integer, Airport>, Pair<Long, Long>> stats) {
        finishTakeOffs();
        finishLandings();

        int x = processParkedAirplanes(stats);

        processNewTakeOffs();
        processNewLandings();

        updateOverCrowded(deltaTime);
        generateNewPassengers(deltaTime);
        return x;
    }

    public void finishTakeOffs() {
        Iterator<AirplaneEntry> it = startingAirplanes.iterator();
        AirplaneEntry entry;
        while (it.hasNext()) {
            if ((entry=it.next()).airplane.getTimeSpent().getCurrentTime() >= type.timeSpentTakingOff) {
                if (entry.airplane.isValid()) entry.airplane.startNextJourney();
                else updater.getAirplanes().remove(entry.airplane);
                startingAirplanes.remove(entry);
                //it.remove();
            }
        }
    }

    public void finishLandings() {
        Iterator<AirplaneEntry> it = landingAirplanes.iterator();
        AirplaneEntry entry;
        while (it.hasNext()) {
            if ((entry=it.next()).airplane.getTimeSpent().getCurrentTime() >= type.timeSpentLanding) {
                parkedAirplanes.add(new AirplaneEntry(entry.airplane, currentlyFreeTerminal, currentlyFreeTerminal));
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
            landingAirplanes.add(new AirplaneEntry(a, currentlyFreeLandingRunway, (landingAirplanes.size() + currentlyFreeTerminal) % type.terminals));
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
            if (!a.hasLoaded()) break;
            a.setUnloaded(false);
            a.setLoaded(false);
            startingAirplanes.add(new AirplaneEntry(a, currentlyFreeStartingRunway, parkedAirplanes.get(0).terminalIndex));
            parkedAirplanes.remove(0);
            currentlyFreeStartingRunway++;
            currentlyFreeStartingRunway %= type.takeoffRunways;
            a.startTakeOffProcedure();
        }
    }

    public float distance(Airport airport){
        return airport.getPosition().distance(position);
    }
}
