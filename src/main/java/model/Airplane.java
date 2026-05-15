package model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Airplane {
    Line line;
    int idx = 0;
    private boolean flyingForward = true;

    private boolean currentlyFlying = false;
    private float[] position = new float[2];

    AirplaneType type;

    private final List<Passenger> passengersOnBoard = new ArrayList<>();
    private float timeSpent = 0;

    Airplane(Line line, AirplaneType type) {
        this.line = line;
        position[0] = line.get(0).getPosition()[0];
        position[1] = line.get(0).getPosition()[1];
        this.type = type;
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
                it.remove();
            }
        }
    }

    public void loadPassengers() {
        Iterator<Passenger> it = line.get(idx).getPassengers().iterator();
        Passenger p;
        while (it.hasNext() && passengersOnBoard.size() < type.capacity) {
            if ((p = it.next()).wantsToBoard(this)) {
                passengersOnBoard.add(p);
                it.remove();
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
        float[] targetPos = target.getPosition();

        float dx = targetPos[0] - position[0];
        float dy = targetPos[1] - position[1];
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        float moveDist = type.speed * deltaTime;

        if (distance <= moveDist) {
            position[0] = targetPos[0];
            position[1] = targetPos[1];
            target.airplaneReportsToLanding(this);
            currentlyFlying = false;
        } else {
            position[0] += (dx / distance) * moveDist;
            position[1] += (dy / distance) * moveDist;
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

        if (idx == -1 || idx == line.size()) {
            flyingForward = !flyingForward;
            if (idx == -1) idx = 0;
            else idx = line.size() - 1;
        }
    }

    public float getTimeSpent() {
        return timeSpent;
    }

    public float[] getPosition() {
        return position;
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
}
