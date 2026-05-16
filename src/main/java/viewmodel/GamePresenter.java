package viewmodel;

import model.Airport;
import model.BasicGameData;
import java.util.List;

public class GamePresenter {
    private BasicGameData data;
    private List<Airport> airports;

    public GamePresenter() {
        this.data = new BasicGameData();
        this.airports = AirportListGenerator.generateAirports();
    }

    public String getTitle() { return data.getTitle(); }
    public float[] getColor() { return data.getSkyColor(); }
    public List<Airport> getAirports() { return airports; }
}