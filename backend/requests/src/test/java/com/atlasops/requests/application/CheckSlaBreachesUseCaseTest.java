package com.atlasops.requests.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

import com.atlasops.requests.domain.RequestPriority;
import com.atlasops.requests.domain.RequestStatus;
import com.atlasops.requests.domain.ServiceRequest;
import com.atlasops.requests.domain.ports.ServiceRequestRepository;
import com.atlasops.shared.domain.ports.Clock;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for CheckSlaBreachesUseCase.
 * Validates: P2.10 — Request SLA with deadline and alerts
 */
@ExtendWith(MockitoExtension.class)
class CheckSlaBreachesUseCaseTest {

    private static final Instant NOW = Instant.parse("2025-01-15T10:00:00Z");
    private static final String TENANT = "tenant-alpha";

    @Mock private ServiceRequestRepository requestRepository;
    @Mock private Clock clock;

    private CheckSlaBreachesUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CheckSlaBreachesUseCase(requestRepository, clock);
        when(clock.now()).thenReturn(NOW);
    }

    private ServiceRequest aRequest(String id, Instant createdAt) {
        return ServiceRequest.create(id, "Test Request " + id, "Description",
                RequestPriority.MEDIUM, "cust-001", TENANT, createdAt);
    }

    @Test
    void should_returnZero_when_noOpenRequests() {
        when(requestRepository.findAllByTenantId(isNull(), any(RequestStatus.class),
                isNull(), isNull(), anyInt(), anyInt()))
                .thenReturn(new ServiceRequestPageResult(List.of(), 0, 0, 0));

        int result = useCase.execute();

        assertThat(result).isZero();
    }

    @Test
    void should_countBreaches_when_slaDeadlineExceeded() {
        // Request created 10 days ago — well past the 5-day SLA default
        Instant oldCreatedAt = NOW.minusSeconds(10L * 24 * 3600);
        ServiceRequest oldRequest = aRequest("req-001", oldCreatedAt);

        when(requestRepository.findAllByTenantId(isNull(), any(RequestStatus.class),
                isNull(), isNull(), anyInt(), anyInt()))
                .thenReturn(new ServiceRequestPageResult(List.of(oldRequest), 1, 1, 1));

        int result = useCase.execute();

        assertThat(result).isEqualTo(1);
    }

    @Test
    void should_notCount_when_slaDeadlineNotReached() {
        // Request created 1 day ago — within the 5-day SLA
        Instant recentCreatedAt = NOW.minusSeconds(1L * 24 * 3600);
        ServiceRequest recentRequest = aRequest("req-002", recentCreatedAt);

        when(requestRepository.findAllByTenantId(isNull(), any(RequestStatus.class),
                isNull(), isNull(), anyInt(), anyInt()))
                .thenReturn(new ServiceRequestPageResult(List.of(recentRequest), 1, 1, 1));

        int result = useCase.execute();

        assertThat(result).isZero();
    }

    @Test
    void should_throwNullPointer_when_clockIsNull() {
        assertThatThrownBy(() -> new CheckSlaBreachesUseCase(requestRepository, null))
                .isInstanceOf(NullPointerException.class);
    }
}
