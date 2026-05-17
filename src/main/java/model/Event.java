package model;

import java.util.List;

public interface Event {

    //zwraca true jesli operacja sie powiodla
    public boolean handle_event();

    // prywatne klasy ktere implementuja ten interfejs odpowiedzialne za poszczegolny eventy

    public class AddLineEvent implements Event {
        GameEngine engine;
        List<Airport> route;

        public AddLineEvent(GameEngine engine, List<Airport> route) {
            this.engine = engine;
            this.route = route;
        }

        @Override
        public boolean handle_event() {
            if(engine.get_number_of_available_lines() == 0 || engine.get_number_of_available_airplanes() == 0){
                return false;
            }
            engine.decrement_number_of_available_lines();
            engine.decrement_number_of_available_airplanes();

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

    public class RemoveLineEvent implements Event {
        GameEngine engine;
        int id_of_line;

        RemoveLineEvent(int id_of_line, GameEngine engine) {
            this.id_of_line = id_of_line;
            this.engine = engine;
        }

        @Override
        public boolean handle_event() {
            return true;
        }
    }

    public class EditLineAddEvent implements Event {
        GameEngine engine;
        int airport_to_add;
        int before_airport;
        int after_airport;
        int line_id;

        EditLineAddEvent(int before_airport, int id_of_airport_to_add, int after_airport,  GameEngine engine, int line_id) {
            this.before_airport = before_airport;
            this.after_airport = after_airport;
            this.airport_to_add = id_of_airport_to_add;
            this.engine = engine;
            this.line_id = line_id;
        }

        @Override
        public boolean handle_event() {
            return true;
        }
    }

    public class EditLineRemoveEvent implements Event {
        GameEngine engine;
        int airport_to_remove;
        int line_id;

        EditLineRemoveEvent(int airport_to_remove, GameEngine engine,  int line_id) {
            this.airport_to_remove = airport_to_remove;
            this.engine = engine;
            this.line_id = line_id;
        }

        @Override
        public boolean handle_event() {
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
        public boolean handle_event() {
            return true;
        }
    }

    public class RemoveAirplaneEvent implements Event {
        GameEngine engine;
        int line_id;
        int airport_to_remove;

        RemoveAirplaneEvent(int line_id, GameEngine engine, int airport_to_remove) {
            this.line_id = line_id;
            this.engine = engine;
            this.airport_to_remove = airport_to_remove;
        }

        @Override
        public boolean handle_event() {
            return true;
        }
    }
}
