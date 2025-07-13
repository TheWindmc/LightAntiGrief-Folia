package me.statuxia.lightantigrief.trigger;

import lombok.Getter;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Trigger {

    private static final long RESET_TIME_MS = 1000L * 60 * 20; // 20 минут

    @Getter
    private final BufferTrigger buffer;
    private final AtomicInteger totalTriggered;
    private final AtomicLong lastTriggered;

    public Trigger(BufferTrigger buffer) {
        this.buffer = buffer;
        this.totalTriggered = new AtomicInteger(1);
        this.lastTriggered = new AtomicLong(System.currentTimeMillis());
    }

    private Trigger(BufferTrigger buffer, int initialValue, long initialTime) {
        this.buffer = buffer;
        this.totalTriggered = new AtomicInteger(initialValue);
        this.lastTriggered = new AtomicLong(initialTime);
    }

    public int getTotalTriggered() {
        return totalTriggered.get();
    }

//    public long getLastTriggered() {
//        return lastTriggered.get();
//    }

    public Trigger incrementTriggers() {
        return incrementTriggers(1);
    }

    public synchronized Trigger incrementTriggers(int price) {
        long currentTime = System.currentTimeMillis();
        long lastTime = lastTriggered.get();

        if (lastTime + RESET_TIME_MS < currentTime) {
            return new Trigger(buffer, price, currentTime);
        }

        totalTriggered.addAndGet(price);
        lastTriggered.set(currentTime);
        return this;
    }

    public boolean isExpired() {
        return lastTriggered.get() + RESET_TIME_MS < System.currentTimeMillis();
    }

//    public long getTimeUntilReset() {
//        long timePassed = System.currentTimeMillis() - lastTriggered.get();
//        long timeLeft = RESET_TIME_MS - timePassed;
//        return Math.max(0, timeLeft);
//    }
}