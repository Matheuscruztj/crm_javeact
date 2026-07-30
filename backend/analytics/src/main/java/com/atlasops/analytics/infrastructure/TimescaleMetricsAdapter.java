package com.atlasops.analytics.infrastructure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Adapter for TimescaleDB time-series metrics storage.
 * Only instantiated when the {@code app.features.timescaledb.enabled} feature flag is true.
 *
 * <p>When TimescaleDB is not provisioned, this adapter degrades gracefully and records the
 * metric request as a no-op so the application can continue operating.
 *
 * <p>Validates: P2.3 — TimescaleDB metrics adapter stub with feature flag
 */
@Component
@ConditionalOnProperty(name = "app.features.timescaledb.enabled", havingValue = "true")
public class TimescaleMetricsAdapter {

    private static final Logger log = LoggerFactory.getLogger(TimescaleMetricsAdapter.class);

    public TimescaleMetricsAdapter() {
        log.info("TimescaleDB adapter enabled without TimescaleDB dependency; using no-op fallback");
    }

    public void recordMetric(String metricName, double value) {
        log.debug("Recording metric '{}'={} via no-op Timescale fallback", metricName, value);
    }
}
