package model;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class Updater {

    GameData data;

    private Time currentTime = new Time(0);

    private final long weekTime = 7;


    public Updater(GameData data) {
        this.data = data;
    }

    void generatePassenger(Airport airport) {
        airport.addPassenger(new Passenger(data.getShapeHandler().getRandomUsed()));
    }

    List<Airplane> getAirplanes() {
        return data.getAirplanes();
    }



    public class Result {
        int passengersTransported;
        int numberOfAddedLines;
        int numberOfAddedAirplanes;

        boolean gameOver;

        private final List<Airport> airports;
        private final List<Airplane> airplanes;
        List<Line> lines;

        Result(List<Airport> airports,  List<Airplane> airplanes, List<Line> lines, int numberOfAddedLines, int numberOfAddedAirplanes, boolean gameOver, int passengersTransported) {
            this.airports =  airports;
            this.numberOfAddedLines = numberOfAddedLines;
            this.numberOfAddedAirplanes = numberOfAddedAirplanes;
            this.gameOver = gameOver;
            this.passengersTransported = passengersTransported;

            this.airplanes = airplanes;
            this.lines = lines;
        }

        public boolean isItOver() {
            return gameOver;
        }

        public int getPassengersTransported() {
            return passengersTransported;
        }

    }

    Airport getNextAirport() {
        Airport next = data.getAirportSupplier().get();
        data.getShapeHandler().updateUse(next.getShape());
        return next;
    }

    Time timeOfLastAirportAdded = new Time(0);
    Time timeOfLastUpdate = new Time(0);

    public Result update(long deltaTime){

        currentTime.addTime(deltaTime);

        int numberOfAddedLines = 0;
        int numberOfAddedAirplanes = 0;

        if(timeOfLastUpdate.getCurrentTime() == 0){
            timeOfLastUpdate.setCurrentTime(timeOfLastUpdate.getCurrentTime());
        }
        else if(currentTime.getInGameDays() - timeOfLastUpdate.getInGameDays() > weekTime){
            timeOfLastUpdate.setCurrentTime(timeOfLastUpdate.getCurrentTime());
            if(data.getNumberOfAvailableLines() < data.getLimitOfLines()){
                data.incrementNumberOfAvailableLines();
                numberOfAddedLines++;
            }

            data.incrementNumberOfAvailableAirplanes();
            numberOfAddedAirplanes++;
        }

        for(Airplane a: data.getAirplanes()){
            a.update(deltaTime);
        }

        int temp = data.getTotalTransportedPassengers();
        for(Airport a: data.getAirports()){
            data.addTotalTransportedPassengers(a.update(deltaTime, data.stats));
        }

        boolean gameOver = false;
        for(Airport a: data.getAirports()){
            Time temp2 = a.howLongOverCrowded();
            if(temp2.getInGameDays() > data.getMaxOvercrowdedTime()){
                gameOver = true;
            }
        }

        if(timeOfLastAirportAdded.getCurrentTime() == 0){
            try {
                data.addAirport(getNextAirport());
                data.addAirport(getNextAirport());
                data.addAirport(getNextAirport());
                timeOfLastAirportAdded.setCurrentTime(currentTime.getCurrentTime());
            } catch (Exception e) {}
        }
        else if(currentTime.getInGameHours() - timeOfLastAirportAdded.getInGameHours() > 8){
            System.out.println("adding airport, time: " + currentTime.getInSeconds());
            try {
                data.addAirport(getNextAirport());
                timeOfLastAirportAdded.setCurrentTime(currentTime.getCurrentTime());
            }
            catch (Exception e) {}
        }

        Result result = new Result(data.getAirports(), data.getAirplanes(), data.getLines(), numberOfAddedLines, numberOfAddedAirplanes, gameOver, data.getTotalTransportedPassengers() - temp);
        return result;
    }
}
