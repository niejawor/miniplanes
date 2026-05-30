package viewmodel;

import model.Airport;
import model.GameData;
import model.Line;
import model.Airplane;
import model.AirplaneType;

import java.util.List;

//TODO: move to VM
public interface Event {

    //zwraca true jesli operacja sie powiodla
    public boolean handleEvent();

    // prywatne klasy ktere implementuja ten interfejs odpowiedzialne za poszczegolny eventy

    public final class AddLineEvent implements Event {
        GameData gameData;
        List<Airport> route;

        public AddLineEvent(GameData gameData, List<Airport> route) {
            this.gameData = gameData;
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

            gameData.addLine(newLine, airplane);

            return true;
        }
    }

    public final class RemoveLineEvent implements Event {
        GameData gameData;
        int lineId;

        RemoveLineEvent(int lineId, GameData gameData) {
            this.lineId = lineId;
            this.gameData = gameData;
        }

        @Override
        public boolean handleEvent() {
            Line line = gameData.getLines().remove(lineId);
            for (Airplane airplane : gameData.getAirplanes())
                if (airplane.line == line) airplane.setInvalid();
            return true;
        }
    }

    public class EditLineAddEvent implements Event {
        GameData gameData;
        int airportToAdd;
        int beforeAirport;
        int afterAirport;
        int lineId;

        public EditLineAddEvent(int beforeAirport, int idOfAirportToAdd, int afterAirport, GameData gameData, int lineId) {
            this.beforeAirport = beforeAirport;
            this.afterAirport = afterAirport;
            this.airportToAdd = idOfAirportToAdd;
            this.gameData = gameData;
            this.lineId = lineId;
        }

        @Override
        public boolean handleEvent() {
            var airports = gameData.getAirports();
            Line line = gameData.getLines().get(lineId);
            int insertionIndex = -1;
            for (int i = 0; i < line.size(); i++) {
                if (line.get(i) == airports.get(beforeAirport)) {
                    insertionIndex = i + 1;
                    break;
                }
            }
            boolean result = line.addAirportBetween(airports.get(beforeAirport), airports.get(afterAirport), airports.get(airportToAdd));
            if (result && insertionIndex != -1) {
                for (Airplane airplane : gameData.getAirplanes()) {
                    if (airplane.line == line && airplane.idx >= insertionIndex) {
                        airplane.idx++;
                    }
                }
            }
            return result;
        }
    }

    public class EditLineAddToEdgeEvent implements Event {
        private final GameData gameData;
        private final int lineId;
        private final int airportId;
        private final int edgeAirportId;

        public EditLineAddToEdgeEvent(GameData gameData, int lineId, int edgeAirportId, int airportId){
            this.gameData = gameData;
            this.lineId = lineId;
            this.edgeAirportId = edgeAirportId;
            this.airportId = airportId;
        }

        @Override
        public boolean handleEvent(){
            var airports = gameData.getAirports();
            Line line = gameData.getLines().get(lineId);
            Airport edgeAirport = airports.get(edgeAirportId);
            boolean insertAtStart = edgeAirport == line.get(0);
            boolean result = line.addAirportToEdge(edgeAirport, airports.get(airportId));
            if (result && insertAtStart) {
                for (Airplane airplane : gameData.getAirplanes()) {
                    if (airplane.line == line && airplane.idx >= 0) {
                        airplane.idx++;
                    }
                }
            }
            return result;
        }
    }

    public class EditLineRemoveEvent implements Event {
        GameData gameData;
        int airportToRemove;
        int lineId;

        EditLineRemoveEvent(int airportToRemove, GameData gameData, int lineId) {
            this.airportToRemove = airportToRemove;
            this.gameData = gameData;
            this.lineId = lineId;
        }

        @Override
        public boolean handleEvent() {
            if (!gameData.getLines().get(lineId).delAirport(gameData.getAirports().get(airportToRemove))) return false;
            if (gameData.getLines().get(lineId).size() <= 1) {
                Event event = new RemoveLineEvent(lineId, gameData);
                event.handleEvent();
            }
            return true;
        }
    }

    public class AddAirplaneEvent implements Event {
        GameData gameData;
        int lineId;

        AddAirplaneEvent(int lineId, GameData gameData) {
            this.lineId = lineId;
            this.gameData = gameData;
        }

        @Override
        public boolean handleEvent() {
            List<Airplane> airplanes = gameData.getAirplanes();
            if (gameData.getNumberOfAvailableAirplanes() == 0) return false;
            gameData.decrementNumberOfAvailableAirplanes();
            return airplanes.add(new Airplane(gameData.getLines().get(lineId), AirplaneType.SmallAirplane));
        }
    }

    public class RemoveAirplaneEvent implements Event {
        GameData gameData;
        int lineId;
        Airplane airplaneToRemove;

        RemoveAirplaneEvent(int lineId, GameData gameData, Airplane airplaneToRemove) {
            this.lineId = lineId;
            this.gameData = gameData;
            this.airplaneToRemove = airplaneToRemove;
        }

        @Override
        public boolean handleEvent() {
            airplaneToRemove.setInvalid();
            return true;
        }
    }

}
