package com.shally.urlshortener.utils;

public class SnowflakeGenerator {

    private final long machineId;
    private final long epoch = 1700000000000L;

    private long lastTimestamp = -1L;
    private long sequence = 0L;

    public SnowflakeGenerator(long machineId) {
        this.machineId = machineId;
    }

    public synchronized long nextId() {

        long timestamp = System.currentTimeMillis();

        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & 4095;
            if (sequence == 0) {
                while (timestamp <= lastTimestamp) {
                    timestamp = System.currentTimeMillis();
                }
            }
        } else {
            sequence = 0;
        }

        lastTimestamp = timestamp;

        return ((timestamp - epoch) << 22)
                | (machineId << 12)
                | sequence;
    }
}