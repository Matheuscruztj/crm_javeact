package com.atlasops.tenants.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlasops.shared.domain.exceptions.DuplicateResourceException;
import com.atlasops.shared.domain.ports.Clock;
import com.atlasops.shared.domain.ports.IdGenerator;
import com.atlasops.tenants.domain.Tenant;
import com.atlasops.tenants.domain.TenantStatus;
import com.atlasops.tenants.domain.ports.TenantRepository;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateTenantUseCaseTest {

  private static final String GENERATED_ID = "tenant-001";
  private static final Instant FIXED_NOW = Instant.parse("2025-01-15T10:30:00Z");

  @Mock private TenantRepository tenantRepository;

  @Mock private IdGenerator idGenerator;

  @Mock private Clock clock;

  @InjectMocks private CreateTenantUseCase useCase;

  @Test
  void should_createTenant_when_nameIsUniqueAndValid() {
    // Arrange
    String name = "Acme Corp";
    when(tenantRepository.existsByNameIgnoreCase(name)).thenReturn(false);
    when(idGenerator.generate()).thenReturn(GENERATED_ID);
    when(clock.now()).thenReturn(FIXED_NOW);
    when(tenantRepository.save(any(Tenant.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    Tenant result = useCase.execute(name);

    // Assert
    assertThat(result.getId()).isEqualTo(GENERATED_ID);
    assertThat(result.getName().getValue()).isEqualTo(name);
    assertThat(result.getStatus()).isEqualTo(TenantStatus.ACTIVE);
    assertThat(result.getCreatedAt()).isEqualTo(FIXED_NOW);
    verify(tenantRepository).save(any(Tenant.class));
  }

  @Test
  void should_throwDuplicateResourceException_when_nameAlreadyExists() {
    // Arrange
    String name = "Acme Corp";
    when(tenantRepository.existsByNameIgnoreCase(name)).thenReturn(true);

    // Act & Assert
    assertThatThrownBy(() -> useCase.execute(name))
        .isInstanceOf(DuplicateResourceException.class)
        .hasMessageContaining("Acme Corp");

    verify(tenantRepository, never()).save(any());
  }

  @Test
  void should_throwIllegalArgumentException_when_nameIsInvalid() {
    // Act & Assert — name too short
    assertThatThrownBy(() -> useCase.execute("AB")).isInstanceOf(IllegalArgumentException.class);

    verify(tenantRepository, never()).save(any());
  }

  @Test
  void should_trimNameBeforeValidation_when_nameHasWhitespace() {
    // Arrange
    String nameWithSpaces = "   Acme Corp   ";
    String trimmedName = "Acme Corp";
    when(tenantRepository.existsByNameIgnoreCase(trimmedName)).thenReturn(false);
    when(idGenerator.generate()).thenReturn(GENERATED_ID);
    when(clock.now()).thenReturn(FIXED_NOW);
    when(tenantRepository.save(any(Tenant.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    Tenant result = useCase.execute(nameWithSpaces);

    // Assert
    assertThat(result.getName().getValue()).isEqualTo(trimmedName);
  }

  @Test
  void should_enforceCaseInsensitiveUniqueness_when_nameExistsInDifferentCase() {
    // Arrange
    String name = "Acme Corp";
    when(tenantRepository.existsByNameIgnoreCase(name)).thenReturn(true);

    // Act & Assert
    assertThatThrownBy(() -> useCase.execute(name)).isInstanceOf(DuplicateResourceException.class);
  }
}
