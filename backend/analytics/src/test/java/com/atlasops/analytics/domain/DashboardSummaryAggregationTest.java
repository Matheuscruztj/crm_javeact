package com.atlasops.analytics.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for DashboardSummary aggregation queries.
 *
 * <p>Validates: P0.A.3 — Complementar testes unitários: analytics aggregation queries
 */
class DashboardSummaryAggregationTest {

    private static final Instant NOW = Instant.parse("2025-01-15T10:00:00Z");

    @Test
    void should_returnMetricValue_when_metricExists() {
        Map<MetricName, Double> metrics = new EnumMap<>(MetricName.class);
        metrics.put(MetricName.CUSTOMER_COUNT, 42.0);
        metrics.put(MetricName.REQUEST_COUNT, 100.0);

        DashboardSummary summary = new DashboardSummary("tenant-alpha", metrics, NOW);

        assertThat(summary.get(MetricName.CUSTOMER_COUNT)).isEqualTo(42.0);
        assertThat(summary.get(MetricName.REQUEST_COUNT)).isEqualTo(100.0);
    }

    @Test
    void should_returnZero_when_metricNotPresent() {
        DashboardSummary summary = new DashboardSummary("tenant-alpha", Map.of(), NOW);

        assertThat(summary.get(MetricName.CUSTOMER_COUNT)).isEqualTo(0.0);
    }

    @Test
    void should_rejectCreation_when_tenantIdIsNull() {
        assertThatThrownBy(() -> new DashboardSummary(null, Map.of(), NOW))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("TenantId");
    }

    @Test
    void should_rejectCreation_when_metricsIsNull() {
        assertThatThrownBy(() -> new DashboardSummary("tenant-alpha", null, NOW))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Metrics");
    }

    @Test
    void should_rejectCreation_when_computedAtIsNull() {
        assertThatThrownBy(() -> new DashboardSummary("tenant-alpha", Map.of(), null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("ComputedAt");
    }

    @Test
    void should_beImmutable_when_metricsMapModified() {
        Map<MetricName, Double> mutableMetrics = new EnumMap<>(MetricName.class);
        mutableMetrics.put(MetricName.CUSTOMER_COUNT, 10.0);

        DashboardSummary summary = new DashboardSummary("tenant-alpha", mutableMetrics, NOW);
        mutableMetrics.put(MetricName.REQUEST_COUNT, 999.0);

        // The internal map should not reflect the external modification
        assertThat(summary.get(MetricName.REQUEST_COUNT)).isEqualTo(0.0);
    }

    @Test
    void should_matchTenantId_when_summaryCreated() {
        DashboardSummary summary = new DashboardSummary("tenant-beta", Map.of(), NOW);

        assertThat(summary.tenantId()).isEqualTo("tenant-beta");
    }
}
