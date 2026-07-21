package com.meterline.status;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;

public interface MeterRepository {

    Optional<MeterStatus> findById(String meterId);

    Collection<String> allMeterIds();

    void updateLastSeen(String meterId, Instant at);

    void markOnline(String meterId);

    void markOffline(String meterId);
}
