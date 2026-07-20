package com.atlasops.analytics.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.atlasops.analytics.application.GetDashboardUseCase;
import com.atlasops.analytics.domain.DashboardSummary;
import com.atlasops.analytics.domain.MetricName;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Unit tests for AnalyticsController.
 * Validates: P0.I.2 — Analytics Module Foundation
 */
@ExtendWith(MockitoExtension.class)
class AnalyticsControllerTest {

    private static final String TENANT = "tenant-alpha";

    @Mock private GetDashboardUseCase getDashboardUseCase;

    private AnalyticsController controller;

    @BeforeEach
    void setUp() {
        controller = new AnalyticsController(getDashboardUseCase);
    }

    @Test
    void should_returnDashboard_when_tenantHasMetrics() {
        DashboardSummary summary = new DashboardSummary(
                TENANT,
                Map.of(
                        MetricName.CUSTOMER_COUNT, 15.0,
                        MetricName.REQUEST_COUNT, 42.0,
                        MetricName.PENDING_APPROVAL_COUNT, 3.0),
                Instant.parse("2025-01-15T10:00:00Z"));
        when(getDashboardUseCase.execute(TENANT)).thenReturn(summary);

        ResponseEntity<DashboardResponse> response = controller.getDashboard(TENANT);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().tenantId()).isEqualTo(TENANT);
    }

    @Test
    void should_returnEmptyMetrics_when_tenantHasNoData() {
        DashboardSummary empty = new DashboardSummary(
                TENANT, Map.of(), Instant.parse("2025-01-15T10:00:00Z"));
        when(getDashboardUseCase.execute(TENANT)).thenReturn(empty);

        ResponseEntity<DashboardResponse> response = controller.getDashboard(TENANT);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
