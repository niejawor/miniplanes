package model;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class GameData {
    private final List<Airport> airports = new CopyOnWriteArrayList<>();
    private final List<Airplane> airplanes = new CopyOnWriteArrayList<>();
    private final List<Line> lines = new CopyOnWriteArrayList<>();
    private Supplier<Airport> airportSupplier;// = new AirportSupplier(AirportListGenerator.generateAirports(this), null);
    private final EnumIterator<Shape> shapeHandler = new EnumIterator<>(Shape.class);
    Updater updater;

    private final AtomicInteger numberOfAvailableLines;
    private final AtomicInteger numberOfAvailableAirplanes;
    private final int limitOfLines = 7;

    //TODO: delete
    private final int TARGET_TPS = 90;

    int maxOvercrowdedTime = TARGET_TPS*60*2;

    private final AtomicInteger totalTransportedPassengers;

    public GameData(){
        numberOfAvailableLines = new AtomicInteger(0);
        numberOfAvailableAirplanes = new AtomicInteger(0);
        totalTransportedPassengers = new AtomicInteger(0);
        //this.updater = updater;
        //airportSupplier = new AirportSupplier(AirportListGenerator.generateAirports(updater), null);
    }

    public void setUpdater(Updater updater) {
        this.updater = updater;
        airportSupplier = new AirportSupplier(AirportListGenerator.generateAirports(updater), null);
    }

    public List<Airport> getAirports(){
        return airports;
    }

    public List<Airplane> getAirplanes(){
        return airplanes;
    }

    public List<Line> getLines(){
        return lines;
    }

    Supplier<Airport> getAirportSupplier(){
        return airportSupplier;
    }

    EnumIterator<Shape> getShapeHandler(){
        return shapeHandler;
    }

    public int getNumberOfAvailableLines(){
        return numberOfAvailableLines.get();
    }

    public int getNumberOfAvailableAirplanes(){
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

    public void addLine(Line line, Airplane airplane){
        lines.add(line);
        airplanes.add(airplane);
    }

    public void decrementNumberOfAvailableAirplanes() {
        numberOfAvailableAirplanes.decrementAndGet();
    }
}
