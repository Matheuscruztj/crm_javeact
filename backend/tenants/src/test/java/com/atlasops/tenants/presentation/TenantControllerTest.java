package com.atlasops.tenants.presentation;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.atlasops.shared.domain.exceptions.DuplicateResourceException;
import com.atlasops.shared.domain.exceptions.ResourceNotFoundException;
import com.atlasops.tenants.application.CreateTenantUseCase;
import com.atlasops.tenants.application.DeactivateTenantUseCase;
import com.atlasops.tenants.application.GetTenantUseCase;
import com.atlasops.tenants.domain.Tenant;
import com.atlasops.tenants.domain.TenantName;
import com.atlasops.tenants.domain.TenantStatus;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@ExtendWith(MockitoExtension.class)
@DisplayName("TenantController")
class TenantControllerTest {

  private static final String TENANT_ID = "tenant-001";
  private static final String TENANT_NAME = "Acme Corp";
  private static final Instant FIXED_NOW = Instant.parse("2025-01-15T10:00:00Z");

  private MockMvc mockMvc;

  @Mock private CreateTenantUseCase createTenantUseCase;

  @Mock private GetTenantUseCase getTenantUseCase;

  @Mock private DeactivateTenantUseCase deactivateTenantUseCase;

  @InjectMocks private TenantController tenantController;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(tenantController)
            .setControllerAdvice(new TestExceptionHandler())
            .build();
  }

  @Test
  void should_createTenant_when_validNameProvided() throws Exception {
    Tenant tenant =
        Tenant.reconstitute(
            TENANT_ID, new TenantName(TENANT_NAME), TenantStatus.ACTIVE, FIXED_NOW, FIXED_NOW);
    when(createTenantUseCase.execute(TENANT_NAME)).thenReturn(tenant);

    mockMvc
        .perform(
            post("/api/v1/tenants")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Acme Corp\"}"))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "/api/v1/tenants/" + TENANT_ID))
        .andExpect(jsonPath("$.id").value(TENANT_ID))
        .andExpect(jsonPath("$.name").value(TENANT_NAME))
        .andExpect(jsonPath("$.status").value("ACTIVE"));
  }

  @Test
  void should_return409_when_tenantNameAlreadyExists() throws Exception {
    when(createTenantUseCase.execute(TENANT_NAME))
        .thenThrow(new DuplicateResourceException("Tenant with name 'Acme Corp' already exists"));

    mockMvc
        .perform(
            post("/api/v1/tenants")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Acme Corp\"}"))
        .andExpect(status().isConflict());
  }

  @Test
  void should_return400_when_nameIsBlank() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/tenants")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void should_return400_when_nameIsMissing() throws Exception {
    mockMvc
        .perform(post("/api/v1/tenants").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void should_returnTenant_when_validIdProvided() throws Exception {
    Tenant tenant =
        Tenant.reconstitute(
            TENANT_ID, new TenantName(TENANT_NAME), TenantStatus.ACTIVE, FIXED_NOW, FIXED_NOW);
    when(getTenantUseCase.execute(TENANT_ID)).thenReturn(tenant);

    mockMvc
        .perform(get("/api/v1/tenants/" + TENANT_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(TENANT_ID))
        .andExpect(jsonPath("$.name").value(TENANT_NAME))
        .andExpect(jsonPath("$.status").value("ACTIVE"))
        .andExpect(jsonPath("$.createdAt").exists())
        .andExpect(jsonPath("$.updatedAt").exists());
  }

  @Test
  void should_return404_when_tenantNotFound() throws Exception {
    when(getTenantUseCase.execute("nonexistent"))
        .thenThrow(new ResourceNotFoundException("Tenant with id 'nonexistent' not found"));

    mockMvc.perform(get("/api/v1/tenants/nonexistent")).andExpect(status().isNotFound());
  }

  @Test
  void should_deactivateTenant_when_validIdProvided() throws Exception {
    Tenant tenant =
        Tenant.reconstitute(
            TENANT_ID, new TenantName(TENANT_NAME), TenantStatus.INACTIVE, FIXED_NOW, FIXED_NOW);
    when(deactivateTenantUseCase.execute(TENANT_ID)).thenReturn(tenant);

    mockMvc
        .perform(patch("/api/v1/tenants/" + TENANT_ID + "/deactivate"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(TENANT_ID))
        .andExpect(jsonPath("$.status").value("INACTIVE"));
  }

  @Test
  void should_return404_when_deactivatingNonexistentTenant() throws Exception {
    when(deactivateTenantUseCase.execute("nonexistent"))
        .thenThrow(new ResourceNotFoundException("Tenant with id 'nonexistent' not found"));

    mockMvc
        .perform(patch("/api/v1/tenants/nonexistent/deactivate"))
        .andExpect(status().isNotFound());
  }

  /**
   * Minimal exception handler for standalone MockMvc tests, mirroring the GlobalExceptionHandler in
   * app-boot.
   */
  @RestControllerAdvice
  static class TestExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleNotFound(ResourceNotFoundException ex) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(java.util.Map.of("status", 404, "detail", ex.getMessage()));
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Object> handleDuplicate(DuplicateResourceException ex) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(java.util.Map.of("status", 409, "detail", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleBadRequest(IllegalArgumentException ex) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(java.util.Map.of("status", 400, "detail", ex.getMessage()));
    }

    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidation(
        org.springframework.web.bind.MethodArgumentNotValidException ex) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(java.util.Map.of("status", 400, "detail", "Validation failed"));
    }
  }
}
