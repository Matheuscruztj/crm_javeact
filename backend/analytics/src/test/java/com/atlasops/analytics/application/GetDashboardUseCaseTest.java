package com.atlasops.analytics.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlasops.analytics.domain.DashboardSummary;
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
 * Unit tests for {@link GetDashboardUseCase}.
 *
 * <p>Validates: P0.A.3 — Complement unit tests for analytics module
 */
@ExtendWith(MockitoExtension.class)
class GetDashboardUseCaseTest {

    @Mock
    private MetricsAggregator metricsAggregator;

    @InjectMocks
    private GetDashboardUseCase useCase;

    private static final Instant NOW = Instant.parse("2025-01-15T10:00:00Z");

    @Test
    void should_returnDashboard_when_tenantIdIsValid() {
        DashboardSummary expected = new DashboardSummary(
                "tenant-alpha",
                Map.of(MetricName.CUSTOMER_COUNT, 5.0),
                NOW);
        when(metricsAggregator.computeDashboard("tenant-alpha")).thenReturn(expected);

        DashboardSummary result = useCase.execute("tenant-alpha");

        assertThat(result.tenantId()).isEqualTo("tenant-alpha");
        assertThat(result.get(MetricName.CUSTOMER_COUNT)).isEqualTo(5.0);
        verify(metricsAggregator).computeDashboard("tenant-alpha");
    }

    @Test
    void should_throwNullPointer_when_tenantIdIsNull() {
        assertThatThrownBy(() -> useCase.execute(null))
                .isInstanceOf(NullPointerException.class);
    }
}
