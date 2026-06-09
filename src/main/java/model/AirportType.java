package model;

public enum AirportType {
    BigAirport(5,4, 2, 1, 3, 25f, 25f),
    SmallAirport(5,2, 1, 1,  2,60f, 60f);

    public final int capacity;
    public final int takeoffRunways;
    public final int landingRunways;
    public final int terminals;
    public final int passengerCapacity;
    public final float timeSpentLanding;
    public final float timeSpentTakingOff;

    AirportType(int passengerCapacity, int capacity, int takeoffRunways, int landingRunways, int terminals, float timeSpentLanding, float timeSpentTakingOff) {
        this.capacity = capacity;
        this.terminals = terminals;
        this.takeoffRunways = takeoffRunways;
        this.landingRunways = landingRunways;
        this.passengerCapacity = passengerCapacity;
        this.timeSpentLanding = timeSpentLanding;
        this.timeSpentTakingOff = timeSpentTakingOff;
    }
}
