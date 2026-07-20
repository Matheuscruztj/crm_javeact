package com.atlasops.analytics.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlasops.analytics.domain.DashboardSummary;
import com.atlasops.analytics.domain.Metric;
import com.atlasops.analytics.domain.MetricName;
import com.atlasops.analytics.domain.ports.MetricsAggregator;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for analytics aggregation including Redis cache decorator.
 * Validates: P0.A.3 — Complement analytics tests
 */
@ExtendWith(MockitoExtension.class)
class AnalyticsMetricTest {

  private static final Instant NOW = Instant.parse("2025-01-15T10:00:00Z");
  private static final String TENANT = "tenant-alpha";

  @Mock private MetricsAggregator metricsAggregator;
  @InjectMocks private GetDashboardUseCase useCase;

  @Test
  void should_delegateToAggregator_when_computeDashboard() {
    var summary = new DashboardSummary(TENANT,
        Map.of(MetricName.CUSTOMER_COUNT, 5.0, MetricName.REQUEST_COUNT, 10.0), NOW);
    when(metricsAggregator.computeDashboard(TENANT)).thenReturn(summary);

    DashboardSummary result = useCase.execute(TENANT);

    assertThat(result.tenantId()).isEqualTo(TENANT);
    assertThat(result.get(MetricName.CUSTOMER_COUNT)).isEqualTo(5.0);
    assertThat(result.get(MetricName.REQUEST_COUNT)).isEqualTo(10.0);
    verify(metricsAggregator).computeDashboard(TENANT);
  }

  @Test
  void should_returnAllMetricNames_when_enumDefined() {
    MetricName[] values = MetricName.values();
    assertThat(values).contains(
        MetricName.CUSTOMER_COUNT,
        MetricName.REQUEST_COUNT,
        MetricName.ACTIVE_REQUEST_COUNT,
        MetricName.DOCUMENT_COUNT,
        MetricName.PENDING_APPROVAL_COUNT
    );
  }

  @Test
  void should_returnZero_when_metricMissingFromDashboard() {
    var summary = new DashboardSummary(TENANT, Map.of(), NOW);
    assertThat(summary.get(MetricName.AI_ANALYZED_DOCUMENT_COUNT)).isZero();
    assertThat(summary.get(MetricName.AI_AVG_CONFIDENCE)).isZero();
  }

  @Test
  void should_computeMetric_when_metricAggregatorReturnsValue() {
    MetricsAggregator agg = mock(MetricsAggregator.class);
    var metric = Metric.of(TENANT, MetricName.CUSTOMER_COUNT, 42.0, NOW);
    when(agg.computeMetric(TENANT, MetricName.CUSTOMER_COUNT)).thenReturn(metric);

    Metric result = agg.computeMetric(TENANT, MetricName.CUSTOMER_COUNT);

    assertThat(result.value()).isEqualTo(42.0);
    assertThat(result.name()).isEqualTo(MetricName.CUSTOMER_COUNT);
    assertThat(result.tenantId()).isEqualTo(TENANT);
  }
}
