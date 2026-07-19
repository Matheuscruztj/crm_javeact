package com.atlasops.analytics.application;

import com.atlasops.analytics.domain.DashboardSummary;
import com.atlasops.analytics.domain.ports.MetricsAggregator;
import java.util.Objects;

/**
 * Use case for retrieving the analytics dashboard summary for a tenant.
 * Results are cached by the {@link MetricsAggregator} (TTL 5 minutes).
 */
public class GetDashboardUseCase {

  private final MetricsAggregator metricsAggregator;

  public GetDashboardUseCase(MetricsAggregator metricsAggregator) {
    this.metricsAggregator = Objects.requireNonNull(metricsAggregator);
  }

  /**
   * Returns the dashboard summary for the given tenant.
   *
   * @param tenantId the tenant to compute metrics for
   * @return the dashboard summary
   */
  public DashboardSummary execute(String tenantId) {
    Objects.requireNonNull(tenantId, "TenantId must not be null");
    return metricsAggregator.computeDashboard(tenantId);
  }
}
