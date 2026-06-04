package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.io.File;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AirportListGenerator {
    public static List<Airport> generateAirports(Updater updater) {
        List<Airport> airports = new ArrayList<>();
        EnumIterator<Shape> shapeHandler = new EnumIterator<>(Shape.class);
        ObjectMapper mapper = new ObjectMapper();

        try {
            List<Map<String, Double>> data = mapper.readValue(new File("airports.json"),
                    mapper.getTypeFactory().constructCollectionType(List.class, Map.class));

            for (Map<String, Double> m : data) {
                airports.add(new Airport(
                        shapeHandler.getRandomValue(),
                        new Point(m.get("x").floatValue(), m.get("y").floatValue()),
                        AirportType.SmallAirport,
                        updater
                ));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return airports;
    }
}