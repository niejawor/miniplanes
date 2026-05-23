package model;

import java.util.List;

public interface Event {

    //zwraca true jesli operacja sie powiodla
    public boolean handleEvent();

    // prywatne klasy ktere implementuja ten interfejs odpowiedzialne za poszczegolny eventy

    public final class AddLineEvent implements Event {
        GameEngine engine;
        List<Airport> route;

        public AddLineEvent(GameEngine engine, List<Airport> route) {
            this.engine = engine;
            this.route = route;
        }

        @Override
        public boolean handleEvent() {
//            if(engine.get_number_of_available_lines() == 0 || engine.get_number_of_available_airplanes() == 0){
//                return false;
//            }
//            engine.decrement_number_of_available_lines();
//            engine.decrement_number_of_available_airplanes();

            Line newLine = new Line(route.get(0), route.get(1));

            for (int i = 2; i < route.size(); i++) {
                newLine.addAirportToEdge(route.get(i - 1), route.get(i));
            }

            Airplane airplane = new Airplane(newLine, AirplaneType.SmallAirplane);
            newLine.get(0).airplaneReportsToLanding(airplane);

            engine.add_line(newLine, airplane);

            return true;
        }
    }

    public final class RemoveLineEvent implements Event {
        GameEngine engine;
        int lineId;

        RemoveLineEvent(int lineId, GameEngine engine) {
            this.lineId = lineId;
            this.engine = engine;
        }

        @Override
        public boolean handleEvent() {
            Line line = engine.getLines().remove(lineId);
            for (Airplane airplane : engine.getAirplanes())
                if (airplane.line == line) airplane.setInvalid();
            return true;
        }
    }

    public class EditLineAddEvent implements Event {
        GameEngine engine;
        int airport_to_add;
        int before_airport;
        int after_airport;
        int lineId;

        EditLineAddEvent(int before_airport, int id_of_airport_to_add, int after_airport,  GameEngine engine, int lineId) {
            this.before_airport = before_airport;
            this.after_airport = after_airport;
            this.airport_to_add = id_of_airport_to_add;
            this.engine = engine;
            this.lineId = lineId;
        }

        @Override
        public boolean handleEvent() {
            var airports = engine.getAirports();
            return engine.getLines().get(lineId).addAirportBetween(airports.get(before_airport), airports.get(after_airport), airports.get(airport_to_add));
        }
    }

    public class EditLineAddToEdgeEvent implements Event{
        private final GameEngine engine;
        private final int lineId;
        private final int airportId;
        private final int edgeAirportId;

        public EditLineAddToEdgeEvent(GameEngine engine, int lineId, int edgeAirportId, int airportId){
            this.engine = engine;
            this.lineId = lineId;
            this.edgeAirportId = edgeAirportId;
            this.airportId = airportId;
        }

        @Override
        public boolean handleEvent(){
            var airports = engine.getAirports();
            return engine.getLines().get(lineId).addAirportToEdge(airports.get(edgeAirportId), airports.get(airportId));
        }
    }

    public class EditLineRemoveEvent implements Event {
        GameEngine engine;
        int airportToRemove;
        int lineId;

        EditLineRemoveEvent(int airportToRemove, GameEngine engine, int lineId) {
            this.airportToRemove = airportToRemove;
            this.engine = engine;
            this.lineId = lineId;
        }

        @Override
        public boolean handleEvent() {
            if (!engine.getLines().get(lineId).delAirport(engine.getAirports().get(airportToRemove))) return false;
            if (engine.getLines().get(lineId).size() <= 1) engine.add_event(new RemoveLineEvent(lineId, engine));
            return true;
        }
    }

    public class AddAirplaneEvent implements Event {
        GameEngine engine;
        int line_id;

        AddAirplaneEvent(int line_id, GameEngine engine) {
            this.line_id = line_id;
            this.engine = engine;
        }

        @Override
        public boolean handleEvent() {
            List<Airplane> airplanes = engine.getAirplanes();
            if (engine.getNumberOfAvailableAirplanes() == 0) return false;
            engine.decrementNumberOfAvailableAirplanes();
            return airplanes.add(new Airplane(engine.getLines().get(line_id), AirplaneType.SmallAirplane));
        }
    }

    public class RemoveAirplaneEvent implements Event {
        GameEngine engine;
        int line_id;
        Airplane airplaneToRemove;

        RemoveAirplaneEvent(int line_id, GameEngine engine, Airplane airplaneToRemove) {
            this.line_id = line_id;
            this.engine = engine;
            this.airplaneToRemove = airplaneToRemove;
        }

        @Override
        public boolean handleEvent() {
            airplaneToRemove.setInvalid();
            return true;
        }
    }

}
