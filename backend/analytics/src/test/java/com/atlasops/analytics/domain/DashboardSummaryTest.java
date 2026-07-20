package com.atlasops.analytics.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link DashboardSummary} domain record.
 *
 * <p>Validates: P0.A.3 — Complement unit tests for analytics module
 */
class DashboardSummaryTest {

    private static final Instant NOW = Instant.parse("2025-01-15T10:00:00Z");

    @Test
    void should_createDashboardSummary_when_allFieldsValid() {
        Map<MetricName, Double> metrics = Map.of(
                MetricName.CUSTOMER_COUNT, 10.0,
                MetricName.REQUEST_COUNT, 25.0);

        DashboardSummary summary = new DashboardSummary("tenant-alpha", metrics, NOW);

        assertThat(summary.tenantId()).isEqualTo("tenant-alpha");
        assertThat(summary.get(MetricName.CUSTOMER_COUNT)).isEqualTo(10.0);
        assertThat(summary.get(MetricName.REQUEST_COUNT)).isEqualTo(25.0);
    }

    @Test
    void should_returnZero_when_metricNotPresent() {
        DashboardSummary summary = new DashboardSummary("tenant-alpha", Map.of(), NOW);

        assertThat(summary.get(MetricName.CUSTOMER_COUNT)).isEqualTo(0.0);
    }

    @Test
    void should_rejectCreation_when_tenantIdIsNull() {
        assertThatThrownBy(() -> new DashboardSummary(null, Map.of(), NOW))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void should_rejectCreation_when_metricsIsNull() {
        assertThatThrownBy(() -> new DashboardSummary("tenant-alpha", null, NOW))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void should_beImmutable_when_originalMapMutated() {
        java.util.HashMap<MetricName, Double> mutableMap = new java.util.HashMap<>();
        mutableMap.put(MetricName.CUSTOMER_COUNT, 5.0);

        DashboardSummary summary = new DashboardSummary("tenant-alpha", mutableMap, NOW);
        mutableMap.put(MetricName.REQUEST_COUNT, 99.0);

        assertThat(summary.get(MetricName.REQUEST_COUNT)).isEqualTo(0.0);
    }
}
