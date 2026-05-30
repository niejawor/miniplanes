package model;

public class Time {
    private long currentTime;
    public Time(long currentTime) {
        this.currentTime = currentTime;
    }

    public long getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(long currentTime) {
        this.currentTime = currentTime;
    }

    public void addTime(long currentTime) {
        this.currentTime += currentTime;
    }

    public long getInSeconds() {
        return currentTime / 1000_000_000;
    }

    // 1 dzien to 43.2 sekund, 1 godzina to 1.8 sekundy, 1 minuta to 0,03 sekundy
    public long  getInGameMinutes() {
        return (long)((double)getInSeconds() / (double)0.03);
    }

    public long  getInGameHours() {
        return (long)((double)getInSeconds() / (double)1.8);
    }

    public long getInGameDays() {
        return (long)((double)getInSeconds() / (double)43.2);
    }

    public void setInMinutes(long i) {
        long v = i * (long)(1000000000f * 0.03f);
        this.currentTime = v;
    }
}
