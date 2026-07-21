package com.meterline.status;

import java.util.ArrayList;
import java.util.List;

final class RecordingEventPublisher implements EventPublisher {

    private final List<MeterWentOffline> published = new ArrayList<>();

    @Override
    public void publish(MeterWentOffline event) {
        published.add(event);
    }

    List<MeterWentOffline> published() {
        return List.copyOf(published);
    }

    List<String> offlineMeterIds() {
        return published.stream().map(MeterWentOffline::meterId).toList();
    }
}
