/*
Prosty supplier wyrzucający (za pomocą get()) kolejne pojawiające się lotniska przy naiwnym kryterium
minimalnej odległości od już wyplutych lotnisk
W konstruktorze bierze kolekcję lotnisk, na której ma pracować i (opcjonalne) lotnisko startowe.
Opcjonalne tzn. null, jak null to wtedy losuje sobie pierwsze i zaczyna wypluwanie od niego.
Jak start!=null to wtedy nie wypluwa start, tylko wypluwa kolejne

 */

package model;

import java.util.*;
import java.util.function.Supplier;

public class AirportSupplier implements Supplier<Airport> {
    private final LinkedHashSet<Airport> used, unused;
    private final HashMap<Airport, Float> minDist; //HashMap, bo żadne dwa różne lotniska nie są semantycznie identyczne
    private Airport prevDel;

    private void initialize(Airport start){
        float temp;
        used.add(start);
        for (Airport airport : unused) {
            temp = airport.distance(start);
            minDist.put(airport, temp);
        }
    }

    public AirportSupplier(Collection<Airport> collection, Airport start){
        prevDel = start;
        unused = new LinkedHashSet<>(collection.stream().filter(Objects::nonNull).toList());
        used = new LinkedHashSet<>();
        minDist = new HashMap<>();
        if(start != null) {
            unused.remove(start);
            initialize(start);
        }
    }


    @Override
    public Airport get(){
        if (unused.isEmpty()) throw new IllegalStateException("Wszystkie lotniska wyczerpane");

        if (prevDel == null){
            initialize(prevDel = unused.removeFirst());
            return prevDel;
        }

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
