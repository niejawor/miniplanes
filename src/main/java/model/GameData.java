package model;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class GameData {
    private final List<Airport> airports = new CopyOnWriteArrayList<>();
    private final List<Airplane> airplanes = new CopyOnWriteArrayList<>();
    private final List<Line> lines = new CopyOnWriteArrayList<>();
    private final Supplier<Airport> airportSupplier;// = new AirportSupplier(AirportListGenerator.generateAirports(this), null);
    private final EnumIterator<Shape> shapeHandler = new EnumIterator<>(Shape.class);
    Updater updater;

    private final AtomicInteger numberOfAvailableLines;
    private final AtomicInteger numberOfAvailableAirplanes;
    private final int limitOfLines = 7;

    //TODO: delete
    private final int TARGET_TPS = 90;

    int maxOvercrowdedTime = TARGET_TPS*60*2;

    private final AtomicInteger totalTransportedPassengers;

    GameData(Updater updater){
        numberOfAvailableLines = new AtomicInteger(0);
        numberOfAvailableAirplanes = new AtomicInteger(0);
        totalTransportedPassengers = new AtomicInteger(0);
        this.updater = updater;
        airportSupplier = new AirportSupplier(AirportListGenerator.generateAirports(updater), null);
    }

    List<Airport> getAirports(){
        return airports;
    }

    List<Airplane> getAirplanes(){
        return airplanes;
    }

    List<Line> getLines(){
        return lines;
    }

    Supplier<Airport> getAirportSupplier(){
        return airportSupplier;
    }

    EnumIterator<Shape> getShapeHandler(){
        return shapeHandler;
    }

    int getNumberOfAvailableLines(){
        return numberOfAvailableLines.get();
    }

    int getNumberOfAvailableAirplanes(){
        return numberOfAvailableAirplanes.get();
    }

    int getMaxOvercrowdedTime(){
        return maxOvercrowdedTime;
    }

    int getTotalTransportedPassengers(){
        return totalTransportedPassengers.get();
    }

    void addTotalTransportedPassengers(int passengers){
        totalTransportedPassengers.addAndGet(passengers);
    }

    void addAirport(Airport airport){
        airports.add(airport);
    }

    int getLimitOfLines(){
        return limitOfLines;
    }

    void incrementNumberOfAvailableLines(){
        numberOfAvailableLines.addAndGet(1);
    }

    void incrementNumberOfAvailableAirplanes(){
        numberOfAvailableAirplanes.addAndGet(1);
    }
}
