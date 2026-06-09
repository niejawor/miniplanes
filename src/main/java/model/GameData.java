package model;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class GameData {
    private final List<Airport> airports = new ArrayList<>();
    private final List<Airplane> airplanes = new ArrayList<>();
    private final List<Line> lines = new ArrayList<>();
    private Supplier<Airport> airportSupplier;// = new AirportSupplier(AirportListGenerator.generateAirports(this), null);
    private final EnumIterator<Shape> shapeHandler = new EnumIterator<>(Shape.class);
    Updater updater;

    private final AtomicInteger numberOfAvailableLines;
    private final AtomicInteger numberOfAvailableAirplanes;
    private final int limitOfLines = 7;

    private static final int INITIAL_AVAILABLE_AIRPLANES = 3;
    private static final int INITIAL_UNLOCKED_LINE_COLORS = 3;
    private static final int MAX_LINES_BETWEEN_AIRPORTS = 3;

    // Pelna pula kolorow; kolejne kolory odblokowuja sie przez popup nagrody.
    private final List<Color> allLineColors = List.of(
            Color.Red, Color.Green, Color.Blue, Color.Yellow, Color.Orange, Color.Purple, Color.Cyan, Color.Black
    );
    private final List<Color> unlockedLineColors = new CopyOnWriteArrayList<>();


    int maxOvercrowdedTime = 2; //in days

    // klucz: krawedz z lotniska first do second, wartosc: first - suma czasow, sec - liczba pobranych danych zainicjalizowalbym na czas przelotu dystansu wprost
    private HashMap<Pair<Integer, Integer>, Pair<Integer,Integer>> stats = new HashMap<>();

    private HashMap<Integer, List<List<Integer>>> bestNextStop = new HashMap<>();

    private final AtomicInteger totalTransportedPassengers;

    public GameData(){
        numberOfAvailableLines = new AtomicInteger(0);
        numberOfAvailableAirplanes = new AtomicInteger(INITIAL_AVAILABLE_AIRPLANES);
        totalTransportedPassengers = new AtomicInteger(0);
        for (int i = 0; i < INITIAL_UNLOCKED_LINE_COLORS && i < allLineColors.size(); i++) {
            unlockedLineColors.add(allLineColors.get(i));
        }

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

    public HashMap<Pair<Integer, Integer>, Pair<Integer, Integer>> getStats() {
        return stats;
    }

    public void setBestNextStop(HashMap<Integer,List<List<Integer>>> bestNextStop) {
        this.bestNextStop.clear();
        this.bestNextStop.putAll(bestNextStop);
    }

    public HashMap<Integer, List<List<Integer>>> getBestNextStop() {
        return bestNextStop;
    }

    public List<Airplane> getAirplanes(){
        return airplanes;
    }

    public List<Line> getLines(){
        return lines;
    }

    public List<Color> getPalette(){
        return Collections.unmodifiableList(unlockedLineColors);
    }

    public boolean hasLockedLineColor() {
        return unlockedLineColors.size() < allLineColors.size();
    }

    public Color getNextLockedLineColor() {
        if (!hasLockedLineColor()) return null;
        return allLineColors.get(unlockedLineColors.size());
    }

    public Color unlockNextLineColor() {
        Color next = getNextLockedLineColor();
        if (next != null) unlockedLineColors.add(next);
        return next;
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

    public int getMaxOvercrowdedTime(){
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
        bestNextStop.put(airport.getIndex(), new ArrayList<>(8));
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

    public void addAvailableAirplane(){
        numberOfAvailableAirplanes.addAndGet(1);
    }

    public boolean consumeAvailableAirplane() {
        while (true) {
            int current = numberOfAvailableAirplanes.get();
            if (current <= 0) return false;
            if (numberOfAvailableAirplanes.compareAndSet(current, current - 1)) return true;
        }
    }

    public boolean canAddLine(List<Airport> route) {
        if (route == null || route.size() < 2) return false;
        for (int i = 0; i + 1 < route.size(); i++) {
            if (!canAddConnection(route.get(i), route.get(i + 1), null)) return false;
        }
        return true;
    }

    public boolean canInsertAirport(Line line, Airport before, Airport nowy, Airport after) {
        return canAddConnection(before, nowy, line) && canAddConnection(nowy, after, line);
    }

    public boolean canAddAirportToEdge(Line line, Airport edge, Airport nowy) {
        return canAddConnection(edge, nowy, line);
    }

    public boolean canAddConnection(Airport a, Airport b, Line ignoredLine) {
        if (a == null || b == null || a == b) return false;
        return countConnections(a, b, ignoredLine) < MAX_LINES_BETWEEN_AIRPORTS;
    }

    private int countConnections(Airport a, Airport b, Line ignoredLine) {
        int counter = 0;
        for (Line line : lines) {
            if (line == ignoredLine) continue;
            for (int i = 0; i + 1 < line.size(); i++) {
                Airport x = line.get(i);
                Airport y = line.get(i + 1);
                if ((x == a && y == b) || (x == b && y == a)) {
                    counter++;
                }
            }
        }
        return counter;
    }

    public void addLine(Line line, Airplane airplane){
        lines.add(line);
        airplanes.add(airplane);
    }

    public void decrementNumberOfAvailableAirplanes() {
        numberOfAvailableAirplanes.decrementAndGet();
    }
}
