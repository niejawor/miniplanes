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
    private Point position;

    AirplaneType type;

    private final List<Passenger> passengersOnBoard = new CopyOnWriteArrayList<>();
    private Time timeSpent = new Time(0);

    public Airplane(Line line, AirplaneType type) {
        this.line = line;
        position = line.get(0).getPosition();
        this.type = type;
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
    public int unloadPassengers(Airport airport, HashMap<Pair<Integer, Airport>, Pair<Long, Long>> stats) {
        final IntBox nPassengers = new IntBox();
        final int pass = passengersOnBoard.size();

        stats.compute(new Pair<>(getOrigin().index, getDestination()),
                (k, stat) ->
                        new Pair<>((stat != null ? stat.getKey() : 0) + pass*timeSpent.getCurrentTime(), (stat != null ? stat.getValue() : 0) + pass));

        passengersOnBoard.removeIf(passenger -> {
            if (passenger.destination == airport.getShape()) {
                nPassengers.inc();
                return true;
            } else if (!passenger.wantsToBoard(this)) {
                airport.addPassenger(passenger);
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

        //System.out.println("moveDist: " + moveDist);

        if (distance <= moveDist) {
            position = targetPos;
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
        if (flyingForward) {
            idx++;
            if (idx >= line.size()) {
                flyingForward = false;
                idx = line.size() - 2;
            }
        } else {
            idx--;
            if (idx <= -1) {
                flyingForward = true;
                idx = 1;
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

    public boolean hasUnloaded() { return unloaded; }
    public void setUnloaded(boolean b) { unloaded = b; }

    public boolean hasLoaded() { return loaded; }
    public void setLoaded(boolean b) { loaded = b; }
}
