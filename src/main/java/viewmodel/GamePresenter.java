package viewmodel;

import model.BasicGameData;

public class GamePresenter {
    private BasicGameData data;

    public GamePresenter() {
        this.data = new BasicGameData();
    }

    public String getTitle() { return data.getTitle(); }
    public float[] getColor() { return data.getSkyColor(); }
}
