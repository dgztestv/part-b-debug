package com.meterline.status;

public interface EventPublisher {

    void publish(MeterWentOffline event);
}
