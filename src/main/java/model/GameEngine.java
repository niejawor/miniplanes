package model;

import viewmodel.AirportListGenerator;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;


// vm uruchomi sobie na jednym watku simulate i to bedzie sobie na spokojnie chodzic, inforamcje o wszytkim bedzie mogla poobierac od gameengine za pomoca odpowiedniego gettera
public class GameEngine {
    private AtomicInteger current_tick;
    private final List<Airport> airports = new CopyOnWriteArrayList<>();
    private final List<Airplane> airplanes = new CopyOnWriteArrayList<>();
    private final List<Line> lines = new CopyOnWriteArrayList<>();
    private final Supplier<Airport> airportSupplier = new AirportSupplier(AirportListGenerator.generateAirports(), null);

    private final Queue<Event> events = new ConcurrentLinkedQueue<>();

    private AtomicInteger number_of_available_lines;
    private AtomicInteger number_of_available_airplanes;

    private final int limit_of_lines = 0;

    private final int update_time = 60*60*5; //co 5 minut

    private final int max_overcrowded_time = 60*60; //minuta


    private AtomicInteger total_transported_passengers;

    private AtomicBoolean isRunning;
    private final int TARGET_TPS = 90;
    private final int OPTIMAL_TIME = 1000000000 / TARGET_TPS; // w nano sekundach

    GameEngine() {
        current_tick = new AtomicInteger(0);
        total_transported_passengers = new AtomicInteger(0);
        number_of_available_lines = new AtomicInteger(3);
        number_of_available_airplanes = new AtomicInteger(3);
        isRunning = new AtomicBoolean(false);
    }

    Airport get_next_airport(){
        return airportSupplier.get();
    }

    public void add_event(Event e){
        events.add(e);
    } // za pomoca tego vm bedzie mogla dodawac eventy

    public int get_current_tick(){
        return current_tick.get();
    }

    public List<Airport> get_airports(){
        return airports;
    }

    public List<Airplane> get_airplanes(){
        return airplanes;
    }

    public List<Line> get_lines(){
        return lines;
    }

    public int get_total_transported_passengers(){
        return total_transported_passengers.get();
    }

    public int get_number_of_available_lines(){
        return number_of_available_lines.get();
    }

    public int get_number_of_available_airplanes(){
        return number_of_available_airplanes.get();
    }

    public boolean is_running(){
        return isRunning.get();
    }



    public void Simulate(){
        isRunning = new AtomicBoolean(true);

        while(true){
            long start_time = System.nanoTime();


            while(!events.isEmpty()){
                Event event = events.poll();
                event.handle_event();
            }
            final int get_tick = current_tick.get();


            if(get_tick % update_time == 0){
                if (number_of_available_lines.get() < limit_of_lines) {
                    number_of_available_lines.getAndIncrement();
                }
                number_of_available_airplanes.getAndIncrement();
            }

            //wysylam update do samolotow i lotnisk ile czasu minelo - w tickach

            for(Airplane a: airplanes){
                a.update((float)1/TARGET_TPS);
            }

            for(Airport a: airports){
                //total_transported_passengers.getAndAdd(a.update((float)1/TARGET_TPS));
                a.update((float)1/TARGET_TPS);
            }

            //sprawdza czy cos jest overcrowded - ewentualny koniec gry

            for(Airport a: airports){
                float temp = a.howLongOverCrowded();
                if(temp > max_overcrowded_time){
                    isRunning.set(false);
                    break;
                }
            }

            //dodanie nowych lotnisk jesli jest na to czas - poczatkowo co 2 minuty

            if(get_tick % 60*60*2 == 0){
                airports.add(get_next_airport());
            }

            if(System.nanoTime() - start_time < OPTIMAL_TIME){
                try{
                    long time_left = System.nanoTime() - start_time;
                    long millis = time_left / 1000000;
                    int nanos = (int) (time_left % 1000000);
                    Thread.sleep(millis, nanos);

                } catch (Exception e){}
            }

            //do kolejnego ticka
            current_tick.addAndGet(1);

        }
    }

}
