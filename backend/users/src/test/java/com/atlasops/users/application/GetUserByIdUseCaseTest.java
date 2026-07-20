package com.atlasops.users.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.atlasops.shared.domain.exceptions.ResourceNotFoundException;
import com.atlasops.users.domain.User;
import com.atlasops.users.domain.UserRole;
import com.atlasops.users.domain.ports.UserRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetUserByIdUseCaseTest {

  private static final Instant NOW = Instant.parse("2025-01-15T10:00:00Z");

  @Mock private UserRepository userRepository;
  private GetUserByIdUseCase useCase;

  @BeforeEach
  void setUp() { useCase = new GetUserByIdUseCase(userRepository); }

  private User sampleUser(String id) {
    return User.create(id, "user@test.com", "Test User", "hashed-pw",
        UserRole.ANALYST, "tenant-alpha", NOW);
  }

  @Test
  void should_returnUser_when_found() {
    when(userRepository.findById("user-001")).thenReturn(Optional.of(sampleUser("user-001")));

    User result = useCase.execute("user-001");

    assertThat(result.getId()).isEqualTo("user-001");
    assertThat(result.getEmail()).isEqualTo("user@test.com");
  }

  @Test
  void should_throwNotFound_when_userMissing() {
    when(userRepository.findById("nonexistent")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> useCase.execute("nonexistent"))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("nonexistent");
  }

  @Test
  void should_throwIllegalArgument_when_idIsBlank() {
    assertThatThrownBy(() -> useCase.execute("  "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("User ID");
  }

  @Test
  void should_throwIllegalArgument_when_idIsNull() {
    assertThatThrownBy(() -> useCase.execute(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("User ID");
  }
}
