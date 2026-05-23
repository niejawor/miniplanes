package model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Airplane {
    final Line line;
    int idx = 0;
    private boolean flyingForward = true;
    private boolean valid = true;
    private boolean currentlyFlying = false;
    private Point position;

    AirplaneType type;

    private final List<Passenger> passengersOnBoard = new CopyOnWriteArrayList<>();;
    private float timeSpent = 0;

    Airplane(Line line, AirplaneType type) {
        this.line = line;
        position = line.get(0).getPosition();
        this.type = type;
    }

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
        return passengersOnBoard;
    }

    public void unloadPassengers() {
        Iterator<Passenger> it = passengersOnBoard.iterator();
        Passenger p;
        while (it.hasNext()) {
            if (!(p = it.next()).wantsToContinue(this)) {
                line.get(idx).addPassenger(p);
                passengersOnBoard.remove(p);
                //it.remove();
            }
        }
    }

    public void loadPassengers() {
        Iterator<Passenger> it = line.get(idx).getPassengers().iterator();
        Passenger p;
        while (it.hasNext() && passengersOnBoard.size() < type.capacity) {
            if ((p = it.next()).wantsToBoard(this)) {
                passengersOnBoard.add(p);
                line.get(idx).getPassengers().remove(p);
                //it.remove();
            }
        }
    }

    public void update(float deltaTime) {
        timeSpent += deltaTime;
        if (currentlyFlying)
            moveTowardsTarget(deltaTime);
    }

    private void moveTowardsTarget(float deltaTime) {
        Airport target = line.get(idx);
        Point targetPos = target.getPosition();

        float dx = targetPos.getX() - position.getX();
        float dy = targetPos.getY() - position.getY();
        float distance = position.distance(targetPos);

        float moveDist = type.speed * deltaTime;

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
        timeSpent = 0;
        prepareNextFlight();
    }

    private void prepareNextFlight() {
        if (flyingForward) idx++;
        else idx--;

        if (idx <= -1 || idx >= line.size()) {
            flyingForward = !flyingForward;
            if (idx <= -1) idx = 0;
            else idx = line.size() - 1;
        }
    }

    public float getTimeSpent() {
        return timeSpent;
    }

    public Point getPosition() {
        return position.getCopy();
    }

    public void startTakeOffProcedure() {
        timeSpent = 0;
    }

    public void startDockingProcedure() {
        timeSpent = 0;
    }

    public void startLandingProcedure() {
        timeSpent = 0;
    }

    public boolean isCurrentlyFlying() {
        return currentlyFlying;
    }

    public void setInvalid() { valid = false; }

    public boolean isValid() { return valid; }
}
