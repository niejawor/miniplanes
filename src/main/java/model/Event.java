package model;

public interface Event {

    void handle_event();

    // pryywatne klasy ktere implementuja ten interfejs odpowiedzialne za poszczegolny eventy

    public class AddLineEvent implements Event {
        GameEngine engine;
        int id_of_first_airport;
        int id_of_second_airport;

        AddLineEvent(int id_of_first_airport, int id_of_second_airport, GameEngine engine) {
            this.id_of_first_airport = id_of_first_airport;
            this.id_of_second_airport = id_of_second_airport;
            this.engine = engine;
        }

        @Override
        public void handle_event() {}
    }

    public class RemoveLineEvent implements Event {
        GameEngine engine;
        int id_of_line;

        RemoveLineEvent(int id_of_line, GameEngine engine) {
            this.id_of_line = id_of_line;
            this.engine = engine;
        }

        @Override
        public void handle_event() {}
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
        public void handle_event() {}
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
        public void handle_event() {}
    }

    public class AddAirplaneEvent implements Event {
        GameEngine engine;
        int line_id;

        AddAirplaneEvent(int line_id, GameEngine engine) {
            this.line_id = line_id;
            this.engine = engine;
        }

        @Override
        public void handle_event() {}
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
        public void handle_event() {}
    }
}
