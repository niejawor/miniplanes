package viewmodel;

import model.Airport;
import model.GameData;
import model.Line;
import model.Airplane;
import model.AirplaneType;
import model.Color;

import java.util.List;

public interface Event {

    //zwraca true jesli operacja sie powiodla
    public boolean handleEvent();

    // prywatne klasy ktere implementuja ten interfejs odpowiedzialne za poszczegolny eventy

    public final class AddLineEvent implements Event {
        GameData gameData;
        List<Airport> route;
        Color color;

        public AddLineEvent(GameData gameData, List<Airport> route, Color color) {
            this.gameData = gameData;
            this.route = route;
            this.color = color;
        }

        @Override
        public boolean handleEvent() {
            if (route == null || route.size() < 2) {
                return false;
            }
            if (!gameData.getPalette().contains(color) || !gameData.canAddLine(route)) {
                return false;
            }

            // Kazdy kolor to dokladnie jedna linia - nie tworzymy duplikatu.
            for (Line existing : gameData.getLines()) {
                if (existing.color == color) {
                    return false;
                }
            }

            Line newLine = new Line(route.get(0), route.get(1), color);

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
            if (lineId < 0 || lineId >= gameData.getLines().size()) return false;
            Line line = gameData.getLines().remove(lineId);
            for (Airplane airplane : gameData.getAirplanes()) {
                if (airplane.line == line) {
                    if (airplane.shouldReturnToPoolWhenRemoved()) {
                        gameData.addAvailableAirplane();
                    }
                    airplane.setInvalid();
                }
            }
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
            Airport before = airports.get(beforeAirport);
            Airport nowy = airports.get(airportToAdd);
            Airport after = airports.get(afterAirport);
            if (!gameData.canInsertAirport(line, before, nowy, after)) return false;
            boolean result = line.addAirportBetween(before, after, nowy);
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
        private final int edgeAirportId;   // lotnisko brzegowe (pierwsze lub ostatnie) linii
        private final int newAirportId;    // lotnisko, ktore dokladamy za brzegowym

        public EditLineAddToEdgeEvent(GameData gameData, int lineId, int edgeAirportId, int newAirportId){
            this.gameData = gameData;
            this.lineId = lineId;
            this.edgeAirportId = edgeAirportId;
            this.newAirportId = newAirportId;
        }

        @Override
        public boolean handleEvent(){
            var airports = gameData.getAirports();
            if (lineId < 0 || lineId >= gameData.getLines().size()) return false;
            Line line = gameData.getLines().get(lineId);
            Airport edgeAirport = airports.get(edgeAirportId);
            Airport newAirport = airports.get(newAirportId);
            if (!gameData.canAddAirportToEdge(line, edgeAirport, newAirport)) return false;
            boolean insertAtStart = edgeAirport == line.get(0);
            boolean result = line.addAirportToEdge(edgeAirport, newAirport);
            if (result && insertAtStart) {
                // Doklejenie na poczatku przesuwa wszystkie indeksy o 1 - lecace samoloty musza
                // przesunac swoj cel, aby nadal celowac w to samo lotnisko.
                for (Airplane airplane : gameData.getAirplanes()) {
                    if (airplane.line == line) {
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

    public final class AddAirplaneToLineEvent implements Event {
        GameData gameData;
        int lineId;
        int airportId;

        AddAirplaneToLineEvent(GameData gameData, int lineId, int airportId) {
            this.lineId = lineId;
            this.airportId = airportId;
            this.gameData = gameData;
        }

        @Override
        public boolean handleEvent() {
            if (lineId < 0 || lineId >= gameData.getLines().size()) return false;
            if (airportId < 0 || airportId >= gameData.getAirports().size()) return false;
            Line line = gameData.getLines().get(lineId);
            if (line.size() < 2) return false;
            Airport startAirport = gameData.getAirports().get(airportId);
            if (!line.contains(startAirport) || !gameData.consumeAvailableAirplane()) return false;

            // Nowy samolot pojawia sie na wybranym lotnisku linii i zglasza sie do ladowania,
            // dzieki czemu wlacza sie w normalny cykl postoj -> start -> lot.
            Airplane airplane = new Airplane(line, AirplaneType.SmallAirplane, startAirport, true);
            startAirport.airplaneReportsToLanding(airplane);
            return gameData.getAirplanes().add(airplane);
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
