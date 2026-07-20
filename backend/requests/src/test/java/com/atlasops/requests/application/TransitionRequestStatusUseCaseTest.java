package com.atlasops.requests.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlasops.requests.domain.RequestPriority;
import com.atlasops.requests.domain.RequestStatus;
import com.atlasops.requests.domain.ServiceRequest;
import com.atlasops.requests.domain.ports.ServiceRequestRepository;
import com.atlasops.shared.domain.exceptions.ResourceNotFoundException;
import com.atlasops.shared.domain.ports.EventPublisher;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransitionRequestStatusUseCaseTest {

  private static final Instant NOW = Instant.parse("2025-01-15T10:00:00Z");
  private static final String TENANT_ID = "tenant-alpha";

  @Mock private ServiceRequestRepository serviceRequestRepository;
  @Mock private EventPublisher eventPublisher;

  private TransitionRequestStatusUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new TransitionRequestStatusUseCase(serviceRequestRepository, eventPublisher);
  }

  private ServiceRequest openRequest(String id) {
    return ServiceRequest.create(id, "Test Request", "Description",
        RequestPriority.MEDIUM, "cust-001", TENANT_ID, NOW);
  }

  @Test
  void should_transitionToInProgress_when_requestIsOpen() {
    ServiceRequest request = openRequest("req-001");
    when(serviceRequestRepository.findByIdAndTenantId("req-001", TENANT_ID))
        .thenReturn(Optional.of(request));
    when(serviceRequestRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    var command = new TransitionRequestStatusUseCase.TransitionCommand(
        "req-001", RequestStatus.IN_PROGRESS, TENANT_ID, "analyst-001");

    ServiceRequest result = useCase.execute(command);

    assertThat(result.getStatus()).isEqualTo(RequestStatus.IN_PROGRESS);
    verify(eventPublisher).publish(any());
  }

  @Test
  void should_throwResourceNotFound_when_requestDoesNotExist() {
    when(serviceRequestRepository.findByIdAndTenantId("nonexistent", TENANT_ID))
        .thenReturn(Optional.empty());

    var command = new TransitionRequestStatusUseCase.TransitionCommand(
        "nonexistent", RequestStatus.IN_PROGRESS, TENANT_ID, "analyst-001");

    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void should_throwNullPointer_when_commandIsNull() {
    assertThatThrownBy(() -> useCase.execute(null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void should_publishEvent_when_transitionSucceeds() {
    ServiceRequest request = openRequest("req-002");
    when(serviceRequestRepository.findByIdAndTenantId("req-002", TENANT_ID))
        .thenReturn(Optional.of(request));
    when(serviceRequestRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    var command = new TransitionRequestStatusUseCase.TransitionCommand(
        "req-002", RequestStatus.IN_PROGRESS, TENANT_ID, "analyst-002");
    useCase.execute(command);

    verify(eventPublisher).publish(any());
    verify(serviceRequestRepository).save(any());
  }
}
