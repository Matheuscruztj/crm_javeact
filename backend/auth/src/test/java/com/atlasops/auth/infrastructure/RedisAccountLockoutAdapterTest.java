package com.atlasops.auth.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlasops.auth.domain.AccountLockout;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

@ExtendWith(MockitoExtension.class)
class RedisAccountLockoutAdapterTest {

  @Mock private StringRedisTemplate redisTemplate;

  @Mock private HashOperations<String, Object, Object> hashOperations;

  private RedisAccountLockoutAdapter adapter;

  @BeforeEach
  void setUp() {
    adapter = new RedisAccountLockoutAdapter(redisTemplate);
  }

  @Test
  void should_returnZeroAttempts_when_noLockoutDataExists() {
    // Arrange
    when(redisTemplate.opsForHash()).thenReturn(hashOperations);
    when(hashOperations.entries("lockout:user@test.com")).thenReturn(Map.of());

    // Act
    AccountLockout lockout = adapter.getAttempts("user@test.com");

    // Assert
    assertThat(lockout.email()).isEqualTo("user@test.com");
    assertThat(lockout.failedAttempts()).isZero();
    assertThat(lockout.lockedUntil()).isNull();
  }

  @Test
  void should_returnAttempts_when_lockoutDataExists() {
    // Arrange
    when(redisTemplate.opsForHash()).thenReturn(hashOperations);
    Map<Object, Object> entries = new HashMap<>();
    entries.put("attempts", "3");
    when(hashOperations.entries("lockout:user@test.com")).thenReturn(entries);

    // Act
    AccountLockout lockout = adapter.getAttempts("user@test.com");

    // Assert
    assertThat(lockout.email()).isEqualTo("user@test.com");
    assertThat(lockout.failedAttempts()).isEqualTo(3);
    assertThat(lockout.lockedUntil()).isNull();
  }

  @Test
  void should_returnAttemptsAndLockedUntil_when_accountIsLocked() {
    // Arrange
    when(redisTemplate.opsForHash()).thenReturn(hashOperations);
    Instant lockedUntil = Instant.parse("2025-01-15T10:15:00Z");
    Map<Object, Object> entries = new HashMap<>();
    entries.put("attempts", "5");
    entries.put("lockedUntil", lockedUntil.toString());
    when(hashOperations.entries("lockout:user@test.com")).thenReturn(entries);

    // Act
    AccountLockout lockout = adapter.getAttempts("user@test.com");

    // Assert
    assertThat(lockout.email()).isEqualTo("user@test.com");
    assertThat(lockout.failedAttempts()).isEqualTo(5);
    assertThat(lockout.lockedUntil()).isEqualTo(lockedUntil);
  }

  @Test
  void should_incrementAndSetTTL_when_incrementFailedAttempts() {
    // Arrange
    when(redisTemplate.opsForHash()).thenReturn(hashOperations);

    // Act
    adapter.incrementFailedAttempts("user@test.com");

    // Assert
    verify(hashOperations).increment("lockout:user@test.com", "attempts", 1);
    verify(redisTemplate).expire("lockout:user@test.com", Duration.ofMinutes(15));
  }

  @Test
  void should_deleteKey_when_resetFailedAttempts() {
    // Act
    adapter.resetFailedAttempts("user@test.com");

    // Assert
    verify(redisTemplate).delete("lockout:user@test.com");
  }

  @Test
  void should_setLockedUntilAndTTL_when_lockAccount() {
    // Arrange
    when(redisTemplate.opsForHash()).thenReturn(hashOperations);
    Instant lockedUntil = Instant.parse("2025-01-15T10:15:00Z");

    // Act
    adapter.lockAccount("user@test.com", lockedUntil);

    // Assert
    verify(hashOperations).put("lockout:user@test.com", "lockedUntil", "2025-01-15T10:15:00Z");
    verify(redisTemplate).expire("lockout:user@test.com", Duration.ofMinutes(15));
  }
}
