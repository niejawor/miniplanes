package model;

import java.util.ArrayList;
import java.util.List;

public class AirportListGenerator {
    public static List<Airport> generateAirports() {
        List<Airport> airports = new ArrayList<>();
        EnumIterator<Shape> shapeHandler = new EnumIterator<>(Shape.class);
        
        /*
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.090f, 0.200f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.180f, 0.090f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.200f, 0.240f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.040f, 0.720f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.070f, 0.880f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.210f, 0.560f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.440f, 0.610f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.420f, 0.800f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.520f, 0.890f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.540f, 0.590f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.580f, 0.510f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.610f, 0.630f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.680f, 0.710f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.710f, 0.890f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.820f, 0.830f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.640f, 0.230f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.670f, 0.280f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.740f, 0.300f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.820f, 0.420f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.790f, 0.050f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.120f, 0.780f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.200f, 0.320f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.270f, 0.450f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.320f, 0.580f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.440f, 0.360f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.450f, 0.470f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.460f, 0.170f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.510f, 0.080f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.480f, 0.720f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.510f, 0.460f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.590f, 0.350f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.710f, 0.600f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.820f, 0.560f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.950f, 0.250f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.960f, 0.450f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.350f, 0.380f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.380f, 0.540f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.210f, 0.740f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.490f, 0.280f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.570f, 0.240f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.530f, 0.380f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.660f, 0.390f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.640f, 0.460f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.690f, 0.520f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.745f, 0.530f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.750f, 0.700f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.650f, 0.780f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.710f, 0.150f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.810f, 0.250f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.880f, 0.340f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.910f, 0.070f), AirportType.SmallAirport));
        */

        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.090f, 0.200f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.150f, 0.100f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.160f, 0.230f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.040f, 0.720f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.070f, 0.880f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.210f, 0.560f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.440f, 0.610f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.420f, 0.800f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.520f, 0.890f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.540f, 0.590f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.580f, 0.510f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.610f, 0.630f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.680f, 0.710f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.710f, 0.890f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.790f, 0.810f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.640f, 0.230f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.740f, 0.300f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.820f, 0.420f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.790f, 0.050f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.120f, 0.780f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.200f, 0.320f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.270f, 0.450f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.320f, 0.580f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.440f, 0.360f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.450f, 0.470f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.460f, 0.170f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.550f, 0.080f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.480f, 0.720f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.510f, 0.460f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.590f, 0.350f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.710f, 0.600f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.820f, 0.560f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.950f, 0.250f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.960f, 0.450f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.350f, 0.350f), AirportType.SmallAirport));
        airports.add(new Airport(shapeHandler.getRandomValue(), new Point(0.210f, 0.770f), AirportType.SmallAirport));


        return airports;
    }
}