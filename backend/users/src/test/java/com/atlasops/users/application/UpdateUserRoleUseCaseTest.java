package com.atlasops.users.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlasops.shared.domain.exceptions.ResourceNotFoundException;
import com.atlasops.shared.domain.ports.Clock;
import com.atlasops.users.domain.User;
import com.atlasops.users.domain.UserRole;
import com.atlasops.users.domain.ports.UserRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateUserRoleUseCaseTest {

  private static final Instant FIXED_NOW = Instant.parse("2025-01-15T10:30:00Z");
  private static final Instant UPDATED_NOW = Instant.parse("2025-01-16T08:00:00Z");
  private static final String USER_ID = "user-001";
  private static final String TENANT_ID = "tenant-alpha";

  @Mock private UserRepository userRepository;

  @Mock private Clock clock;

  @InjectMocks private UpdateUserRoleUseCase useCase;

  @Test
  void should_updateRole_when_roleIsValid() {
    // Arrange
    User existingUser =
        User.create(
            USER_ID,
            "user@example.com",
            "John Doe",
            "hashedPw",
            UserRole.ANALYST,
            TENANT_ID,
            FIXED_NOW);
    when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser));
    when(clock.now()).thenReturn(UPDATED_NOW);
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

    UpdateUserRoleCommand command = new UpdateUserRoleCommand(USER_ID, "ADMIN");

    // Act
    User result = useCase.execute(command);

    // Assert
    assertThat(result.getRole()).isEqualTo(UserRole.ADMIN);
    assertThat(result.getUpdatedAt()).isEqualTo(UPDATED_NOW);
    verify(userRepository).save(existingUser);
  }

  @Test
  void should_updateRoleToClient_when_roleIsClient() {
    // Arrange
    User existingUser =
        User.create(
            USER_ID,
            "user@example.com",
            "John Doe",
            "hashedPw",
            UserRole.ADMIN,
            TENANT_ID,
            FIXED_NOW);
    when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser));
    when(clock.now()).thenReturn(UPDATED_NOW);
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

    UpdateUserRoleCommand command = new UpdateUserRoleCommand(USER_ID, "CLIENT");

    // Act
    User result = useCase.execute(command);

    // Assert
    assertThat(result.getRole()).isEqualTo(UserRole.CLIENT);
  }

  @Test
  void should_throwIllegalArgumentException_when_roleIsInvalid() {
    // Arrange
    UpdateUserRoleCommand command = new UpdateUserRoleCommand(USER_ID, "SUPER_USER");

    // Act & Assert
    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Invalid role");

    verify(userRepository, never()).save(any());
  }

  @Test
  void should_throwResourceNotFoundException_when_userNotFound() {
    // Arrange
    when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

    UpdateUserRoleCommand command = new UpdateUserRoleCommand(USER_ID, "ADMIN");

    // Act & Assert
    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining(USER_ID);

    verify(userRepository, never()).save(any());
  }

  @Test
  void should_throwIllegalArgumentException_when_userIdIsNull() {
    // Arrange
    UpdateUserRoleCommand command = new UpdateUserRoleCommand(null, "ADMIN");

    // Act & Assert
    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("UserId");

    verify(userRepository, never()).save(any());
  }

  @Test
  void should_throwIllegalArgumentException_when_roleIsEmpty() {
    // Arrange
    UpdateUserRoleCommand command = new UpdateUserRoleCommand(USER_ID, "");

    // Act & Assert
    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Role");

    verify(userRepository, never()).save(any());
  }

  @Test
  void should_acceptRoleCaseInsensitive_when_roleIsLowercase() {
    // Arrange
    User existingUser =
        User.create(
            USER_ID,
            "user@example.com",
            "John Doe",
            "hashedPw",
            UserRole.ANALYST,
            TENANT_ID,
            FIXED_NOW);
    when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser));
    when(clock.now()).thenReturn(UPDATED_NOW);
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

    UpdateUserRoleCommand command = new UpdateUserRoleCommand(USER_ID, "client");

    // Act
    User result = useCase.execute(command);

    // Assert
    assertThat(result.getRole()).isEqualTo(UserRole.CLIENT);
  }
}
