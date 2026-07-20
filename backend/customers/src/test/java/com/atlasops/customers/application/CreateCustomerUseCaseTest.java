package com.atlasops.customers.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlasops.customers.domain.Customer;
import com.atlasops.customers.domain.CustomerStatus;
import com.atlasops.customers.domain.ports.CustomerRepository;
import com.atlasops.shared.domain.exceptions.DuplicateResourceException;
import com.atlasops.shared.domain.ports.Clock;
import com.atlasops.shared.domain.ports.EventPublisher;
import com.atlasops.shared.domain.ports.IdGenerator;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateCustomerUseCaseTest {

  private static final Instant FIXED_NOW = Instant.parse("2025-01-15T10:00:00Z");
  private static final String GENERATED_ID = "customer-001";
  private static final String TENANT_ID = "tenant-alpha";
  private static final String ACTOR_ID = "user-001";

  @Mock private CustomerRepository customerRepository;
  @Mock private EventPublisher eventPublisher;
  @Mock private IdGenerator idGenerator;
  @Mock private Clock clock;

  private CreateCustomerUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new CreateCustomerUseCase(customerRepository, eventPublisher, idGenerator, clock);
  }

  @Test
  void should_createCustomer_when_allFieldsValid() {
    when(idGenerator.generate()).thenReturn(GENERATED_ID);
    when(clock.now()).thenReturn(FIXED_NOW);
    when(customerRepository.existsByEmailAndTenantId("alpha@empresa.com", TENANT_ID))
        .thenReturn(false);
    when(customerRepository.save(any(Customer.class))).thenAnswer(i -> i.getArgument(0));

    var command = new CreateCustomerCommand(
        "Empresa Alpha", "alpha@empresa.com", null, null, null, null, null, null, null,
        TENANT_ID, ACTOR_ID);

    Customer result = useCase.execute(command);

    assertThat(result.getId()).isEqualTo(GENERATED_ID);
    assertThat(result.getName()).isEqualTo("Empresa Alpha");
    assertThat(result.getEmail().getValue()).isEqualTo("alpha@empresa.com");
    assertThat(result.getStatus()).isEqualTo(CustomerStatus.ACTIVE);
    assertThat(result.getTenantId().getValue()).isEqualTo(TENANT_ID);
    assertThat(result.getCreatedAt()).isEqualTo(FIXED_NOW);
    verify(eventPublisher).publish(any());
  }

  @Test
  void should_trimName_when_nameHasLeadingTrailingSpaces() {
    when(idGenerator.generate()).thenReturn(GENERATED_ID);
    when(clock.now()).thenReturn(FIXED_NOW);
    when(customerRepository.existsByEmailAndTenantId(anyString(), anyString())).thenReturn(false);
    when(customerRepository.save(any(Customer.class))).thenAnswer(i -> i.getArgument(0));

    var command = new CreateCustomerCommand(
        "  Empresa Beta  ", "beta@test.com", null, null, null, null, null, null, null,
        TENANT_ID, ACTOR_ID);

    Customer result = useCase.execute(command);

    assertThat(result.getName()).isEqualTo("Empresa Beta");
  }

  @Test
  void should_throwDuplicate_when_emailAlreadyExistsInTenant() {
    when(customerRepository.existsByEmailAndTenantId("dup@test.com", TENANT_ID)).thenReturn(true);

    var command = new CreateCustomerCommand(
        "Some Corp", "dup@test.com", null, null, null, null, null, null, null,
        TENANT_ID, ACTOR_ID);

    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(DuplicateResourceException.class)
        .hasMessageContaining("dup@test.com");
    verify(customerRepository, never()).save(any());
    verify(eventPublisher, never()).publish(any());
  }

  @Test
  void should_throwIllegalArgument_when_nameIsBlank() {
    var command = new CreateCustomerCommand(
        "  ", "valid@test.com", null, null, null, null, null, null, null,
        TENANT_ID, ACTOR_ID);

    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("name");
    verify(customerRepository, never()).save(any());
  }

  @Test
  void should_throwIllegalArgument_when_emailIsBlank() {
    var command = new CreateCustomerCommand(
        "Valid Name", "  ", null, null, null, null, null, null, null,
        TENANT_ID, ACTOR_ID);

    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Email");
  }

  @Test
  void should_throwIllegalArgument_when_tenantIdIsBlank() {
    var command = new CreateCustomerCommand(
        "Valid Name", "valid@test.com", null, null, null, null, null, null, null,
        "  ", ACTOR_ID);

    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("TenantId");
  }

  @Test
  void should_createCustomerWithAddress_when_addressProvided() {
    when(idGenerator.generate()).thenReturn(GENERATED_ID);
    when(clock.now()).thenReturn(FIXED_NOW);
    when(customerRepository.existsByEmailAndTenantId(anyString(), anyString())).thenReturn(false);
    when(customerRepository.save(any(Customer.class))).thenAnswer(i -> i.getArgument(0));

    var command = new CreateCustomerCommand(
        "Corp HQ", "hq@corp.com", "123 Main St", "São Paulo", "SP",
        "01310-100", "BR", -23.5505, -46.6333, TENANT_ID, ACTOR_ID);

    Customer result = useCase.execute(command);

    assertThat(result.getAddress()).isNotNull();
    assertThat(result.getAddress().getCity()).isEqualTo("São Paulo");
  }

  @Test
  void should_publishCustomerCreatedEvent_when_customerSaved() {
    when(idGenerator.generate()).thenReturn(GENERATED_ID);
    when(clock.now()).thenReturn(FIXED_NOW);
    when(customerRepository.existsByEmailAndTenantId(anyString(), anyString())).thenReturn(false);
    when(customerRepository.save(any(Customer.class))).thenAnswer(i -> i.getArgument(0));

    var command = new CreateCustomerCommand(
        "Event Corp", "event@corp.com", null, null, null, null, null, null, null,
        TENANT_ID, ACTOR_ID);
    useCase.execute(command);

    verify(eventPublisher).publish(any());
  }
}
