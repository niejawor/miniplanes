package model;
import java.util.function.*;
import java.util.*;

public final class Line {
    private final static Supplier<Color> nextColorGetter = new EnumIterator<>(Color.class);

    private final LinkedList<Airport> path = new LinkedList<>();
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
        if (idx < 0) return path.getFirst();
        else if (idx >= path.size()) return path.getLast();
        return path.get(idx);
    }

    public LinkedList<Airport> getPath(){
        return path;
    }

    public boolean addAirportBetween(Airport a, Airport b, Airport nowy) {
        int counter = 0;
        for (Airport airport : path){
            if (airport == a || airport == b){
                path.add(counter + 1, nowy);
                return true;
            }
            ++counter;
        }
        return false;
    }

    public boolean addAirportToEdge(Airport brzegowe, Airport nowy) {
        if(brzegowe == path.getFirst()) { path.addFirst(nowy); return true; }
        else if (brzegowe == path.getLast()) { path.addLast(nowy); return true; }
        return false;
    }

    public boolean delAirport(Airport airport){
        return path.remove(airport);
    }

}
