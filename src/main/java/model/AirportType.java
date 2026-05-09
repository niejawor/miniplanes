package model;

public enum AirportType {
    BigAirport(5,4, 2, 1),  //2 pasy startowe i jeden pas do lądowania oraz możliwość na 4 zaparkowane samoloty
    SmallAirport(5,2, 1, 1); //1 pas startowy i jeden pas do lądowania oraz możliwość na 2 zaparkowane samoloty

    final int capacity;
    final int takeoffRunways;
    final int landingRunways;
    final int passengerCapacity;

    AirportType(int passengerCapacity, int capacity, int takeoffRunways, int landingRunways) {
        this.capacity = capacity;
        this.takeoffRunways = takeoffRunways;
        this.landingRunways = landingRunways;
        this.passengerCapacity = passengerCapacity;
    }
}
