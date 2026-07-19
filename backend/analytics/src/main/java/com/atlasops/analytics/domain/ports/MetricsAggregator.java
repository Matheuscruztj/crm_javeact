package com.atlasops.analytics.domain.ports;

import com.atlasops.analytics.domain.DashboardSummary;
import com.atlasops.analytics.domain.Metric;
import com.atlasops.analytics.domain.MetricName;

/**
 * Port for computing and caching aggregated metrics.
 */
public interface MetricsAggregator {

  /**
   * Computes or retrieves (from cache) all dashboard metrics for the tenant.
   *
   * @param tenantId the tenant to compute metrics for
   * @return the dashboard summary
   */
  DashboardSummary computeDashboard(String tenantId);

  /**
   * Computes a single metric for the tenant.
   *
   * @param tenantId the tenant to compute the metric for
   * @param name the metric to compute
   * @return the computed metric
   */
  Metric computeMetric(String tenantId, MetricName name);
}
