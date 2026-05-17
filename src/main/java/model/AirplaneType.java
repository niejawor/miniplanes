package model;

public enum AirplaneType {
    BigAirplane(8, 0.005f),
    SmallAirplane(4, 0.01f);

    final int capacity;
    final float speed;
    AirplaneType(int capacity, float speed) {
        this.capacity = capacity;
        this.speed = speed;
    }
}
