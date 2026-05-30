package model;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class Updater {

    GameData data;

    //TODO: delete
    private final int TARGET_TPS = 90;

    final AtomicInteger currentTime = new AtomicInteger(0);
    private final int updateTime = TARGET_TPS*60*5; //co 5 minut



    public Updater(GameData data) {
        this.data = data;
        currentTime.set(0);
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


    public Result update(int deltaTime){
        currentTime.addAndGet(deltaTime);

        int numberOfAddedLines = 0;
        int numberOfAddedAirplanes = 0;

        if(currentTime.get() % updateTime == 0){
            if(data.getNumberOfAvailableLines() < data.getLimitOfLines()){
                data.incrementNumberOfAvailableLines();
                numberOfAddedLines++;
            }

            data.incrementNumberOfAvailableAirplanes();
            numberOfAddedAirplanes++;
        }

        for(Airplane a: data.getAirplanes()){
            a.update((float)1/TARGET_TPS);
        }

        int temp = data.getTotalTransportedPassengers();
        for(Airport a: data.getAirports()){
            data.addTotalTransportedPassengers(a.update((float)1/TARGET_TPS));
        }

        boolean gameOver = false;
        for(Airport a: data.getAirports()){
            float temp2 = a.howLongOverCrowded();
            if(temp2 > data.getMaxOvercrowdedTime()){
                gameOver = true;
            }
        }

        if(currentTime.get() % TARGET_TPS*60*2 == 0){
            try {
                data.addAirport(getNextAirport());
            } catch (Exception e) {}
        }

        Result result = new Result(data.getAirports(), data.getAirplanes(), data.getLines(), numberOfAddedLines, numberOfAddedAirplanes, gameOver, data.getTotalTransportedPassengers() - temp);
        return result;
    }
}
