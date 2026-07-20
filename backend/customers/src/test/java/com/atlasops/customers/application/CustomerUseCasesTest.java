package com.atlasops.customers.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlasops.customers.domain.Customer;
import com.atlasops.customers.domain.CustomerStatus;
import com.atlasops.customers.domain.ports.CustomerRepository;
import com.atlasops.customers.domain.ports.UserCustomerAssociationRepository;
import com.atlasops.shared.domain.exceptions.BusinessRuleViolationException;
import com.atlasops.shared.domain.exceptions.DuplicateResourceException;
import com.atlasops.shared.domain.exceptions.ResourceNotFoundException;
import com.atlasops.shared.domain.ports.Clock;
import com.atlasops.shared.domain.types.Email;
import com.atlasops.shared.domain.types.TenantId;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

/**
 * Unit tests for customer use cases: Get, Activate, Deactivate, List, AssociateClient.
 */
@ExtendWith(MockitoExtension.class)
class CustomerUseCasesTest {

  private static final Instant NOW = Instant.parse("2025-01-15T10:00:00Z");
  private static final String TENANT = "tenant-alpha";
  private static final String CUSTOMER_ID = "cust-001";

  @Mock private CustomerRepository customerRepository;
  @Mock private UserCustomerAssociationRepository associationRepository;
  @Mock private Clock clock;

  private Customer activeCustomer() {
    return Customer.create(CUSTOMER_ID, "Alpha Corp",
        new Email("alpha@corp.com"), null, new TenantId(TENANT), NOW);
  }

  // ─── GetCustomerUseCase ────────────────────────────────────────────────────

  @Nested
  class GetCustomer {
    GetCustomerUseCase useCase;
    @BeforeEach void init() { useCase = new GetCustomerUseCase(customerRepository); }

    @Test
    void should_returnCustomer_when_found() {
      when(customerRepository.findById(CUSTOMER_ID, TENANT)).thenReturn(Optional.of(activeCustomer()));
      Customer result = useCase.execute(CUSTOMER_ID, TENANT);
      assertThat(result.getId()).isEqualTo(CUSTOMER_ID);
    }

    @Test
    void should_throwNotFound_when_missing() {
      when(customerRepository.findById(anyString(), anyString())).thenReturn(Optional.empty());
      assertThatThrownBy(() -> useCase.execute("x", TENANT))
          .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void should_throwNullPointer_when_idIsNull() {
      assertThatThrownBy(() -> useCase.execute(null, TENANT))
          .isInstanceOf(NullPointerException.class);
    }
  }

  // ─── ActivateCustomerUseCase ───────────────────────────────────────────────

  @Nested
  class ActivateCustomer {
    ActivateCustomerUseCase useCase;
    @BeforeEach void init() { useCase = new ActivateCustomerUseCase(customerRepository, clock); }

    @Test
    void should_activateCustomer_when_inactive() {
      Customer c = activeCustomer();
      c.deactivate(NOW); // make inactive first
      when(customerRepository.findById(CUSTOMER_ID, TENANT)).thenReturn(Optional.of(c));
      when(clock.now()).thenReturn(NOW.plusSeconds(60));
      when(customerRepository.save(any())).thenAnswer(i -> i.getArgument(0));

      Customer result = useCase.execute(CUSTOMER_ID, TENANT);
      assertThat(result.getStatus()).isEqualTo(CustomerStatus.ACTIVE);
    }

    @Test
    void should_throwBusinessRule_when_alreadyActive() {
      when(customerRepository.findById(CUSTOMER_ID, TENANT)).thenReturn(Optional.of(activeCustomer()));
      when(clock.now()).thenReturn(NOW.plusSeconds(60));

      assertThatThrownBy(() -> useCase.execute(CUSTOMER_ID, TENANT))
          .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void should_throwNotFound_when_missing() {
      when(customerRepository.findById(anyString(), anyString())).thenReturn(Optional.empty());
      assertThatThrownBy(() -> useCase.execute("x", TENANT))
          .isInstanceOf(ResourceNotFoundException.class);
    }
  }

  // ─── DeactivateCustomerUseCase ─────────────────────────────────────────────

  @Nested
  class DeactivateCustomer {
    DeactivateCustomerUseCase useCase;
    @BeforeEach void init() { useCase = new DeactivateCustomerUseCase(customerRepository, clock); }

