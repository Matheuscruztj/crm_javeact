package com.atlasops.tenants.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.atlasops.shared.domain.exceptions.ResourceNotFoundException;
import com.atlasops.tenants.domain.Tenant;
import com.atlasops.tenants.domain.TenantName;
import com.atlasops.tenants.domain.TenantStatus;
import com.atlasops.tenants.domain.ports.TenantRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetTenantUseCaseTest {

  private static final String TENANT_ID = "tenant-001";
  private static final Instant FIXED_NOW = Instant.parse("2025-01-15T10:30:00Z");

  @Mock private TenantRepository tenantRepository;

  @InjectMocks private GetTenantUseCase useCase;

  @Test
  void should_returnTenant_when_tenantExists() {
    // Arrange
    Tenant tenant = Tenant.create(TENANT_ID, new TenantName("Acme Corp"), FIXED_NOW);
    when(tenantRepository.findById(TENANT_ID)).thenReturn(Optional.of(tenant));

    // Act
    Tenant result = useCase.execute(TENANT_ID);

    // Assert
    assertThat(result.getId()).isEqualTo(TENANT_ID);
    assertThat(result.getName().getValue()).isEqualTo("Acme Corp");
    assertThat(result.getStatus()).isEqualTo(TenantStatus.ACTIVE);
  }

  @Test
  void should_throwResourceNotFoundException_when_tenantDoesNotExist() {
    // Arrange
    when(tenantRepository.findById(TENANT_ID)).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> useCase.execute(TENANT_ID))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining(TENANT_ID);
  }
}
