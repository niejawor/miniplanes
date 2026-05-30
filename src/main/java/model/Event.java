package model;

import java.util.List;

//TODO: move to VM
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

            engine.addLine(newLine, airplane);

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
        int airportToAdd;
        int beforeAirport;
        int afterAirport;
        int lineId;

        public EditLineAddEvent(int beforeAirport, int idOfAirportToAdd, int afterAirport, GameEngine engine, int lineId) {
            this.beforeAirport = beforeAirport;
            this.afterAirport = afterAirport;
            this.airportToAdd = idOfAirportToAdd;
            this.engine = engine;
            this.lineId = lineId;
        }

        @Override
        public boolean handleEvent() {
            var airports = engine.getAirports();
            Line line = engine.getLines().get(lineId);
            int insertionIndex = -1;
            for (int i = 0; i < line.size(); i++) {
                if (line.get(i) == airports.get(beforeAirport)) {
                    insertionIndex = i + 1;
                    break;
                }
            }
            boolean result = line.addAirportBetween(airports.get(beforeAirport), airports.get(afterAirport), airports.get(airportToAdd));
            if (result && insertionIndex != -1) {
                for (Airplane airplane : engine.getAirplanes()) {
                    if (airplane.line == line && airplane.idx >= insertionIndex) {
                        airplane.idx++;
                    }
                }
            }
            return result;
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
            Line line = engine.getLines().get(lineId);
            Airport edgeAirport = airports.get(edgeAirportId);
            boolean insertAtStart = edgeAirport == line.get(0);
            boolean result = line.addAirportToEdge(edgeAirport, airports.get(airportId));
            if (result && insertAtStart) {
                for (Airplane airplane : engine.getAirplanes()) {
                    if (airplane.line == line && airplane.idx >= 0) {
                        airplane.idx++;
                    }
                }
            }
            return result;
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
            if (engine.getLines().get(lineId).size() <= 1) engine.addEvent(new RemoveLineEvent(lineId, engine));
            return true;
        }
    }

    public class AddAirplaneEvent implements Event {
        GameEngine engine;
        int lineId;

        AddAirplaneEvent(int lineId, GameEngine engine) {
            this.lineId = lineId;
            this.engine = engine;
        }

        @Override
        public boolean handleEvent() {
            List<Airplane> airplanes = engine.getAirplanes();
            if (engine.getNumberOfAvailableAirplanes() == 0) return false;
            engine.decrementNumberOfAvailableAirplanes();
            return airplanes.add(new Airplane(engine.getLines().get(lineId), AirplaneType.SmallAirplane));
        }
    }

    public class RemoveAirplaneEvent implements Event {
        GameEngine engine;
        int lineId;
        Airplane airplaneToRemove;

        RemoveAirplaneEvent(int lineId, GameEngine engine, Airplane airplaneToRemove) {
            this.lineId = lineId;
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
