package viewmodel;

import model.Airport;
import model.AirportType;
import model.Shape;

import java.util.ArrayList;
import java.util.List;

/**
 * Założenie: Płótno mapy ma proporcje 16:9.
 * (0.0f, 0.0f) = Lewy Górny Róg
 * (1.0f, 1.0f) = Prawy Dolny Róg
 */

public class AirportListGenerator {
    public static List<Airport> generate50SmallAirports() {
        List<Airport> airports = new ArrayList<>();

        airports.add(new Airport(Shape.getRandomShape(), new float[] {0.10f, 0.40f}, AirportType.SmallAirport)); // Dublin, IE
        airports.add(new Airport(Shape.getRandomShape(), new float[] {0.22f, 0.45f}, AirportType.SmallAirport)); // Londyn, UK
        airports.add(new Airport(Shape.getRandomShape(), new float[] {0.18f, 0.35f}, AirportType.SmallAirport)); // Edynburg, UK
        airports.add(new Airport(Shape.getRandomShape(), new float[] {0.12f, 0.38f}, AirportType.SmallAirport)); // Belfast, UK
        airports.add(new Airport(Shape.getRandomShape(), new float[] {0.20f, 0.42f}, AirportType.SmallAirport)); // Manchester, UK

        airports.add(new Airport(Shape.getRandomShape(), new float[] {0.22f, 0.73f}, AirportType.SmallAirport)); // Madryt, ES
        airports.add(new Airport(Shape.getRandomShape(), new float[] {0.15f, 0.75f}, AirportType.SmallAirport)); // Lizbona, PT
        airports.add(new Airport(Shape.getRandomShape(), new float[] {0.30f, 0.70f}, AirportType.SmallAirport)); // Barcelona, ES
        airports.add(new Airport(Shape.getRandomShape(), new float[] {0.18f, 0.81f}, AirportType.SmallAirport)); // Sewilla, ES
        airports.add(new Airport(Shape.getRandomShape(), new float[] {0.28f, 0.76f}, AirportType.SmallAirport)); // Walencja, ES

        airports.add(new Airport(Shape.getRandomShape(), new float[] {0.55f, 0.23f}, AirportType.SmallAirport)); // Sztokholm, SE
        airports.add(new Airport(Shape.getRandomShape(), new float[] {0.50f, 0.20f}, AirportType.SmallAirport)); // Oslo, NO
        airports.add(new Airport(Shape.getRandomShape(), new float[] {0.48f, 0.30f}, AirportType.SmallAirport)); // Kopenhaga, DK
        airports.add(new Airport(Shape.getRandomShape(), new float[] {0.62f, 0.18f}, AirportType.SmallAirport)); // Helsinki, FI
        airports.add(new Airport(Shape.getRandomShape(), new float[] {0.05f, 0.10f}, AirportType.SmallAirport)); // Reykjavik, IS

        airports.add(new Airport(Shape.getRandomShape(), new float[] {0.36f, 0.40f}, AirportType.SmallAirport)); // Amsterdam, NL
        airports.add(new Airport(Shape.getRandomShape(), new float[] {0.35f, 0.44f}, AirportType.SmallAirport)); // Bruksela, BE
        airports.add(new Airport(Shape.getRandomShape(), new float[] {0.48f, 0.40f}, AirportType.SmallAirport)); // Berlin, DE
        airports.add(new Airport(Shape.getRandomShape(), new float[] {0.40f, 0.48f}, AirportType.SmallAirport)); // Frankfurt, DE
        airports.add(new Airport(Shape.getRandomShape(), new float[] {0.44f, 0.52f}, AirportType.SmallAirport)); // Monachium, DE
        airports.add(new Airport(Shape.getRandomShape(), new float[] {0.41f, 0.37f}, AirportType.SmallAirport)); // Hamburg, DE

        airports.add(new Airport(Shape.getRandomShape(), new float[] {0.33f, 0.48f}, AirportType.SmallAirport)); // Paryż, FR
        airports.add(new Airport(Shape.getRandomShape(), new float[] {0.36f, 0.68f}, AirportType.SmallAirport)); // Marsylia, FR
        airports.add(new Airport(Shape.getRandomShape(), new float[] {0.34f, 0.58f}, AirportType.SmallAirport)); // Lyon, FR
        airports.add(new Airport(Shape.getRandomShape(), new float[] {0.28f, 0.60f}, AirportType.SmallAirport)); // Bordeaux, FR
        airports.add(new Airport(Shape.getRandomShape(), new float[] {0.38f, 0.70f}, AirportType.SmallAirport)); // Nicea, FR

        airports.add(new Airport(Shape.getRandomShape(), new float[] {0.51f, 0.70f}, AirportType.SmallAirport)); // Rzym, IT
        airports.add(new Airport(Shape.getRandomShape(), new float[] {0.47f, 0.60f}, AirportType.SmallAirport)); // Mediolan, IT
        airports.add(new Airport(Shape.getRandomShape(), new float[] {0.52f, 0.78f}, AirportType.SmallAirport)); // Neapol, IT
        airports.add(new Airport(Shape.getRandomShape(), new float[] {0.50f, 0.63f}, AirportType.SmallAirport)); // Wenecja, IT
        airports.add(new Airport(Shape.getRandomShape(), new float[] {0.49f, 0.68f}, AirportType.SmallAirport)); // Florencja, IT
        airports.add(new Airport(Shape.getRandomShape(), new float[] {0.50f, 0.83f}, AirportType.SmallAirport)); // Palermo, IT (Sycylia)

        airports.add(new Airport(Shape.getRandomShape(), new float[] {0.58f, 0.42f}, AirportType.SmallAirport)); // Warszawa, PL
        airports.add(new Airport(Shape.getRandomShape(), new float[] {0.50f, 0.48f}, AirportType.SmallAirport)); // Praga, CZ
        airports.add(new Airport(Shape.getRandomShape(), new float[] {0.52f, 0.53f}, AirportType.SmallAirport)); // Wiedeń, AT
        airports.add(new Airport(Shape.getRandomShape(), new float[] {0.55f, 0.55f}, AirportType.SmallAirport)); // Budapeszt, HU
        airports.add(new Airport(Shape.getRandomShape(), new float[] {0.65f, 0.60f}, AirportType.SmallAirport)); // Bukareszt, RO
        airports.add(new Airport(Shape.getRandomShape(), new float[] {0.62f, 0.65f}, AirportType.SmallAirport)); // Sofia, BG
        airports.add(new Airport(Shape.getRandomShape(), new float[] {0.58f, 0.60f}, AirportType.SmallAirport)); // Belgrad, RS
        airports.add(new Airport(Shape.getRandomShape(), new float[] {0.60f, 0.30f}, AirportType.SmallAirport)); // Ryga, LV
        airports.add(new Airport(Shape.getRandomShape(), new float[] {0.60f, 0.25f}, AirportType.SmallAirport)); // Tallinn, EE
        airports.add(new Airport(Shape.getRandomShape(), new float[] {0.63f, 0.35f}, AirportType.SmallAirport)); // Wilno, LT

        airports.add(new Airport(Shape.getRandomShape(), new float[] {0.60f, 0.80f}, AirportType.SmallAirport)); // Ateny, GR
        airports.add(new Airport(Shape.getRandomShape(), new float[] {0.73f, 0.71f}, AirportType.SmallAirport)); // Stambuł, TR
        airports.add(new Airport(Shape.getRandomShape(), new float[] {0.80f, 0.78f}, AirportType.SmallAirport)); // Ankara, TR
        airports.add(new Airport(Shape.getRandomShape(), new float[] {0.82f, 0.86f}, AirportType.SmallAirport)); // Nikozja, CY

        airports.add(new Airport(Shape.getRandomShape(), new float[] {0.85f, 0.20f}, AirportType.SmallAirport)); // Moskwa, RU
        airports.add(new Airport(Shape.getRandomShape(), new float[] {0.80f, 0.15f}, AirportType.SmallAirport)); // Petersburg, RU
        airports.add(new Airport(Shape.getRandomShape(), new float[] {0.75f, 0.45f}, AirportType.SmallAirport)); // Kijów, UA
        airports.add(new Airport(Shape.getRandomShape(), new float[] {0.70f, 0.38f}, AirportType.SmallAirport)); // Mińsk, BY

        return airports;
    }
}