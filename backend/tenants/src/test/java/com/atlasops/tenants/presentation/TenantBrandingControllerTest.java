package com.atlasops.tenants.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.atlasops.tenants.application.UpdateTenantBrandingUseCase;
import com.atlasops.tenants.domain.Tenant;
import com.atlasops.tenants.domain.TenantName;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Unit tests for TenantBrandingController.
 * Validates: P2.12 — Tenant branding endpoint
 */
@ExtendWith(MockitoExtension.class)
class TenantBrandingControllerTest {

    private static final Instant NOW = Instant.parse("2025-01-15T10:00:00Z");

    @Mock private UpdateTenantBrandingUseCase updateBrandingUseCase;

    private TenantBrandingController controller;

    @BeforeEach
    void setUp() {
        controller = new TenantBrandingController(updateBrandingUseCase);
    }

    private Tenant aTenant() {
        return Tenant.create("tenant-001", new TenantName("Alpha Corp"), NOW);
    }

    @Test
    void should_updateBranding_when_validRequest() {
        Tenant updated = aTenant();
        when(updateBrandingUseCase.execute(any())).thenReturn(updated);

        var request = new TenantBrandingController.BrandingRequest(
                "https://example.com/logo.png", "#1a73e8");
        ResponseEntity<TenantResponse> response = controller.updateBranding("tenant-001", request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void should_acceptNullFields_when_partialUpdateProvided() {
        Tenant updated = aTenant();
        when(updateBrandingUseCase.execute(any())).thenReturn(updated);

        // Both fields are optional
        var request = new TenantBrandingController.BrandingRequest(null, null);
        ResponseEntity<TenantResponse> response = controller.updateBranding("tenant-001", request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
