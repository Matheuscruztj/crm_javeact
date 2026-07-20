package com.atlasops.analytics.infrastructure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Stub adapter for TimescaleDB time-series metrics storage.
 * Only instantiated when the {@code app.features.timescaledb.enabled} feature flag is true.
 *
 * <p>Not yet implemented — placeholder for time-series analytics (SLA trends, throughput, etc.).
 *
 * <p>Validates: P2.3 — TimescaleDB metrics adapter stub with feature flag
 */
@Component
@ConditionalOnProperty(name = "app.features.timescaledb.enabled", havingValue = "true")
public class TimescaleMetricsAdapter {

    private static final Logger log = LoggerFactory.getLogger(TimescaleMetricsAdapter.class);

    public TimescaleMetricsAdapter() {
        log.info("TimescaleDB adapter not yet implemented — feature flag enabled but implementation pending");
    }

    /**
     * Stub method — not yet implemented.
     *
     * @throws UnsupportedOperationException always
     */
    public void recordMetric(String metricName, double value) {
        throw new UnsupportedOperationException(
                "TimescaleDB adapter not yet implemented. Feature flag enabled but dependency not configured.");
    }
}
