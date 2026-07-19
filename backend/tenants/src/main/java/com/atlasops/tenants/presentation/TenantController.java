package com.atlasops.tenants.presentation;

import com.atlasops.tenants.application.CreateTenantUseCase;
import com.atlasops.tenants.application.DeactivateTenantUseCase;
import com.atlasops.tenants.application.GetTenantUseCase;
import com.atlasops.tenants.domain.Tenant;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for tenant management operations.
 *
 * <p>Endpoints:
 *
 * <ul>
 *   <li>POST /api/v1/tenants — creates a new tenant (ADMIN only)
 *   <li>GET /api/v1/tenants/{id} — retrieves a tenant by ID
 *   <li>PATCH /api/v1/tenants/{id}/deactivate — deactivates a tenant (ADMIN only)
 * </ul>
 *
 * <p>Validates: Requirements 3.1, 3.4, 3.5, 3.6
 */
@RestController
@RequestMapping("/api/v1/tenants")
public class TenantController {

  private final CreateTenantUseCase createTenantUseCase;
  private final GetTenantUseCase getTenantUseCase;
  private final DeactivateTenantUseCase deactivateTenantUseCase;

  public TenantController(
      CreateTenantUseCase createTenantUseCase,
      GetTenantUseCase getTenantUseCase,
      DeactivateTenantUseCase deactivateTenantUseCase) {
    this.createTenantUseCase = createTenantUseCase;
    this.getTenantUseCase = getTenantUseCase;
    this.deactivateTenantUseCase = deactivateTenantUseCase;
  }

  /**
   * Creates a new tenant with the given name.
   *
   * @param request the create tenant request containing the name
   * @return 201 Created with the tenant representation and Location header
   */
  @PostMapping
  public ResponseEntity<TenantResponse> create(@Valid @RequestBody CreateTenantRequest request) {
    Tenant tenant = createTenantUseCase.execute(request.name());
    TenantResponse response = TenantResponse.from(tenant);
    URI location = URI.create("/api/v1/tenants/" + tenant.getId());
    return ResponseEntity.created(location).body(response);
  }

  /**
   * Retrieves a tenant by its identifier.
   *
   * @param id the tenant identifier
   * @return 200 OK with the tenant representation
   */
  @GetMapping("/{id}")
  public ResponseEntity<TenantResponse> getById(@PathVariable String id) {
    Tenant tenant = getTenantUseCase.execute(id);
    TenantResponse response = TenantResponse.from(tenant);
    return ResponseEntity.ok(response);
  }

  /**
   * Deactivates an existing tenant, setting its status to INACTIVE.
   *
   * @param id the tenant identifier
   * @return 200 OK with the updated tenant representation
   */
  @PatchMapping("/{id}/deactivate")
  public ResponseEntity<TenantResponse> deactivate(@PathVariable String id) {
    Tenant tenant = deactivateTenantUseCase.execute(id);
    TenantResponse response = TenantResponse.from(tenant);
    return ResponseEntity.ok(response);
  }
}
