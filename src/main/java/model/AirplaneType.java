package model;

public enum AirplaneType {
    BigAirplane(8, 20),
    SmallAirplane(4, 10);

    final int capacity;
    final float speed;
    AirplaneType(int capacity, float speed) {
        this.capacity = capacity;
        this.speed = speed;
    }
}
