/*
Prosty supplier wyrzucający (za pomocą get()) kolejne pojawiające się lotniska przy kryterium minimalnej odległości od już widocznych lotnisk
W konstruktorze bierze kolekcję lotnisk, na której ma pracować i lotnisko startowe (jego nie będzie wyrzucał, zacznie od najbliższego jemu)
 */

package viewmodel;

import model.Airport;

import java.util.*;
import java.util.function.Supplier;

public class AirportSupplier implements Supplier<Airport> {
    private final LinkedHashSet<Airport> used, unused;
    private final HashMap<Airport, Float> minDist; //HashMap, bo żadne dwa różne lotniska nie są semantycznie identyczne
    private Airport prevDel;

    AirportSupplier(Collection<Airport> collection, Airport start){
        prevDel = start;
        float temp;
        unused = new LinkedHashSet<>(collection);
        used = new LinkedHashSet<>();
        minDist = new HashMap<>();
        used.add(start);
        unused.remove(start);
        for (Airport airport : unused) {
            temp = airport.distance(start);
            minDist.put(airport, temp);
        }
    }

    @Override
    public Airport get(){
        if (unused.isEmpty()) throw new IllegalStateException("Wszystkie lotniska wyczerpane");
        Airport next = null;
        float currentMinDist, newDist, best = -1f;
        for (Airport airport : unused) {
            currentMinDist = minDist.get(airport);
            newDist = airport.distance(prevDel);
            if (newDist < currentMinDist) {
                minDist.put(airport, newDist);
                currentMinDist = newDist;
            }
            if (best == -1f || currentMinDist < best){
                best = currentMinDist;
                next =  airport;
            }
        }
        unused.remove(next);
        used.add(next);
        minDist.remove(next);
        return prevDel = next;
    }

}
