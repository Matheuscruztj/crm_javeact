package com.atlasops.users.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlasops.shared.domain.exceptions.BusinessRuleViolationException;
import com.atlasops.shared.domain.exceptions.ResourceNotFoundException;
import com.atlasops.shared.domain.ports.Clock;
import com.atlasops.users.domain.User;
import com.atlasops.users.domain.UserRole;
import com.atlasops.users.domain.UserStatus;
import com.atlasops.users.domain.ports.UserRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeactivateUserUseCaseTest {

  private static final Instant FIXED_NOW = Instant.parse("2025-01-15T10:30:00Z");
  private static final Instant DEACTIVATION_TIME = Instant.parse("2025-01-16T08:00:00Z");
  private static final String USER_ID = "user-001";
  private static final String REQUESTING_USER_ID = "user-002";
  private static final String TENANT_ID = "tenant-alpha";

  @Mock private UserRepository userRepository;

  @Mock private Clock clock;

  @InjectMocks private DeactivateUserUseCase useCase;

  @Test
  void should_deactivateUser_when_requestedByDifferentUser() {
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
    when(clock.now()).thenReturn(DEACTIVATION_TIME);
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

    DeactivateUserCommand command = new DeactivateUserCommand(USER_ID, REQUESTING_USER_ID);

    // Act
    User result = useCase.execute(command);

    // Assert
    assertThat(result.getStatus()).isEqualTo(UserStatus.INACTIVE);
    assertThat(result.getUpdatedAt()).isEqualTo(DEACTIVATION_TIME);
    assertThat(result.isActive()).isFalse();
    verify(userRepository).save(existingUser);
  }

  @Test
  void should_throwBusinessRuleViolationException_when_selfDeactivation() {
    // Arrange
    DeactivateUserCommand command = new DeactivateUserCommand(USER_ID, USER_ID);

    // Act & Assert
    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(BusinessRuleViolationException.class)
        .hasMessageContaining("Self-deactivation is not allowed");

    verify(userRepository, never()).save(any());
  }

  @Test
  void should_throwResourceNotFoundException_when_userNotFound() {
    // Arrange
    when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

    DeactivateUserCommand command = new DeactivateUserCommand(USER_ID, REQUESTING_USER_ID);

    // Act & Assert
    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining(USER_ID);

    verify(userRepository, never()).save(any());
  }

  @Test
  void should_throwIllegalArgumentException_when_userIdIsNull() {
    // Arrange
    DeactivateUserCommand command = new DeactivateUserCommand(null, REQUESTING_USER_ID);

    // Act & Assert
    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("UserId");

    verify(userRepository, never()).save(any());
  }

  @Test
  void should_throwIllegalArgumentException_when_requestingUserIdIsNull() {
    // Arrange
    DeactivateUserCommand command = new DeactivateUserCommand(USER_ID, null);

    // Act & Assert
    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("RequestingUserId");

    verify(userRepository, never()).save(any());
  }

  @Test
  void should_markUserInactive_when_deactivating() {
    // Arrange
    User existingUser =
        User.create(
            USER_ID,
            "admin@example.com",
            "Admin User",
            "hashedPw",
            UserRole.ADMIN,
            TENANT_ID,
            FIXED_NOW);
    when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser));
    when(clock.now()).thenReturn(DEACTIVATION_TIME);
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

    DeactivateUserCommand command = new DeactivateUserCommand(USER_ID, REQUESTING_USER_ID);

    // Act
    User result = useCase.execute(command);

    // Assert
    assertThat(result.getStatus()).isEqualTo(UserStatus.INACTIVE);
    assertThat(result.isActive()).isFalse();
  }
}
