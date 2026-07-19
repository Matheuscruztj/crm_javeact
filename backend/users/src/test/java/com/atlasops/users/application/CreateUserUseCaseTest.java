package com.atlasops.users.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlasops.shared.domain.exceptions.DuplicateResourceException;
import com.atlasops.shared.domain.ports.Clock;
import com.atlasops.shared.domain.ports.IdGenerator;
import com.atlasops.users.domain.User;
import com.atlasops.users.domain.UserRole;
import com.atlasops.users.domain.UserStatus;
import com.atlasops.users.domain.ports.PasswordHashPort;
import com.atlasops.users.domain.ports.UserRepository;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateUserUseCaseTest {

  private static final String GENERATED_ID = "user-001";
  private static final Instant FIXED_NOW = Instant.parse("2025-01-15T10:30:00Z");
  private static final String HASHED_PASSWORD = "$2a$10$hashedpasswordvalue";
  private static final String TENANT_ID = "tenant-alpha";

  @Mock private UserRepository userRepository;

  @Mock private PasswordHashPort passwordHashPort;

  @Mock private IdGenerator idGenerator;

  @Mock private Clock clock;

  @InjectMocks private CreateUserUseCase useCase;

  @Test
  void should_createUser_when_allFieldsValid() {
    // Arrange
    CreateUserCommand command =
        new CreateUserCommand("user@example.com", "John Doe", "securePass123", "ADMIN", TENANT_ID);
    when(userRepository.existsByEmailAndTenantId("user@example.com", TENANT_ID)).thenReturn(false);
    when(passwordHashPort.hash("securePass123")).thenReturn(HASHED_PASSWORD);
    when(idGenerator.generate()).thenReturn(GENERATED_ID);
    when(clock.now()).thenReturn(FIXED_NOW);
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    User result = useCase.execute(command);

    // Assert
    assertThat(result.getId()).isEqualTo(GENERATED_ID);
    assertThat(result.getEmail()).isEqualTo("user@example.com");
    assertThat(result.getName()).isEqualTo("John Doe");
    assertThat(result.getPasswordHash()).isEqualTo(HASHED_PASSWORD);
    assertThat(result.getRole()).isEqualTo(UserRole.ADMIN);
    assertThat(result.getTenantId()).isEqualTo(TENANT_ID);
    assertThat(result.getStatus()).isEqualTo(UserStatus.ACTIVE);
    assertThat(result.getCreatedAt()).isEqualTo(FIXED_NOW);
    verify(userRepository).save(any(User.class));
  }

  @Test
  void should_normalizeEmailToLowercase_when_emailHasUppercase() {
    // Arrange
    CreateUserCommand command =
        new CreateUserCommand(
            "User@Example.COM", "John Doe", "securePass123", "ANALYST", TENANT_ID);
    when(userRepository.existsByEmailAndTenantId("user@example.com", TENANT_ID)).thenReturn(false);
    when(passwordHashPort.hash("securePass123")).thenReturn(HASHED_PASSWORD);
    when(idGenerator.generate()).thenReturn(GENERATED_ID);
    when(clock.now()).thenReturn(FIXED_NOW);
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    User result = useCase.execute(command);

    // Assert
    assertThat(result.getEmail()).isEqualTo("user@example.com");
  }

  @Test
  void should_throwDuplicateResourceException_when_emailAlreadyExistsInTenant() {
    // Arrange
    CreateUserCommand command =
        new CreateUserCommand("user@example.com", "John Doe", "securePass123", "ADMIN", TENANT_ID);
    when(userRepository.existsByEmailAndTenantId("user@example.com", TENANT_ID)).thenReturn(true);

    // Act & Assert
    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(DuplicateResourceException.class)
        .hasMessageContaining("user@example.com");

    verify(userRepository, never()).save(any());
  }

  @Test
  void should_throwIllegalArgumentException_when_emailIsNull() {
    // Arrange
    CreateUserCommand command =
        new CreateUserCommand(null, "John Doe", "securePass123", "ADMIN", TENANT_ID);

    // Act & Assert
    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Email");

    verify(userRepository, never()).save(any());
  }

  @Test
  void should_throwIllegalArgumentException_when_emailIsInvalidFormat() {
    // Arrange
    CreateUserCommand command =
        new CreateUserCommand("not-an-email", "John Doe", "securePass123", "ADMIN", TENANT_ID);

    // Act & Assert
    assertThatThrownBy(() -> useCase.execute(command)).isInstanceOf(IllegalArgumentException.class);

    verify(userRepository, never()).save(any());
  }

  @Test
  void should_throwIllegalArgumentException_when_nameIsTooShort() {
    // Arrange
    CreateUserCommand command =
        new CreateUserCommand("user@example.com", "A", "securePass123", "ADMIN", TENANT_ID);

    // Act & Assert
    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Name");

    verify(userRepository, never()).save(any());
  }

  @Test
  void should_throwIllegalArgumentException_when_nameIsTooLong() {
    // Arrange
    String longName = "A".repeat(101);
    CreateUserCommand command =
        new CreateUserCommand("user@example.com", longName, "securePass123", "ADMIN", TENANT_ID);

    // Act & Assert
    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Name");

    verify(userRepository, never()).save(any());
  }

  @Test
  void should_throwIllegalArgumentException_when_passwordTooShort() {
    // Arrange
    CreateUserCommand command =
        new CreateUserCommand("user@example.com", "John Doe", "short", "ADMIN", TENANT_ID);

    // Act & Assert
    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Password");

    verify(userRepository, never()).save(any());
  }

  @Test
  void should_throwIllegalArgumentException_when_roleIsInvalid() {
    // Arrange
    CreateUserCommand command =
        new CreateUserCommand(
            "user@example.com", "John Doe", "securePass123", "SUPER_USER", TENANT_ID);

    // Act & Assert
    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Invalid role");

    verify(userRepository, never()).save(any());
  }

  @Test
  void should_throwIllegalArgumentException_when_tenantIdIsNull() {
    // Arrange
    CreateUserCommand command =
        new CreateUserCommand("user@example.com", "John Doe", "securePass123", "ADMIN", null);

    // Act & Assert
    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("TenantId");

    verify(userRepository, never()).save(any());
  }

  @Test
  void should_hashPasswordBeforePersisting_when_creatingUser() {
    // Arrange
    CreateUserCommand command =
        new CreateUserCommand("user@example.com", "John Doe", "myPassword99", "CLIENT", TENANT_ID);
    when(userRepository.existsByEmailAndTenantId("user@example.com", TENANT_ID)).thenReturn(false);
    when(passwordHashPort.hash("myPassword99")).thenReturn(HASHED_PASSWORD);
    when(idGenerator.generate()).thenReturn(GENERATED_ID);
    when(clock.now()).thenReturn(FIXED_NOW);
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    User result = useCase.execute(command);

    // Assert
    assertThat(result.getPasswordHash()).isEqualTo(HASHED_PASSWORD);
    verify(passwordHashPort).hash("myPassword99");
  }

  @Test
  void should_acceptRoleCaseInsensitive_when_roleIsLowercase() {
    // Arrange
    CreateUserCommand command =
        new CreateUserCommand(
            "user@example.com", "John Doe", "securePass123", "analyst", TENANT_ID);
    when(userRepository.existsByEmailAndTenantId("user@example.com", TENANT_ID)).thenReturn(false);
    when(passwordHashPort.hash("securePass123")).thenReturn(HASHED_PASSWORD);
    when(idGenerator.generate()).thenReturn(GENERATED_ID);
    when(clock.now()).thenReturn(FIXED_NOW);
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    User result = useCase.execute(command);

    // Assert
    assertThat(result.getRole()).isEqualTo(UserRole.ANALYST);
  }

  @Test
  void should_trimName_when_nameHasLeadingOrTrailingSpaces() {
    // Arrange
    CreateUserCommand command =
        new CreateUserCommand(
            "user@example.com", "  John Doe  ", "securePass123", "ADMIN", TENANT_ID);
    when(userRepository.existsByEmailAndTenantId("user@example.com", TENANT_ID)).thenReturn(false);
    when(passwordHashPort.hash("securePass123")).thenReturn(HASHED_PASSWORD);
    when(idGenerator.generate()).thenReturn(GENERATED_ID);
    when(clock.now()).thenReturn(FIXED_NOW);
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    User result = useCase.execute(command);

    // Assert
    assertThat(result.getName()).isEqualTo("John Doe");
  }
}
