package com.atlasops.auth.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("AccountLockout")
class AccountLockoutTest {

  private static final Instant NOW = Instant.parse("2025-01-15T10:00:00Z");
  private static final Instant PAST = Instant.parse("2025-01-15T09:00:00Z");
  private static final Instant FUTURE = Instant.parse("2025-01-15T10:15:00Z");

  @Test
  void should_beLocked_when_lockedUntilIsInTheFuture() {
    var lockout = new AccountLockout("user@test.com", 5, FUTURE);

    assertThat(lockout.isLocked(NOW)).isTrue();
  }

  @Test
  void should_notBeLocked_when_lockedUntilIsInThePast() {
    var lockout = new AccountLockout("user@test.com", 5, PAST);

    assertThat(lockout.isLocked(NOW)).isFalse();
  }

  @Test
  void should_notBeLocked_when_lockedUntilIsNull() {
    var lockout = new AccountLockout("user@test.com", 3, null);

    assertThat(lockout.isLocked(NOW)).isFalse();
  }

  @Test
  void should_lock_when_failedAttemptsReachesThreshold() {
    var lockout = new AccountLockout("user@test.com", 5, null);

    assertThat(lockout.shouldLock()).isTrue();
  }

  @Test
  void should_lock_when_failedAttemptsExceedsThreshold() {
    var lockout = new AccountLockout("user@test.com", 7, null);

    assertThat(lockout.shouldLock()).isTrue();
  }

  @Test
  void should_notLock_when_failedAttemptsBelowThreshold() {
    var lockout = new AccountLockout("user@test.com", 4, null);

    assertThat(lockout.shouldLock()).isFalse();
  }

  @Test
  void should_notLock_when_zeroFailedAttempts() {
    var lockout = new AccountLockout("user@test.com", 0, null);

    assertThat(lockout.shouldLock()).isFalse();
  }

  @Test
  void should_throwException_when_emailIsNull() {
    assertThatThrownBy(() -> new AccountLockout(null, 0, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("email");
  }

  @Test
  void should_throwException_when_failedAttemptsIsNegative() {
    assertThatThrownBy(() -> new AccountLockout("user@test.com", -1, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("failedAttempts");
  }

  @Test
  void should_exposeAllFields_when_constructed() {
    var lockout = new AccountLockout("user@test.com", 3, FUTURE);

    assertThat(lockout.email()).isEqualTo("user@test.com");
    assertThat(lockout.failedAttempts()).isEqualTo(3);
    assertThat(lockout.lockedUntil()).isEqualTo(FUTURE);
  }
}
