package com.atlasops.customers.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.atlasops.customers.application.ActivateCustomerUseCase;
import com.atlasops.customers.application.AssociateClientUserUseCase;
import com.atlasops.customers.application.CreateCustomerUseCase;
import com.atlasops.customers.application.DeactivateCustomerUseCase;
import com.atlasops.customers.application.FindCustomersByRadiusUseCase;
import com.atlasops.customers.application.GetCustomerUseCase;
import com.atlasops.customers.application.ListCustomersUseCase;
import com.atlasops.customers.application.SearchCustomersUseCase;
import com.atlasops.customers.application.UpdateCustomerUseCase;
import com.atlasops.customers.domain.Customer;
import com.atlasops.customers.domain.CustomerStatus;
import com.atlasops.shared.domain.types.Email;
import com.atlasops.shared.domain.types.TenantId;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Unit tests for CustomerController (presentation layer, no Spring context).
 */
@ExtendWith(MockitoExtension.class)
class CustomerControllerTest {

  private static final String TENANT_ID = "tenant-alpha";
  private static final String ACTOR_ID = "user-001";
  private static final Instant NOW = Instant.parse("2025-01-15T10:00:00Z");

  @Mock private CreateCustomerUseCase createCustomerUseCase;
  @Mock private GetCustomerUseCase getCustomerUseCase;
  @Mock private ListCustomersUseCase listCustomersUseCase;
  @Mock private UpdateCustomerUseCase updateCustomerUseCase;
  @Mock private DeactivateCustomerUseCase deactivateCustomerUseCase;
  @Mock private ActivateCustomerUseCase activateCustomerUseCase;
  @Mock private SearchCustomersUseCase searchCustomersUseCase;
  @Mock private FindCustomersByRadiusUseCase findCustomersByRadiusUseCase;
  @Mock private AssociateClientUserUseCase associateClientUserUseCase;

  private CustomerController controller;

  @BeforeEach
  void setUp() {
    controller = new CustomerController(
        createCustomerUseCase, getCustomerUseCase, listCustomersUseCase,
        updateCustomerUseCase, deactivateCustomerUseCase, activateCustomerUseCase,
        searchCustomersUseCase, findCustomersByRadiusUseCase, associateClientUserUseCase);
  }

  private Customer sampleCustomer(String id) {
    return Customer.create(id, "Empresa Alpha",
        new Email("alpha@test.com"), null,
        new TenantId(TENANT_ID), NOW);
  }

  @Test
  void should_return201_when_createSucceeds() {
    Customer created = sampleCustomer("cust-001");
    when(createCustomerUseCase.execute(any())).thenReturn(created);

    var request = new CreateCustomerRequest(
        "Empresa Alpha", "alpha@test.com", null, null, null, null, null, null, null);
    ResponseEntity<CustomerResponse> response = controller.create(TENANT_ID, ACTOR_ID, request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getHeaders().getLocation()).isNotNull();
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().id()).isEqualTo("cust-001");
  }

  @Test
  void should_return200_when_getByIdSucceeds() {
    Customer customer = sampleCustomer("cust-002");
    when(getCustomerUseCase.execute("cust-002", TENANT_ID)).thenReturn(customer);

    ResponseEntity<CustomerResponse> response = controller.getById(TENANT_ID, "cust-002");

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().id()).isEqualTo("cust-002");
  }

  @Test
  void should_return200_when_listSucceeds() {
    when(listCustomersUseCase.execute(TENANT_ID, 0, 20)).thenReturn(Page.empty());

    ResponseEntity<PageResponse<CustomerResponse>> response =
        controller.list(TENANT_ID, 0, 20);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
  }

  @Test
  void should_return200_when_deactivateSucceeds() {
    Customer deactivated = sampleCustomer("cust-003");
    when(deactivateCustomerUseCase.execute("cust-003", TENANT_ID)).thenReturn(deactivated);

    ResponseEntity<CustomerResponse> response = controller.deactivate(TENANT_ID, "cust-003");

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void should_return200_when_activateSucceeds() {
    Customer activated = sampleCustomer("cust-004");
    when(activateCustomerUseCase.execute("cust-004", TENANT_ID)).thenReturn(activated);

    ResponseEntity<CustomerResponse> response = controller.activate(TENANT_ID, "cust-004");

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void should_return200_when_searchSucceeds() {
    when(searchCustomersUseCase.execute(anyString(), anyString(), any(int.class), any(int.class)))
        .thenReturn(Page.empty());

    ResponseEntity<PageResponse<CustomerResponse>> response =
        controller.search(TENANT_ID, "Alpha", 0, 20);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void should_return204_when_associateSucceeds() {
    var request = new AssociateClientUserRequest("user-client-001");
    ResponseEntity<Void> response = controller.associate(TENANT_ID, "cust-001", request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
  }
}
