/*
Prosty supplier wyrzucający (za pomocą get()) kolejne pojawiające się lotniska z prawdopodobieństwem
proporcjonalnym do kwadratu minimalnej odległości od już wyplutych lotnisk
W konstruktorze bierze kolekcję lotnisk, na której ma pracować i (opcjonalne) lotnisko startowe.
Opcjonalne tzn. null, jak null to wtedy losuje sobie pierwsze i zaczyna wypluwanie od niego.
Jak start!=null to wtedy nie wypluwa start, tylko wypluwa kolejne

 */

package model;

import java.util.*;
import java.util.function.Supplier;
import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;

public class AirportSupplier implements Supplier<Airport> {

    private final LinkedHashSet<Airport> used, unused;
    private final HashMap<Airport, Float> minDist; //HashMap, bo żadne dwa różne lotniska nie są semantycznie identyczne
    private final Random random = new Random();
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
            int idx = random.nextInt(unused.size());
            for (int i = 0; i < idx; ++i) {
                prevDel = unused.removeFirst();
                unused.addLast(prevDel);
            }
            initialize(prevDel = unused.removeFirst());
            return prevDel;
        }

        List<Pair<Airport, Double>> weightedAirports = new ArrayList<>();
        double totalWeight = 0.0;

        for (Airport airport : unused) {
            Float savedMinDist = minDist.get(airport);
            float currentMinDist = savedMinDist != null ? savedMinDist : Float.POSITIVE_INFINITY;
            float newDist = airport.distance(prevDel);
            if (newDist < currentMinDist) {
                minDist.put(airport, newDist);
                currentMinDist = newDist;
            }

            double weight = Math.pow(1/currentMinDist, 3d * Math.sqrt(6/(double)used.size()));
            weightedAirports.add(new Pair<>(airport, weight));
            totalWeight += weight;
        }

        Airport next = new EnumeratedDistribution<>(weightedAirports).sample();
        unused.remove(next);
        used.add(next);
        minDist.remove(next);
        return prevDel = next;
    }
}
