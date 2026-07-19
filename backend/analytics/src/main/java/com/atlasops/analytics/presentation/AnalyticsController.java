package com.atlasops.analytics.presentation;

import com.atlasops.analytics.application.GetDashboardUseCase;
import com.atlasops.analytics.domain.DashboardSummary;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for analytics dashboard and metrics.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>GET /api/v1/analytics/dashboard — returns all key metrics for the tenant dashboard
 * </ul>
 *
 * <p>Validates: Requirements P0.I.2
 */
@RestController
@RequestMapping("/api/v1/analytics")
@Tag(name = "Analytics", description = "Analytics dashboard and metrics endpoints")
public class AnalyticsController {

  private final GetDashboardUseCase getDashboardUseCase;

  public AnalyticsController(GetDashboardUseCase getDashboardUseCase) {
    this.getDashboardUseCase = getDashboardUseCase;
  }

  /**
   * Returns the analytics dashboard summary for the tenant.
   * Results are cached with a TTL of 5 minutes.
   */
  @Operation(
      summary = "Get analytics dashboard",
      description = "Returns aggregated metrics for the tenant dashboard (cached 5 minutes)")
  @ApiResponse(responseCode = "200", description = "Dashboard metrics")
  @GetMapping("/dashboard")
  public ResponseEntity<DashboardResponse> getDashboard(
      @RequestHeader("X-Tenant-ID") String tenantId) {
    DashboardSummary summary = getDashboardUseCase.execute(tenantId);
    return ResponseEntity.ok(DashboardResponse.from(summary));
  }
}
