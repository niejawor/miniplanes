package model;

public enum AirplaneType {
    BigAirplane(8, 0.000_000_000_0_2f),
    SmallAirplane(5, 0.000_000_000_0_3f);

    final int capacity;
    final float speed;
    AirplaneType(int capacity, float speed) {
        this.capacity = capacity;
        this.speed = speed;
    }
}
