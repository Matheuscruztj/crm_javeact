package com.atlasops.analytics.infrastructure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Adapter for ClickHouse OLAP analytics queries.
 * Only instantiated when the {@code app.features.clickhouse.enabled} feature flag is true.
 *
 * <p>When ClickHouse is not provisioned, this adapter degrades gracefully and treats queries as
 * no-op requests so the application can continue operating.
 *
 * <p>Validates: P2.4 — ClickHouse analytics adapter stub with feature flag
 */
@Component
@ConditionalOnProperty(name = "app.features.clickhouse.enabled", havingValue = "true")
public class ClickHouseAnalyticsAdapter {

    private static final Logger log = LoggerFactory.getLogger(ClickHouseAnalyticsAdapter.class);

    public ClickHouseAnalyticsAdapter() {
        log.info("ClickHouse adapter enabled without ClickHouse dependency; using no-op fallback");
    }

    public void executeQuery(String query) {
        log.debug("ClickHouse query requested but dependency is not configured; no-op fallback: {}", query);
    }
}
