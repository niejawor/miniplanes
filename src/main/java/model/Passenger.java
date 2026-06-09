package model;

import java.util.List;

public class Passenger {
    Shape destination;
    Airport currentAirport;
    List<Integer> targetAirports;
    GameData gameData;
    Time lastLandingTime;

    public Passenger(Shape destination,  Airport currentAirport, GameData gameData, long currentTime) {
        this.destination = destination;
        this.currentAirport = currentAirport;
        this.gameData = gameData;
        lastLandingTime = new Time(currentTime);
    }

    public Shape getDestination() {
        return destination;
    }

    public void changeDestination() {
        destination = gameData.getShapeHandler().getRandomUsed();
    }

    public List<Integer> getTargetAirports() {
        return targetAirports;
    }

    public void setCurrentAirport(Airport currentAirport) {
        this.currentAirport = currentAirport;
    }

    public boolean wantsToBoard(Airplane airplane) {
        List<List<Integer>> temp = gameData.getBestNextStop().get(currentAirport.getIndex());
        if(temp == null || temp.size() < Shape.values().length) return false;
        for(int i=0;i<Shape.values().length;i++){
            if(destination.equals(Shape.values()[i])){
                targetAirports = temp.get(i);
                break;
            }
        }

        int currentIndexInLine = 0;
        int counter = 0;
        for(Airport airport : airplane.line.getPath()){
            if(currentAirport.getIndex() == airport.getIndex()){
                currentIndexInLine =  counter;
                break;
            }
            counter++;
        }

        boolean scanForward;
        if (currentIndexInLine == 0) {
            scanForward = true;
        } else if (currentIndexInLine == airplane.line.size() - 1) {
            scanForward = false;
        } else {
            scanForward = airplane.isFlyingForward();
        }

        if (scanForward) {
            for(int i = currentIndexInLine+1; i<airplane.line.size(); i++){
                if(targetAirports.contains(airplane.line.get(i).getIndex())){
                    return true;
                }
            }
        } else {
            for(int i = currentIndexInLine-1; i>=0; i--){
                if(targetAirports.contains(airplane.line.get(i).getIndex())){
                    return true;
                }
            }
        }

        return false;
    }

    public boolean wantsToContinue(Airplane airplane) {
        return airplane.line.get(airplane.idx).getShape()  != destination;
    }
}