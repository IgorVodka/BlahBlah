package com.company;

public class Clock {
    private long lastUpdate;

    public Clock() {
        this.lastUpdate = 0;
    }

    public void update() {
        this.lastUpdate = System.currentTimeMillis();
    }

    public boolean hasExpired() {
        long time = System.currentTimeMillis();
        long EXPIRATION_TIME = 30000;
        return (time - this.lastUpdate) > EXPIRATION_TIME;
    }
}
