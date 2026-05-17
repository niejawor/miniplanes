package model;

public class Passenger {
    Shape destination;

    public Passenger(Shape destination) {
        this.destination = destination;
    }

    public Shape getDestination() {
        return destination;
    }

    public boolean wantsToBoard(Airplane airplane) {
        if (airplane.isFlyingForward()) {
            for (int i = airplane.idx; i < airplane.line.size(); i++)
                if (airplane.line.get(i).getShape() == destination) return true;
        } else {
            for (int i = airplane.idx; i >= 0; i--)
                if (airplane.line.get(i).getShape() == destination) return true;
        }

        return false;
    }

    public boolean wantsToContinue(Airplane airplane) {
        return airplane.line.get(airplane.idx).getShape()  != destination;
    }
}