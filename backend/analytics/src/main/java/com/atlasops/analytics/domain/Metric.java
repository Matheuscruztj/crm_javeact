package com.atlasops.analytics.domain;

import java.time.Instant;
import java.util.Objects;

/**
 * Value object representing a computed metric for a tenant at a specific point in time.
 *
 * @param tenantId the tenant this metric belongs to
 * @param name the metric name
 * @param value the numeric value of the metric
 * @param computedAt when this metric was last computed
 */
public record Metric(String tenantId, MetricName name, double value, Instant computedAt) {

  public Metric {
    Objects.requireNonNull(tenantId, "TenantId must not be null");
    Objects.requireNonNull(name, "Metric name must not be null");
    Objects.requireNonNull(computedAt, "ComputedAt must not be null");
  }

  /** Creates a metric with the current instant. */
  public static Metric of(String tenantId, MetricName name, double value, Instant now) {
    return new Metric(tenantId, name, value, now);
  }
}
