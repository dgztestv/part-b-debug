package com.meterline.status;

import java.time.Instant;
import java.util.Objects;

/**
 * Point-in-time view of a meter's connectivity.
 */
public final class MeterStatus {

    private final String meterId;
    private final boolean online;
    private final Instant lastSeen;

    public MeterStatus(String meterId, boolean online, Instant lastSeen) {
        this.meterId = Objects.requireNonNull(meterId);
        this.online = online;
        this.lastSeen = Objects.requireNonNull(lastSeen);
    }

    public String getMeterId() {
        return meterId;
    }

    public boolean isOnline() {
        return online;
    }

    public Instant getLastSeen() {
        return lastSeen;
    }

    public MeterStatus withOnline(boolean value) {
        return new MeterStatus(meterId, value, lastSeen);
    }

    public MeterStatus withLastSeen(Instant value) {
        return new MeterStatus(meterId, online, value);
    }

    @Override
    public String toString() {
        return "MeterStatus[" + meterId + " online=" + online + " lastSeen=" + lastSeen + "]";
    }
}
