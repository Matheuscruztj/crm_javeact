package com.atlasops.requests.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.atlasops.requests.domain.RequestPriority;
import com.atlasops.requests.domain.RequestStatus;
import com.atlasops.requests.domain.RequestStatusHistory;
import com.atlasops.requests.domain.ServiceRequest;
import com.atlasops.requests.domain.ports.RequestStatusHistoryRepository;
import com.atlasops.requests.domain.ports.ServiceRequestRepository;
import com.atlasops.shared.domain.exceptions.ResourceNotFoundException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetRequestStatusHistoryUseCaseTest {

  private static final Instant NOW = Instant.parse("2025-01-15T10:00:00Z");
  private static final String TENANT = "tenant-alpha";
  private static final String REQUEST_ID = "req-001";

  @Mock private ServiceRequestRepository requestRepository;
  @Mock private RequestStatusHistoryRepository historyRepository;

  private GetRequestStatusHistoryUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new GetRequestStatusHistoryUseCase(requestRepository, historyRepository);
  }

  private ServiceRequest openRequest() {
    return ServiceRequest.create(REQUEST_ID, "Issue", "Desc",
        RequestPriority.LOW, "cust-001", TENANT, NOW);
  }

  @Test
  void should_returnHistory_when_requestExists() {
    when(requestRepository.findByIdAndTenantId(REQUEST_ID, TENANT))
        .thenReturn(Optional.of(openRequest()));

    RequestStatusHistory h1 = RequestStatusHistory.create(
        "h-001", REQUEST_ID, null, RequestStatus.OPEN, null, "system", NOW);
    RequestStatusHistory h2 = RequestStatusHistory.create(
        "h-002", REQUEST_ID, RequestStatus.OPEN, RequestStatus.IN_PROGRESS,
        "Assigned", "analyst-001", NOW.plusSeconds(60));

    when(historyRepository.findByRequestId(REQUEST_ID, TENANT))
        .thenReturn(List.of(h1, h2));

    List<RequestStatusHistory> result = useCase.execute(REQUEST_ID, TENANT);

    assertThat(result).hasSize(2);
    assertThat(result.get(0).getToStatus()).isEqualTo(RequestStatus.OPEN);
    assertThat(result.get(1).getToStatus()).isEqualTo(RequestStatus.IN_PROGRESS);
    assertThat(result.get(1).getActorId()).isEqualTo("analyst-001");
  }

  @Test
  void should_returnEmpty_when_noHistoryRecorded() {
    when(requestRepository.findByIdAndTenantId(REQUEST_ID, TENANT))
        .thenReturn(Optional.of(openRequest()));
    when(historyRepository.findByRequestId(REQUEST_ID, TENANT)).thenReturn(List.of());

    List<RequestStatusHistory> result = useCase.execute(REQUEST_ID, TENANT);
    assertThat(result).isEmpty();
  }

  @Test
  void should_throwNotFound_when_requestMissing() {
    when(requestRepository.findByIdAndTenantId("x", TENANT)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> useCase.execute("x", TENANT))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void should_throwNullPointer_when_requestIdIsNull() {
    assertThatThrownBy(() -> useCase.execute(null, TENANT))
        .isInstanceOf(NullPointerException.class);
  }
}
