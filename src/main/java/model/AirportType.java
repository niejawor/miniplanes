package model;

public enum AirportType {
    BigAirport(5,4, 2, 1, 3, 5f, 5f),  //2 pasy startowe i jeden pas do lądowania oraz możliwość na 4 zaparkowane samoloty
    SmallAirport(5,2, 1, 1,  2,3f, 3f); //1 pas startowy i jeden pas do lądowania oraz możliwość na 2 zaparkowane samoloty

    final int capacity;
    final int takeoffRunways;
    final int landingRunways;
    final int terminals;
    final int passengerCapacity;
    final float timeSpentLanding;
    final float timeSpentTakingOff;

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
