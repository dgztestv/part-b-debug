package com.meterline.status;

import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stands in for the JDBC repository in local runs and tests. The production
 * implementation talks to MySQL but has the same semantics.
 */
@Repository
public class InMemoryMeterRepository implements MeterRepository {

    private final Map<String, MeterStatus> meters = new ConcurrentHashMap<>();

    /** Test/bootstrap helper - registers a meter that has just come online. */
    public void register(String meterId, Instant lastSeen) {
        meters.put(meterId, new MeterStatus(meterId, true, lastSeen));
    }

    @Override
    public Optional<MeterStatus> findById(String meterId) {
        return Optional.ofNullable(meters.get(meterId));
    }

    @Override
    public Collection<String> allMeterIds() {
        return List.copyOf(meters.keySet());
    }

    @Override
    public void updateLastSeen(String meterId, Instant at) {
        meters.computeIfPresent(meterId, (id, current) -> current.withLastSeen(at));
    }

    @Override
    public void markOnline(String meterId) {
        meters.computeIfPresent(meterId, (id, current) -> current.withOnline(true));
    }

    @Override
    public void markOffline(String meterId) {
        meters.computeIfPresent(meterId, (id, current) -> current.withOnline(false));
    }
}
