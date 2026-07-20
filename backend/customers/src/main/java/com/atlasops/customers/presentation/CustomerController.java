package com.atlasops.customers.presentation;

import com.atlasops.customers.application.ActivateCustomerUseCase;
import com.atlasops.customers.application.AssociateClientUserUseCase;
import com.atlasops.customers.application.CreateCustomerCommand;
import com.atlasops.customers.application.CreateCustomerUseCase;
import com.atlasops.customers.application.DeactivateCustomerUseCase;
import com.atlasops.customers.application.FindCustomersByRadiusUseCase;
import com.atlasops.customers.application.GetCustomerUseCase;
import com.atlasops.customers.application.ListCustomersUseCase;
import com.atlasops.customers.application.SearchCustomersUseCase;
import com.atlasops.customers.application.UpdateCustomerCommand;
import com.atlasops.customers.application.UpdateCustomerUseCase;
import com.atlasops.customers.domain.Customer;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for customer management operations.
 *
 * <p>Endpoints:
 *
 * <ul>
 *   <li>POST /api/v1/customers — creates a new customer
 *   <li>GET /api/v1/customers — lists customers with pagination
 *   <li>GET /api/v1/customers/{id} — retrieves a customer by ID
 *   <li>PUT /api/v1/customers/{id} — updates a customer
 *   <li>PATCH /api/v1/customers/{id}/deactivate — deactivates a customer
 *   <li>GET /api/v1/customers/search — searches customers by name/email
 *   <li>GET /api/v1/customers/nearby — finds customers within radius
 *   <li>POST /api/v1/customers/{id}/associate — associates a CLIENT user
 * </ul>
 *
 * <p>Validates: Requirements 4.1, 4.2, 4.3, 4.5, 6.1, 6.9, 6.10, 7.2
 */
