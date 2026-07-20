package com.atlasops.tenants.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.atlasops.shared.domain.exceptions.ResourceNotFoundException;
import com.atlasops.shared.domain.ports.Clock;
import com.atlasops.tenants.domain.Tenant;
import com.atlasops.tenants.domain.TenantName;
import com.atlasops.tenants.domain.ports.TenantRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateTenantBrandingUseCaseTest {

  private static final Instant NOW = Instant.parse("2025-01-15T10:00:00Z");
  private static final String TENANT_ID = "tenant-001";

  @Mock private TenantRepository tenantRepository;
  @Mock private Clock clock;

  private UpdateTenantBrandingUseCase useCase;

  @BeforeEach
  void setUp() { useCase = new UpdateTenantBrandingUseCase(tenantRepository, clock); }

  private Tenant existingTenant() {
    return Tenant.create(TENANT_ID, new TenantName("Acme Corp"), NOW);
  }

  @Test
  void should_updateBranding_when_validColorAndUrl() {
    when(tenantRepository.findById(TENANT_ID)).thenReturn(Optional.of(existingTenant()));
    when(clock.now()).thenReturn(NOW.plusSeconds(60));
    when(tenantRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    var command = new UpdateTenantBrandingCommand(TENANT_ID, "https://logo.example.com/img.png", "#3B82F6");
    Tenant result = useCase.execute(command);

    assertThat(result.getLogoUrl()).isEqualTo("https://logo.example.com/img.png");
    assertThat(result.getPrimaryColor()).isEqualTo("#3B82F6");
  }

  @Test
  void should_updateBranding_when_onlyUrlProvided() {
    when(tenantRepository.findById(TENANT_ID)).thenReturn(Optional.of(existingTenant()));
    when(clock.now()).thenReturn(NOW.plusSeconds(60));
    when(tenantRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    var command = new UpdateTenantBrandingCommand(TENANT_ID, "https://logo.com/img.png", null);
    Tenant result = useCase.execute(command);

    assertThat(result.getLogoUrl()).isEqualTo("https://logo.com/img.png");
    assertThat(result.getPrimaryColor()).isNull();
  }

  @Test
  void should_throwIllegalArgument_when_colorIsInvalidHex() {
    var command = new UpdateTenantBrandingCommand(TENANT_ID, null, "not-a-color");

    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("primaryColor");
  }

  @Test
  void should_throwIllegalArgument_when_colorMissingHash() {
    var command = new UpdateTenantBrandingCommand(TENANT_ID, null, "3B82F6");

    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void should_throwNotFound_when_tenantMissing() {
    when(tenantRepository.findById(TENANT_ID)).thenReturn(Optional.empty());

    var command = new UpdateTenantBrandingCommand(TENANT_ID, null, null);
    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void should_throwNullPointer_when_commandIsNull() {
    assertThatThrownBy(() -> useCase.execute(null))
        .isInstanceOf(NullPointerException.class);
  }
}
