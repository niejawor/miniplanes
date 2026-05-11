package model;

import java.util.ArrayList;
import java.util.List;

public class Airplane {
    ArrayList<Airport>path = new ArrayList<>();
    int idx = 0;

    private boolean currentlyParked = true;
    private float[] position = new float[2];

    AirplaneType type;

    private final List<Passenger> passengersOnBoard = new ArrayList<>();
    private float timeSpentInAirport = 0;

    Airplane(ArrayList<Airport>path, AirplaneType type) {
        this.path = path;
        position[0] = path.get(0).getPosition()[0];
        position[1] = path.get(0).getPosition()[1];
        this.type = type;
    }

    public void startJourney() {
        currentlyParked = false;
        idx++;
    }

    public void update(float deltaTime) {
        if (currentlyParked) {
            timeSpentInAirport += deltaTime;
            return;
        }

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
            tryLanding(target);
        } else {
            position[0] += (dx / distance) * moveDist;
            position[1] += (dy / distance) * moveDist;
        }
    }

    private void tryLanding(Airport airport) {
        if (airport.canLand()) {
            airport.processLanding(this);
            currentlyParked = true;
            timeSpentInAirport = 0;

            airport.unloadPassengers(this);
            airport.loadPassengers(this);

            prepareNextFlight();
        }
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
        }
    }

    public boolean isCurrentlyParked() {
        return currentlyParked;
    }

    public float getTimeSpentInAirport() {
        return timeSpentInAirport;
    }

    public float[] getPosition() {
        return position;
    }
}
