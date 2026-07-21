package com.meterline.status;

import java.time.Instant;

/**
 * Published when the sweeper decides a meter has stopped reporting.
 * Downstream, this pages the on-call rota.
 */
public record MeterWentOffline(String meterId, Instant detectedAt) {
}
