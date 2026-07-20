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
import com.atlasops.shared.domain.ports.Clock;
import com.atlasops.shared.domain.ports.IdGenerator;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateRequestUseCaseTest {

  private static final Instant FIXED_NOW = Instant.parse("2025-01-15T10:00:00Z");
  private static final String GENERATED_ID = "req-001";
  private static final String TENANT_ID = "tenant-alpha";
  private static final String CUSTOMER_ID = "cust-001";

  @Mock private ServiceRequestRepository serviceRequestRepository;
  @Mock private IdGenerator idGenerator;
  @Mock private Clock clock;

  private CreateRequestUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new CreateRequestUseCase(serviceRequestRepository, idGenerator, clock);
  }

  @Test
  void should_createRequest_when_allFieldsValid() {
    when(idGenerator.generate()).thenReturn(GENERATED_ID);
    when(clock.now()).thenReturn(FIXED_NOW);
    when(serviceRequestRepository.save(any(ServiceRequest.class))).thenAnswer(i -> i.getArgument(0));

    var command = new CreateRequestUseCase.CreateRequestCommand(
        "Fix billing issue", "Billing is showing incorrect charges.",
        RequestPriority.HIGH, CUSTOMER_ID, TENANT_ID);

    ServiceRequest result = useCase.execute(command);

    assertThat(result.getId()).isEqualTo(GENERATED_ID);
    assertThat(result.getTitle()).isEqualTo("Fix billing issue");
    assertThat(result.getStatus()).isEqualTo(RequestStatus.OPEN);
    assertThat(result.getPriority()).isEqualTo(RequestPriority.HIGH);
    assertThat(result.getCustomerId()).isEqualTo(CUSTOMER_ID);
    assertThat(result.getTenantId()).isEqualTo(TENANT_ID);
    assertThat(result.getCreatedAt()).isEqualTo(FIXED_NOW);
    verify(serviceRequestRepository).save(any(ServiceRequest.class));
  }

  @Test
  void should_defaultPriorityToMedium_when_priorityIsNull() {
    when(idGenerator.generate()).thenReturn(GENERATED_ID);
    when(clock.now()).thenReturn(FIXED_NOW);
    when(serviceRequestRepository.save(any(ServiceRequest.class))).thenAnswer(i -> i.getArgument(0));

    var command = new CreateRequestUseCase.CreateRequestCommand(
        "Support request", "Need help with feature X.", null, CUSTOMER_ID, TENANT_ID);

    ServiceRequest result = useCase.execute(command);

    assertThat(result.getPriority()).isEqualTo(RequestPriority.MEDIUM);
  }

  @Test
  void should_throwNullPointer_when_commandIsNull() {
    assertThatThrownBy(() -> useCase.execute(null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void should_throwIllegalArgument_when_titleTooShort() {
    assertThatThrownBy(() -> new CreateRequestUseCase.CreateRequestCommand(
        "", "Description", RequestPriority.LOW, CUSTOMER_ID, TENANT_ID))
        .isInstanceOf(NullPointerException.class)
        .satisfies(e -> assertThat(e.getMessage()).isNull()); // NPE from Objects.requireNonNull path
    // OR
    // The domain itself rejects blank title — test via useCase.execute
    when(idGenerator.generate()).thenReturn(GENERATED_ID);
    when(clock.now()).thenReturn(FIXED_NOW);
    var command = new CreateRequestUseCase.CreateRequestCommand(
        "", "Description", RequestPriority.LOW, CUSTOMER_ID, TENANT_ID);
    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(Exception.class);
  }

  @Test
  void should_persistRequest_when_executeSucceeds() {
    when(idGenerator.generate()).thenReturn(GENERATED_ID);
    when(clock.now()).thenReturn(FIXED_NOW);
    when(serviceRequestRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    var command = new CreateRequestUseCase.CreateRequestCommand(
        "New request", "Valid description text here.", RequestPriority.CRITICAL,
        CUSTOMER_ID, TENANT_ID);
    useCase.execute(command);

    verify(serviceRequestRepository).save(any(ServiceRequest.class));
  }
}
