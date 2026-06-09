package model;

import javafx.util.Pair;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class Airplane {
    public final Line line;
    public int idx = 0;
    private boolean unloaded = false;
    private boolean loaded = false;
    private boolean flyingForward = true;
    private boolean currentlyFlying = false;

    private boolean valid = true;
    private boolean returnToPoolWhenRemoved = false;
    private Point position;

    // lotnisko, na ktorym samolot ostatnio wyladowal / w ktorym aktualnie stoi
    private Airport currentAirport;

    AirplaneType type;

    private final List<Passenger> passengersOnBoard = new CopyOnWriteArrayList<>();
    private Time timeSpent = new Time(0);

    public Airplane(Line line, AirplaneType type) {
        this.line = line;
        currentAirport = line.get(0);
        position = currentAirport.getPosition();
        this.type = type;
    }

    public Airplane(Line line, AirplaneType type, Airport startAirport) {
        this.line = line;
        currentAirport = line.contains(startAirport) ? startAirport : line.get(0);
        position = currentAirport.getPosition();
        this.type = type;
    }

    public Airplane(Line line, AirplaneType type, Airport startAirport, boolean returnToPoolWhenRemoved) {
        this(line, type, startAirport);
        this.returnToPoolWhenRemoved = returnToPoolWhenRemoved;
    }

    public Airport getOrigin() { return line.get(idx + (flyingForward ? -1 : 1)); }

    public Airport getDestination() {
        return line.get(idx);
    }

    public AirplaneType getType() {
        return type;
    }

    public boolean isFlyingForward() {
        return flyingForward;
    }

    public List<Passenger> getPassengersOnBoard() {
        return Collections.unmodifiableList(passengersOnBoard);
    }

    class IntBox {
        int val = 0;
        void inc() {
            val++;
        }
    };
    public int unloadPassengers(Airport airport, Updater updater, GameData data) {
        final IntBox nPassengers = new IntBox();

        passengersOnBoard.removeIf(passenger -> {
            data.stats.compute(new Pair<>(getOrigin().index, getDestination().index),
                    (k, stat) ->
                            new Pair<>((stat != null ? stat.getKey() : 0L) + updater.currentTime.getCurrentTime() - passenger.lastLandingTime.getCurrentTime(),
                                    (stat != null ? stat.getValue() : 0) + 1));

            passenger.lastLandingTime.setCurrentTime(updater.currentTime.getCurrentTime());

            if (passenger.getTargetAirports().contains(airport.getIndex())) {
                if(passenger.getDestination() == airport.getShape()){
                    nPassengers.inc();
                }
                else{
                    passenger.setCurrentAirport(airport);
                    airport.addPassenger(passenger);
                }
                line.incrementTransportedPassengers();
                return true;
            } else if (!passenger.wantsToBoard(this)) {
                passenger.setCurrentAirport(airport);
                airport.addPassenger(passenger);
                line.incrementTransportedPassengers();
                return true;
            }
            return false;
        });
        return nPassengers.val;
    }

    public boolean loadPassenger(Passenger passenger) {
        if (passengersOnBoard.size() < type.capacity) {
            passengersOnBoard.add(passenger);
            return true;
        }
        return false;
    }

    public void update(long deltaTime) {
        timeSpent.addTime(deltaTime);
        if (currentlyFlying)
            moveTowardsTarget(deltaTime);
    }

    private void moveTowardsTarget(long deltaTime) {
        Airport target = line.get(idx);
        Point targetPos = target.getPosition();

        float dx = targetPos.getX() - position.getX();
        float dy = targetPos.getY() - position.getY();
        float distance = position.distance(targetPos);


        float moveDist = type.speed * deltaTime;

        if (distance <= moveDist) {
            position = targetPos;
            currentAirport = target;
            target.airplaneReportsToLanding(this);
            currentlyFlying = false;
        } else {
            position.move((dx / distance) * moveDist, (dy / distance) * moveDist);
        }
    }

    public void startNextJourney() {
        currentlyFlying = true;
        timeSpent.setCurrentTime(0);
        prepareNextFlight();
    }

    private void prepareNextFlight() {
        int cur = line.indexOf(currentAirport);
        int n = line.size();

        // Po edycji trasy lotnisko, na ktorym stoi samolot, moglo zniknac z jego linii.
        // W takim wypadku samolot (po wyladowaniu pasazerow) wraca na poczatek trasy.
        if (cur < 0) {
            flyingForward = true;
            idx = 0;
            return;
        }

        if (n < 2) {
            idx = 0;
            return;
        }

        if (flyingForward) {
            if (cur + 1 < n) {
                idx = cur + 1;
            } else {
                flyingForward = false;
                idx = cur - 1;
            }
        } else {
            if (cur - 1 >= 0) {
                idx = cur - 1;
            } else {
                flyingForward = true;
                idx = cur + 1;
            }
        }
    }

    public Time getTimeSpent() {
        return timeSpent;
    }

    public Point getPosition() {
        return position.getCopy();
    }

    public void startTakeOffProcedure() {
        timeSpent.setCurrentTime(0);
    }

    public void startDockingProcedure() {
        timeSpent.setCurrentTime(0);
    }

    public void startLandingProcedure() {
        timeSpent.setCurrentTime(0);
    }

    public boolean isCurrentlyFlying() {
        return currentlyFlying;
    }

    public void setInvalid() { valid = false; }

    public boolean isValid() { return valid; }

    public boolean shouldReturnToPoolWhenRemoved() { return returnToPoolWhenRemoved; }

    public boolean hasUnloaded() { return unloaded; }
    public void setUnloaded(boolean b) { unloaded = b; }

    public boolean hasLoaded() { return loaded; }
    public void setLoaded(boolean b) { loaded = b; }
}
