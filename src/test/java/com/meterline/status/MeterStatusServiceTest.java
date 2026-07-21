package com.meterline.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class MeterStatusServiceTest {

    private static final String METER = "m-4417";
    private static final Duration HEARTBEAT_INTERVAL = Duration.ofSeconds(30);

    private MutableClock clock;
    private InMemoryMeterRepository repository;
    private RecordingEventPublisher events;
    private MeterStatusService service;

    @BeforeEach
    void setUp() {
        clock = MutableClock.startingAtEpoch();
        repository = new InMemoryMeterRepository();
        events = new RecordingEventPublisher();
        service = new MeterStatusService(repository, events, clock, clock.ticker());
    }

    @Test
    @DisplayName("a meter that stops reporting is marked offline")
    void deadMeterIsMarkedOffline() {
        repository.register(METER, clock.instant());

        // no heartbeats at all for well over the 90s window
        clock.advance(Duration.ofMinutes(4));
        service.sweep();

        assertThat(events.offlineMeterIds()).containsExactly(METER);
        assertThat(repository.findById(METER).orElseThrow().isOnline()).isFalse();
    }

    @Test
    @DisplayName("a heartbeat records the time it arrived")
    void heartbeatPersistsLastSeen() {
        repository.register(METER, clock.instant());

        clock.advance(Duration.ofSeconds(50));
        Instant beat = clock.instant();
        service.recordHeartbeat(METER, beat);

        assertThat(repository.findById(METER).orElseThrow().getLastSeen()).isEqualTo(beat);
    }

    @Test
    @DisplayName("a healthy meter is never marked offline")
    void healthyMeterIsNeverMarkedOffline() {
        repository.register(METER, clock.instant());

        // Meter is healthy for five minutes: heartbeat every 30s, comfortably
        // inside the 90s offline window. The sweeper runs on the same cadence.
        for (int i = 0; i < 10; i++) {
            clock.advance(HEARTBEAT_INTERVAL);
            service.recordHeartbeat(METER, clock.instant());
            service.sweep();
        }

        assertThat(events.published())
                .describedAs("a meter heartbeating every 30s must never be reported offline")
                .isEmpty();
    }
}