@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

  private final CreateCustomerUseCase createCustomerUseCase;
  private final GetCustomerUseCase getCustomerUseCase;
  private final ListCustomersUseCase listCustomersUseCase;
  private final UpdateCustomerUseCase updateCustomerUseCase;
  private final DeactivateCustomerUseCase deactivateCustomerUseCase;
  private final ActivateCustomerUseCase activateCustomerUseCase;
  private final SearchCustomersUseCase searchCustomersUseCase;
  private final FindCustomersByRadiusUseCase findCustomersByRadiusUseCase;
  private final AssociateClientUserUseCase associateClientUserUseCase;

  public CustomerController(
      CreateCustomerUseCase createCustomerUseCase,
      GetCustomerUseCase getCustomerUseCase,
      ListCustomersUseCase listCustomersUseCase,
      UpdateCustomerUseCase updateCustomerUseCase,
      DeactivateCustomerUseCase deactivateCustomerUseCase,
      ActivateCustomerUseCase activateCustomerUseCase,
      SearchCustomersUseCase searchCustomersUseCase,
      FindCustomersByRadiusUseCase findCustomersByRadiusUseCase,
      AssociateClientUserUseCase associateClientUserUseCase) {
    this.createCustomerUseCase = createCustomerUseCase;
    this.getCustomerUseCase = getCustomerUseCase;
    this.listCustomersUseCase = listCustomersUseCase;
    this.updateCustomerUseCase = updateCustomerUseCase;
    this.deactivateCustomerUseCase = deactivateCustomerUseCase;
    this.activateCustomerUseCase = activateCustomerUseCase;
    this.searchCustomersUseCase = searchCustomersUseCase;
    this.findCustomersByRadiusUseCase = findCustomersByRadiusUseCase;
    this.associateClientUserUseCase = associateClientUserUseCase;
  }

  /**
   * Creates a new customer.
   *
   * @param tenantId the tenant identifier from header
   * @param request the create customer request body
   * @return 201 Created with the customer representation and Location header
   */
  @PostMapping
  public ResponseEntity<CustomerResponse> create(
      @RequestHeader("X-Tenant-ID") String tenantId,
      @RequestHeader(value = "X-User-ID", required = false, defaultValue = "system") String actorId,
      @Valid @RequestBody CreateCustomerRequest request) {

    CreateCustomerCommand command =
        new CreateCustomerCommand(
            request.name(),
            request.email(),
            request.street(),
            request.city(),
            request.state(),
            request.postalCode(),
            request.country(),
            request.latitude(),
            request.longitude(),
            tenantId,
            actorId);

    Customer created = createCustomerUseCase.execute(command);
    CustomerResponse response = CustomerResponse.from(created);
    URI location = URI.create("/api/v1/customers/" + created.getId());
    return ResponseEntity.created(location).body(response);
  }

  /**
   * Lists customers with pagination.
   *
   * @param tenantId the tenant identifier from header
   * @param page page number (zero-based, defaults to 0)
   * @param size page size (defaults to 20, max 100)
   * @return 200 OK with paginated response
   */
  @GetMapping
  public ResponseEntity<PageResponse<CustomerResponse>> list(
      @RequestHeader("X-Tenant-ID") String tenantId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {

    Page<Customer> result = listCustomersUseCase.execute(tenantId, page, size);
    return ResponseEntity.ok(toPageResponse(result));
  }

  /**
   * Retrieves a customer by ID.
   *
   * @param tenantId the tenant identifier from header
   * @param id the customer identifier
   * @return 200 OK with the customer representation
   */
  @GetMapping("/{id}")
  public ResponseEntity<CustomerResponse> getById(
      @RequestHeader("X-Tenant-ID") String tenantId, @PathVariable String id) {

    Customer customer = getCustomerUseCase.execute(id, tenantId);
    return ResponseEntity.ok(CustomerResponse.from(customer));
  }

  /**
   * Updates a customer.
   *
   * @param tenantId the tenant identifier from header
   * @param id the customer identifier
   * @param request the update customer request body
   * @return 200 OK with the updated customer representation
   */
  @PutMapping("/{id}")
  public ResponseEntity<CustomerResponse> update(
      @RequestHeader("X-Tenant-ID") String tenantId,
      @PathVariable String id,
      @Valid @RequestBody UpdateCustomerRequest request) {

    UpdateCustomerCommand command =
        new UpdateCustomerCommand(
            id,
            request.name(),
            request.email(),
            request.street(),
            request.city(),
            request.state(),
            request.postalCode(),
            request.country(),
            request.latitude(),
            request.longitude(),
            tenantId);

    Customer updated = updateCustomerUseCase.execute(command);
    return ResponseEntity.ok(CustomerResponse.from(updated));
  }

  /**
   * Deactivates a customer.
   *
   * @param tenantId the tenant identifier from header
   * @param id the customer identifier
   * @return 200 OK with the deactivated customer representation
   */
  @PatchMapping("/{id}/deactivate")
  public ResponseEntity<CustomerResponse> deactivate(
      @RequestHeader("X-Tenant-ID") String tenantId, @PathVariable String id) {

    Customer deactivated = deactivateCustomerUseCase.execute(id, tenantId);
    return ResponseEntity.ok(CustomerResponse.from(deactivated));
  }

  /**
   * Activates a previously deactivated customer.
   *
   * @param tenantId the tenant identifier from header
   * @param id the customer identifier
   * @return 200 OK with the activated customer representation
   */
  @PatchMapping("/{id}/activate")
  public ResponseEntity<CustomerResponse> activate(
      @RequestHeader("X-Tenant-ID") String tenantId, @PathVariable String id) {

    Customer activated = activateCustomerUseCase.execute(id, tenantId);
    return ResponseEntity.ok(CustomerResponse.from(activated));
  }

  /**
   * Searches customers by name or email with partial match.
   *
   * @param tenantId the tenant identifier from header
   * @param q the search query (minimum 2 characters)
   * @param page page number (zero-based, defaults to 0)
   * @param size page size (defaults to 20, max 100)
   * @return 200 OK with paginated response
   */
  @GetMapping("/search")
  public ResponseEntity<PageResponse<CustomerResponse>> search(
      @RequestHeader("X-Tenant-ID") String tenantId,
      @RequestParam String q,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {

    Page<Customer> result = searchCustomersUseCase.execute(q, tenantId, page, size);
    return ResponseEntity.ok(toPageResponse(result));
  }

  /**
   * Finds customers within a specified radius from center coordinates.
   *
   * @param tenantId the tenant identifier from header
   * @param latitude center point latitude
   * @param longitude center point longitude
   * @param radiusKm radius in kilometers
   * @param page page number (zero-based, defaults to 0)
   * @param size page size (defaults to 20, max 100)
   * @return 200 OK with paginated response
   */
  @GetMapping("/nearby")
  public ResponseEntity<PageResponse<CustomerResponse>> nearby(
      @RequestHeader("X-Tenant-ID") String tenantId,
      @RequestParam double latitude,
      @RequestParam double longitude,
      @RequestParam double radiusKm,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {

    int effectiveSize = Math.min(Math.max(size, 1), 100);
    int effectivePage = Math.max(page, 0);

    Page<Customer> result =
        findCustomersByRadiusUseCase.execute(
            latitude, longitude, radiusKm, tenantId, PageRequest.of(effectivePage, effectiveSize));

    return ResponseEntity.ok(toPageResponse(result));
  }

  /**
   * Associates a CLIENT user with a customer.
   *
   * @param tenantId the tenant identifier from header
   * @param id the customer identifier
   * @param request the association request body
   * @return 204 No Content
   */
  @PostMapping("/{id}/users")
  public ResponseEntity<Void> associate(
      @RequestHeader("X-Tenant-ID") String tenantId,
      @PathVariable String id,
      @Valid @RequestBody AssociateClientUserRequest request) {

    var command =
        new AssociateClientUserUseCase.AssociateClientUserCommand(request.userId(), id, tenantId);
    associateClientUserUseCase.execute(command);

    return ResponseEntity.noContent().build();
  }

  /**
   * @deprecated Use POST /{id}/users instead. Kept for backward compatibility.
   */
  @Deprecated(forRemoval = true)
  @PostMapping("/{id}/associate")
  public ResponseEntity<Void> associateLegacy(
      @RequestHeader("X-Tenant-ID") String tenantId,
      @PathVariable String id,
      @Valid @RequestBody AssociateClientUserRequest request) {
    return associate(tenantId, id, request);
  }

  /**
   * Removes the association between a CLIENT user and a customer.
   *
   * @param tenantId the tenant identifier from header
   * @param id the customer identifier
   * @param userId the user identifier to dissociate
   * @return 204 No Content
   */
  @DeleteMapping("/{id}/users/{userId}")
  public ResponseEntity<Void> dissociate(
      @RequestHeader("X-Tenant-ID") String tenantId,
      @PathVariable String id,
      @PathVariable String userId) {

    associateClientUserUseCase.dissociate(userId, id, tenantId);
    return ResponseEntity.noContent().build();
  }

  private PageResponse<CustomerResponse> toPageResponse(Page<Customer> page) {
    List<CustomerResponse> content =
        page.getContent().stream().map(CustomerResponse::from).toList();

    var pageMetadata =
        new PageResponse.PageMetadata(
            page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());

    return new PageResponse<>(content, pageMetadata);
  }
}