    @Test
    void should_deactivateCustomer_when_active() {
      when(customerRepository.findById(CUSTOMER_ID, TENANT)).thenReturn(Optional.of(activeCustomer()));
      when(clock.now()).thenReturn(NOW.plusSeconds(60));
      when(customerRepository.save(any())).thenAnswer(i -> i.getArgument(0));

      Customer result = useCase.execute(CUSTOMER_ID, TENANT);
      assertThat(result.getStatus()).isEqualTo(CustomerStatus.INACTIVE);
    }

    @Test
    void should_throwBusinessRule_when_alreadyInactive() {
      Customer c = activeCustomer();
      c.deactivate(NOW);
      when(customerRepository.findById(CUSTOMER_ID, TENANT)).thenReturn(Optional.of(c));
      when(clock.now()).thenReturn(NOW.plusSeconds(60));

      assertThatThrownBy(() -> useCase.execute(CUSTOMER_ID, TENANT))
          .isInstanceOf(BusinessRuleViolationException.class);
    }
  }

  // ─── ListCustomersUseCase ─────────────────────────────────────────────────

  @Nested
  class ListCustomers {
    ListCustomersUseCase useCase;
    @BeforeEach void init() { useCase = new ListCustomersUseCase(customerRepository); }

    @Test
    void should_returnPage_when_tenantHasCustomers() {
      var page = new PageImpl<>(List.of(activeCustomer()), PageRequest.of(0, 20), 1);
      when(customerRepository.findByTenantId(TENANT, PageRequest.of(0, 20))).thenReturn(page);

      var result = useCase.execute(TENANT, 0, 20);
      assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void should_capPageSizeAt100_when_sizeExceeds() {
      when(customerRepository.findByTenantId(anyString(), any())).thenReturn(new PageImpl<>(List.of()));
      useCase.execute(TENANT, 0, 500);
      verify(customerRepository).findByTenantId(TENANT, PageRequest.of(0, 100));
    }

    @Test
    void should_resetPageToZero_when_negative() {
      when(customerRepository.findByTenantId(anyString(), any())).thenReturn(new PageImpl<>(List.of()));
      useCase.execute(TENANT, -1, 20);
      verify(customerRepository).findByTenantId(TENANT, PageRequest.of(0, 20));
    }
  }

  // ─── AssociateClientUserUseCase ────────────────────────────────────────────

  @Nested
  class AssociateClientUser {
    AssociateClientUserUseCase useCase;
    @BeforeEach void init() { useCase = new AssociateClientUserUseCase(customerRepository, associationRepository); }

    @Test
    void should_saveAssociation_when_notAlreadyAssociated() {
      when(customerRepository.findById(CUSTOMER_ID, TENANT)).thenReturn(Optional.of(activeCustomer()));
      when(associationRepository.exists("user-001", CUSTOMER_ID)).thenReturn(false);

      useCase.execute(new AssociateClientUserUseCase.AssociateClientUserCommand("user-001", CUSTOMER_ID, TENANT));

      verify(associationRepository).save("user-001", CUSTOMER_ID, TENANT);
    }

    @Test
    void should_throwDuplicate_when_alreadyAssociated() {
      when(customerRepository.findById(CUSTOMER_ID, TENANT)).thenReturn(Optional.of(activeCustomer()));
      when(associationRepository.exists("user-001", CUSTOMER_ID)).thenReturn(true);

      assertThatThrownBy(() -> useCase.execute(
          new AssociateClientUserUseCase.AssociateClientUserCommand("user-001", CUSTOMER_ID, TENANT)))
          .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void should_throwNotFound_when_customerMissing() {
      when(customerRepository.findById(anyString(), anyString())).thenReturn(Optional.empty());

      assertThatThrownBy(() -> useCase.execute(
          new AssociateClientUserUseCase.AssociateClientUserCommand("user-001", "x", TENANT)))
          .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void should_deleteAssociation_when_dissociate() {
      when(customerRepository.findById(CUSTOMER_ID, TENANT)).thenReturn(Optional.of(activeCustomer()));

      useCase.dissociate("user-001", CUSTOMER_ID, TENANT);

      verify(associationRepository).delete("user-001", CUSTOMER_ID);
    }
  }
}
