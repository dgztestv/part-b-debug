package com.meterline.status;

import com.google.common.base.Ticker;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * Test clock you can move forward by hand, so time-dependent behaviour is
 * deterministic instead of depending on how long the test took to run.
 *
 * <p>{@link #ticker()} exposes the same timeline to anything that needs a Guava
 * {@link Ticker}, so all time in the system under test moves together.
 */
final class MutableClock extends Clock {

    private Instant now;

    MutableClock(Instant start) {
        this.now = start;
    }

    static MutableClock startingAtEpoch() {
        return new MutableClock(Instant.EPOCH);
    }

    void advance(Duration amount) {
        now = now.plus(amount);
    }

    @Override
    public Instant instant() {
        return now;
    }

    @Override
    public ZoneId getZone() {
        return ZoneOffset.UTC;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return this;
    }

    Ticker ticker() {
        return new Ticker() {
            @Override
            public long read() {
                return now.getEpochSecond() * 1_000_000_000L + now.getNano();
            }
        };
    }
}
