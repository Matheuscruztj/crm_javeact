package com.atlasops.tenants.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlasops.shared.domain.exceptions.ResourceNotFoundException;
import com.atlasops.shared.domain.ports.Clock;
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
class DeactivateTenantUseCaseTest {

  private static final String TENANT_ID = "tenant-001";
  private static final Instant CREATED_AT = Instant.parse("2025-01-10T08:00:00Z");
  private static final Instant DEACTIVATION_TIME = Instant.parse("2025-01-15T10:30:00Z");

  @Mock private TenantRepository tenantRepository;

  @Mock private Clock clock;

  @InjectMocks private DeactivateTenantUseCase useCase;

  @Test
  void should_deactivateTenant_when_tenantExists() {
    // Arrange
    Tenant tenant = Tenant.create(TENANT_ID, new TenantName("Acme Corp"), CREATED_AT);
    when(tenantRepository.findById(TENANT_ID)).thenReturn(Optional.of(tenant));
    when(clock.now()).thenReturn(DEACTIVATION_TIME);
    when(tenantRepository.save(any(Tenant.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    Tenant result = useCase.execute(TENANT_ID);

    // Assert
    assertThat(result.getStatus()).isEqualTo(TenantStatus.INACTIVE);
    assertThat(result.getUpdatedAt()).isEqualTo(DEACTIVATION_TIME);
    verify(tenantRepository).save(tenant);
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
