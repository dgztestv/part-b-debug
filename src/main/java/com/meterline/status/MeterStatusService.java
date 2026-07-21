package com.meterline.status;

import com.google.common.base.Ticker;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * Tracks which meters are reporting.
 *
 * <p>Meters heartbeat every 30 seconds. If we have not heard from one inside
 * {@link #OFFLINE_AFTER} the sweeper marks it offline and publishes an event,
 * which pages the on-call rota.
 *
 * <p>The status read path is hot - the operator dashboard polls it for every meter
 * on a site - so lookups go through a cache.
 */
@Service
public class MeterStatusService {

    private static final Logger log = LoggerFactory.getLogger(MeterStatusService.class);

    private static final Duration OFFLINE_AFTER = Duration.ofSeconds(90);

    private final MeterRepository repository;
    private final EventPublisher events;
    private final Clock clock;

    private final LoadingCache<String, MeterStatus> cache;

    public MeterStatusService(MeterRepository repository,
                             EventPublisher events,
                             Clock clock,
                             Ticker ticker) {
        this.repository = repository;
        this.events = events;
        this.clock = clock;
        this.cache = CacheBuilder.newBuilder()
                .maximumSize(10_000)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .ticker(ticker)
                .build(new CacheLoader<String, MeterStatus>() {
                    @Override
                    public MeterStatus load(String meterId) {
                        return repository.findById(meterId)
                                .orElseThrow(() -> new IllegalArgumentException("unknown meter " + meterId));
                    }
                });
    }

    /** Called from the MQTT listener on every heartbeat frame. */
    public void recordHeartbeat(String meterId, Instant at) {
        repository.updateLastSeen(meterId, at);
        repository.markOnline(meterId);
    }

    /** Serves the operator dashboard, and used by the sweeper below. */
    public MeterStatus statusOf(String meterId) {
        return cache.getUnchecked(meterId);
    }

    @Scheduled(fixedDelay = 30_000)
    public void sweep() {
        Instant cutoff = clock.instant().minus(OFFLINE_AFTER);

        for (String meterId : repository.allMeterIds()) {
            MeterStatus status = statusOf(meterId);

            if (status.isOnline() && status.getLastSeen().isBefore(cutoff)) {
                log.info("meter {} last seen {}, marking offline", meterId, status.getLastSeen());
                repository.markOffline(meterId);
                events.publish(new MeterWentOffline(meterId, clock.instant()));
            }
        }
    }
}
