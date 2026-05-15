package model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Airplane {
    ArrayList<Airport>path = new ArrayList<>();
    int idx = 0;

    private boolean currentlyFlying = false;
    private float[] position = new float[2];

    AirplaneType type;

    private final List<Passenger> passengersOnBoard = new ArrayList<>();
    private float timeSpent = 0;

    Airplane(ArrayList<Airport>path, AirplaneType type) {
        this.path = path;
        position[0] = path.get(0).getPosition()[0];
        position[1] = path.get(0).getPosition()[1];
        this.type = type;
    }

    public List<Passenger> getPassengersOnBoard() {
        return passengersOnBoard;
    }

    public void unloadPassengers() {
        Iterator<Passenger> it = passengersOnBoard.iterator();
        Passenger p;
        while (it.hasNext()) {
            if (!(p = it.next()).wantsToContinue(this)) {
                path.get(idx).addPassenger(p);
                it.remove();
            }
        }
    }

    public void loadPassengers() {
        Iterator<Passenger> it = path.get(idx).getPassengers().iterator();
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
        Airport target = path.get(idx);
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
        idx++;
        if (idx == path.size()) {
            for (int i = 0; i < path.size() / 2; i++) {
                Airport a = path.get(i);
                Airport b = path.get(path.size() - 1 - i);
                path.set(i, b);
                path.set(path.size() - 1 - i, a);
            }
            idx = 0;
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
