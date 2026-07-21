package com.atlasops.analytics.presentation;

import com.atlasops.analytics.domain.DashboardSummary;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Response DTO for the analytics dashboard.
 */
public record DashboardResponse(
    String tenantId,
    Map<String, Double> metrics,
    Instant computedAt) {

  public static DashboardResponse from(DashboardSummary summary) {
    Map<String, Double> metricsMap = summary.metrics().entrySet().stream()
        .collect(Collectors.toMap(
            e -> e.getKey().name(),
            Map.Entry::getValue));

    return new DashboardResponse(summary.tenantId(), metricsMap, summary.computedAt());
  }
}
