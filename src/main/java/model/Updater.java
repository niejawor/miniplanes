package model;

import javafx.util.Pair;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class Updater {

    GameData data;

    final Time currentTime = new Time(0);

    private final long weekTime = 7;


    public Updater(GameData data) {
        this.data = data;
    }

    void generatePassenger(Airport airport) {
        airport.addPassenger(new Passenger(data.getShapeHandler().getRandomUsed(),airport,data, currentTime.getCurrentTime()));
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

        for(Airplane a: data.getAirplanes()){
            a.update(deltaTime);
        }

        int temp = data.getTotalTransportedPassengers();
        for(Airport a: data.getAirports()){
            data.addTotalTransportedPassengers(a.update(deltaTime, this, data));
        }

        boolean gameOver = false;
        for(Airport a: data.getAirports()){
            Time temp2 = a.howLongOverCrowded();
            if(temp2.getInGameDaysPrecise() >= data.getMaxOvercrowdedTime()){
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
        else if(currentTime.getInGameHours() - timeOfLastAirportAdded.getInGameHours() > 12){
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



    public HashMap<Integer, List<List<Integer>>> performPathFinder(HashMap<Pair<Integer, Integer>, Pair<Long, Integer>> stats, List<ArrayList<Integer>> lines, List<Integer> airports, HashMap<Integer, Shape> airportShapes){
        HashMap<Integer, List<Pair<Integer, Float>>> graph = new HashMap<>(); // airport -> index
        HashMap<Integer, List<List<Integer>>> result = new HashMap<>();

        for(Integer airport : airports){ // indeksy lotnisk
            graph.putIfAbsent(airport, new ArrayList<>());
        }

        HashMap<Pair<Integer, Integer>, Boolean> isInLines = new HashMap<>();

        for(ArrayList<Integer> linePath : lines){

            // co z pustymi ?
            Integer previousAirport = linePath.getFirst(); // puste!, potrzebujemy tylko indeksy kolejnych krawedzi
            for(Integer airport : linePath){
                if(Objects.equals(previousAirport, airport)){
                    continue;
                }

                isInLines.put(new Pair<>(previousAirport, airport), true);
                previousAirport = airport;
            }

            previousAirport = linePath.getLast(); // znowu pusty?
            for (Integer airport : linePath.reversed()) {
                if(Objects.equals(previousAirport, airport)){
                    continue;
                }

                isInLines.put(new Pair<>(previousAirport, airport), true);
                previousAirport = airport;
            }

        }


        // Dodaj jedna krawedz na polaczenie: statystyczna jesli sa dane, domyslna w przeciwnym razie
        float defaultEdgeWeight = 10_000f;
        for(Pair<Integer, Integer> lineEdge : isInLines.keySet()){
            Integer from = lineEdge.getKey();
            if(!graph.containsKey(from)) continue;

            Pair<Long, Integer> stat = stats.get(lineEdge);
            float weight;
            if(stat != null && stat.getValue() > 0){
                weight = (float)stat.getKey() / (float)stat.getValue();
            } else {
                weight = defaultEdgeWeight;
            }
            graph.get(from).add(new Pair<>(lineEdge.getValue(), weight));
        }

        for(Integer airport : airports){
            result.put(airport, DijkstraShortestPath(airport, graph, airports, airportShapes));
        }

        return result;
    }

    List<List<Integer>> DijkstraShortestPath(Integer start, HashMap<Integer, List<Pair<Integer, Float>>> graph, List<Integer> airports, HashMap<Integer, Shape> airportShapes){
        List<List<Integer>> result = new ArrayList<>();

        HashMap<Integer, Float> dist = new HashMap<>();
        HashMap<Integer, Integer> prev = new HashMap<>();

        for(Integer airport : airports){
            dist.put(airport, 1000_000_000_000f);
            prev.put(airport, airport);
        }

        dist.put(start, 0.0f);

        // pary lotnisko , odleglosc
        PriorityQueue<Pair<Integer,Float>> priorityQueue = new PriorityQueue<>(Comparator.comparingDouble(Pair::getValue));
        priorityQueue.add(new Pair<>(start,0.0f));


        while(!priorityQueue.isEmpty()){
            Pair<Integer,Float> pair = priorityQueue.poll();

            Float sugestedDistance = pair.getValue();
            Float distance = dist.get(pair.getKey());
            if(sugestedDistance > distance){
                continue;
            }

            for(Pair<Integer, Float> edge : graph.get(pair.getKey())){
                Float newDistance = dist.get(pair.getKey()) + edge.getValue();
                if(newDistance < dist.get(edge.getKey())){
                    dist.put(edge.getKey(), newDistance);
                    prev.put(edge.getKey(), pair.getKey());
                    priorityQueue.add(new Pair<>(edge.getKey(), newDistance));
                }
            }

        }

        Integer min;
        for(int i=0;i<Shape.values().length;i++){
            min = null;

            for(Map.Entry<Integer, Float> entry : dist.entrySet()){
                if(airportShapes.get(entry.getKey()) != Shape.values()[i]){
                    continue;
                }

                if(min == null){
                    min = entry.getKey();
                }
                else if(dist.get(min) > entry.getValue()){
                    min = entry.getKey();
                }

            }

            List<Integer> temp = new ArrayList<>();
            if(min == null) {
                temp.add(start);
                result.add(temp);
                continue;
            }

            Integer prevAirport = min;
            while(!Objects.equals(prev.get(prevAirport), start)){
                if(Objects.equals(prevAirport, prev.get(prevAirport))){
                    break;
                }
                prevAirport = prev.get(prevAirport);
            }
            temp.add(prevAirport);


            for(Map.Entry<Integer, Float> entry : dist.entrySet()){
                if(airportShapes.get(entry.getKey()) != Shape.values()[i]){
                    continue;
                }
                float entryDist = entry.getValue();
                boolean withinThreshold = dist.get(min) * 2.5 >= entryDist;
                boolean unreachable = entryDist >= 500_000_000_000f;
                if(withinThreshold && !unreachable){
                    temp.add(entry.getKey());
                    Integer firstHop = entry.getKey();
                    while(!Objects.equals(prev.get(firstHop), start)){
                        if(Objects.equals(firstHop, prev.get(firstHop))){
                            break;
                        }
                        firstHop = prev.get(firstHop);
                    }
                    temp.add(firstHop);
                } else if(unreachable){
                    temp.add(entry.getKey());
                }
            }

            result.add(temp);
        }

        return result;
    }
}
