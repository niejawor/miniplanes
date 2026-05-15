package model;
import java.util.function.*;
import java.util.*;

public final class Line {
    private final static Supplier<Color> nextColorGetter = new EnumIterator<>(Color.class);

    private final LinkedList<Airport> path = new LinkedList<>();
    private final HashSet<Airplane> planes = new HashSet<>();
    public final Color color;

    public Line(Airport a, Airport b){
        color = nextColorGetter.get();
        path.add(a);
        path.add(b);
    }

    public int size() {
        return path.size();
    }

    public Airport get(int idx) {
        return path.get(idx);
    }

    public void addAirportBetween(Airport a, Airport b, Airport nowy) {
        int counter = 0;
        for (Airport airport : path){
            if (airport == a || airport == b){
                path.add(counter + 1, nowy);
                return;
            }
            ++counter;
        }
        throw new NoSuchElementException("Nie znaleziono lotnisk");
    }

    public void addAirportToEdge(Airport brzegowe, Airport nowy) {
        if(brzegowe == path.getFirst()) path.addFirst(nowy);
        else if (brzegowe == path.getLast()) path.addLast(nowy);
        else throw new IllegalArgumentException("Podane (pierwsze) lotnisko nie jest brzegowe");
    }

    public boolean addAirplane(Airplane plane){
        return planes.add(plane);
    }

    public boolean delAirport(Airport airport){
        return path.remove(airport);
    }

}
