package model;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

// TODO: delete
// vm uruchomi sobie na jednym watku simulate i to bedzie sobie na spokojnie chodzic, inforamcje o wszytkim bedzie mogla poobierac od gameengine za pomoca odpowiedniego gettera
public class GameEngine {
    private final AtomicInteger currentTick;
    private final List<Airport> airports = new CopyOnWriteArrayList<>();
    private final List<Airplane> airplanes = new CopyOnWriteArrayList<>();
    private final List<Line> lines = new CopyOnWriteArrayList<>();
    //private final Supplier<Airport> airportSupplier = new AirportSupplier(AirportListGenerator.generateAirports(this), null);
    private final EnumIterator<Shape> shapeHandler = new EnumIterator<>(Shape.class);

    private final Queue<Event> events = new ConcurrentLinkedQueue<>();

    private final AtomicInteger numberOfAvailableLines;
    private final AtomicInteger numberOfAvailableAirplanes;


    private final AtomicInteger totalTransportedPassengers;

    private AtomicBoolean isRunning;
    private final AtomicBoolean gameOver;

    private final int TARGET_TPS = 90;
    private final int OPTIMAL_TIME = 1000000000 / TARGET_TPS; // w nano sekundach


    private final int updateTime = TARGET_TPS*60*5; //co 5 minut
    private final int maxOvercrowdedTime = TARGET_TPS*60; //minuta

    public GameEngine() {
        currentTick = new AtomicInteger(0);
        totalTransportedPassengers = new AtomicInteger(0);
        numberOfAvailableLines = new AtomicInteger(5); // 3
        numberOfAvailableAirplanes = new AtomicInteger(5); // 3
        isRunning = new AtomicBoolean(true);
        gameOver = new AtomicBoolean(false);
    }

//    Airport getNextAirport(){
//        Airport next =  airportSupplier.get();
//        shapeHandler.updateUse(next.getShape());
//        return next;
//    }


    public void addEvent(Event e){
        events.add(e);
    } // za pomoca tego vm bedzie mogla dodawac eventy

    public int getCurrentTick(){
        return currentTick.get();
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

    public void addLine(Line line, Airplane airplane){
        lines.add(line);
        airplanes.add(airplane);
    }

    public int getTotalTransportedPassengers(){
        return totalTransportedPassengers.get();
    }

    public int getNumberOfAvailableLines(){
        return numberOfAvailableLines.get();
    }

    public int getNumberOfAvailableAirplanes(){
        return numberOfAvailableAirplanes.get();
    }

    public boolean isRunning(){
        return isRunning.get();
    }
    
    public void pause(){
        isRunning.set(false);
    }
    public void resume(){
        isRunning.set(true);
    }
    public void gameOver(){
        gameOver.set(true);
    }

    //public void generatePassenger(Airport airport){}

    public void decrementNumberOfAvailableLines(){
        numberOfAvailableLines.decrementAndGet();
    }

    public void decrementNumberOfAvailableAirplanes(){
        numberOfAvailableAirplanes.decrementAndGet();
    }


    public void Simulate(){
        isRunning = new AtomicBoolean(true);
        //gameOver.set(false);
        while(true){
            if(gameOver.get()){
                break;
            }

            long startTime = System.nanoTime();


            if(!isRunning.get()){
                if(System.nanoTime() - startTime < OPTIMAL_TIME){
                    try{
                        long time_left = OPTIMAL_TIME - (System.nanoTime() - startTime);
                        long millis = time_left / 1000000;
                        int nanos = (int) (time_left % 1000000);
                        Thread.sleep(millis, nanos);

                    } catch (Exception e){}
                }

                currentTick.getAndIncrement();
                continue;
            }


            while(!events.isEmpty()){
                Event event = events.poll();
                event.handleEvent();
            }
            final int getTick = currentTick.get();




            //wysylam update do samolotow i lotnisk ile czasu minelo - w tickach



            //sprawdza czy cos jest overcrowded - ewentualny koniec gry



            //dodanie nowych lotnisk jesli jest na to czas - poczatkowo co 2 minuty

//            if(getTick % TARGET_TPS*60*2 == 0){
//                try {
//                    airports.add(getNextAirport());
//                } catch (Exception e) {
//
//                }
//            }

            if(System.nanoTime() - startTime < OPTIMAL_TIME){
                try{
                    long time_left = OPTIMAL_TIME - (System.nanoTime() - startTime);
                    long millis = time_left / 1000000;
                    int nanos = (int) (time_left % 1000000);
                    Thread.sleep(millis, nanos);

                } catch (Exception e){}
            }

            //do kolejnego ticka
            currentTick.incrementAndGet();

        }
    }


    AtomicInteger nPassengers = new AtomicInteger(0);
    public int getResult() {
        // TODO
        return nPassengers.get();
    }

    public Weekdays getDay() {
        // TODO
        return Weekdays.MON;
    }

    public int getMinutes() {
        // TODO
        return 18 * 60 + 49;
    }
}
