package model;

public class Passenger {
    Shape destination;

    public boolean wantsToBoard(Airplane airplane) {
        for (int i = airplane.idx; i < airplane.path.size(); i++)
            if (airplane.path.get(i).getShape() == destination) return true;
        return false;
    }

    public boolean wantsToContinue(Airplane airplane) {
        return airplane.path.get(airplane.idx).getShape()  != destination;
    }
}