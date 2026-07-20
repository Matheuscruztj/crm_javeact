package com.atlasops.customers.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlasops.customers.domain.Customer;
import com.atlasops.customers.domain.ports.CustomerRepository;
import com.atlasops.shared.domain.exceptions.DuplicateResourceException;
import com.atlasops.shared.domain.exceptions.ResourceNotFoundException;
import com.atlasops.shared.domain.ports.Clock;
import com.atlasops.shared.domain.types.Email;
import com.atlasops.shared.domain.types.TenantId;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateCustomerUseCaseTest {

  private static final Instant CREATED_AT = Instant.parse("2025-01-01T00:00:00Z");
  private static final Instant UPDATED_AT = Instant.parse("2025-01-15T10:00:00Z");
  private static final String TENANT_ID = "tenant-alpha";

  @Mock private CustomerRepository customerRepository;
  @Mock private Clock clock;

  private UpdateCustomerUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new UpdateCustomerUseCase(customerRepository, clock);
  }

  private Customer existingCustomer() {
    return Customer.create("cust-001", "Old Name",
        new Email("old@test.com"), null,
        new TenantId(TENANT_ID), CREATED_AT);
  }

  @Test
  void should_updateCustomer_when_emailUnchanged() {
    Customer customer = existingCustomer();
    when(customerRepository.findById("cust-001", TENANT_ID)).thenReturn(Optional.of(customer));
    when(clock.now()).thenReturn(UPDATED_AT);
    when(customerRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    var command = new UpdateCustomerCommand(
        "cust-001", "New Name", "old@test.com",
        null, null, null, null, null, null, null, TENANT_ID);

    Customer result = useCase.execute(command);

    assertThat(result.getName()).isEqualTo("New Name");
    assertThat(result.getEmail().getValue()).isEqualTo("old@test.com");
    verify(customerRepository).save(any());
  }

  @Test
  void should_updateCustomer_when_emailChangedToNew() {
    Customer customer = existingCustomer();
    when(customerRepository.findById("cust-001", TENANT_ID)).thenReturn(Optional.of(customer));
    when(customerRepository.existsByEmailAndTenantId("new@test.com", TENANT_ID)).thenReturn(false);
    when(clock.now()).thenReturn(UPDATED_AT);
    when(customerRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    var command = new UpdateCustomerCommand(
        "cust-001", "New Name", "new@test.com",
        null, null, null, null, null, null, null, TENANT_ID);

    Customer result = useCase.execute(command);

    assertThat(result.getEmail().getValue()).isEqualTo("new@test.com");
  }

  @Test
  void should_throwDuplicate_when_newEmailExistsForOtherCustomer() {
    Customer customer = existingCustomer();
    when(customerRepository.findById("cust-001", TENANT_ID)).thenReturn(Optional.of(customer));
    when(customerRepository.existsByEmailAndTenantId("taken@test.com", TENANT_ID)).thenReturn(true);

    var command = new UpdateCustomerCommand(
        "cust-001", "Name", "taken@test.com",
        null, null, null, null, null, null, null, TENANT_ID);

    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(DuplicateResourceException.class)
        .hasMessageContaining("taken@test.com");
    verify(customerRepository, never()).save(any());
  }

  @Test
  void should_throwResourceNotFound_when_customerDoesNotExist() {
    when(customerRepository.findById("nonexistent", TENANT_ID)).thenReturn(Optional.empty());

    var command = new UpdateCustomerCommand(
        "nonexistent", "Name", "email@test.com",
        null, null, null, null, null, null, null, TENANT_ID);

    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void should_throwNullPointer_when_commandIsNull() {
    assertThatThrownBy(() -> useCase.execute(null))
        .isInstanceOf(NullPointerException.class);
  }
}
