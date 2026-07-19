package com.atlasops.analytics.domain;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * Aggregate view of key metrics for the analytics dashboard.
 *
 * @param tenantId the tenant this summary belongs to
 * @param metrics map of metric name to value
 * @param computedAt when this summary was computed
 */
public record DashboardSummary(String tenantId, Map<MetricName, Double> metrics, Instant computedAt) {

  public DashboardSummary {
    Objects.requireNonNull(tenantId, "TenantId must not be null");
    Objects.requireNonNull(metrics, "Metrics must not be null");
    Objects.requireNonNull(computedAt, "ComputedAt must not be null");
    metrics = Map.copyOf(metrics); // defensive copy — immutable
  }

  /** Returns a specific metric value, or 0.0 if not present. */
  public double get(MetricName name) {
    return metrics.getOrDefault(name, 0.0);
  }
}
