package com.atlasops.analytics.infrastructure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Stub adapter for ClickHouse OLAP analytics queries.
 * Only instantiated when the {@code app.features.clickhouse.enabled} feature flag is true.
 *
 * <p>Not yet implemented — placeholder for large-scale analytics reports and aggregations.
 *
 * <p>Validates: P2.4 — ClickHouse analytics adapter stub with feature flag
 */
@Component
@ConditionalOnProperty(name = "app.features.clickhouse.enabled", havingValue = "true")
public class ClickHouseAnalyticsAdapter {

    private static final Logger log = LoggerFactory.getLogger(ClickHouseAnalyticsAdapter.class);

    public ClickHouseAnalyticsAdapter() {
        log.info("ClickHouse adapter not yet implemented — feature flag enabled but implementation pending");
    }

    /**
     * Stub method — not yet implemented.
     *
     * @throws UnsupportedOperationException always
     */
    public void executeQuery(String query) {
        throw new UnsupportedOperationException(
                "ClickHouse adapter not yet implemented. Feature flag enabled but dependency not configured.");
    }
}
