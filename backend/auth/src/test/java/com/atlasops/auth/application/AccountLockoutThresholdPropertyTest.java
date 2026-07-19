package com.atlasops.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import com.atlasops.auth.domain.AccountLockout;
import com.atlasops.auth.domain.Role;
import com.atlasops.auth.domain.ports.AccountLockoutPort;
import com.atlasops.auth.domain.ports.AuthUserPort;
import com.atlasops.auth.domain.ports.AuthUserPort.AuthUserData;
import com.atlasops.auth.domain.ports.JwtTokenPort;
import com.atlasops.auth.domain.ports.PasswordHashPort;
import com.atlasops.auth.domain.ports.RefreshTokenRepository;
import com.atlasops.shared.domain.exceptions.UnauthorizedException;
import com.atlasops.shared.domain.ports.Clock;
import com.atlasops.shared.domain.ports.IdGenerator;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import net.jqwik.api.*;

/**
 * Property-based tests for account lockout threshold behavior.
 *
 * <p><b>Validates: Requirements 1.11, 1.12</b>
 *
 * <p>Property 4: Account Lockout Threshold
 *
 * <p>Requirement 1.11: IF a user fails authentication 5 consecutive times within 15 minutes, THEN
 * lock the account for 15 minutes
 *
 * <p>Requirement 1.12: WHEN a user successfully authenticates, reset the failed attempt counter to
 * zero
 */
@Tag("Feature: project-implementation-kickoff, Property 4: Account Lockout Threshold")
class AccountLockoutThresholdPropertyTest {

  private static final Instant NOW = Instant.parse("2025-01-15T10:00:00Z");
  private static final String TENANT_ID = "tenant-test";
  private static final String USER_ID = "user-001";
  private static final String USER_EMAIL = "user@atlasops.test";
  private static final String PASSWORD_HASH = "$2a$10$hashedPassword";
  private static final String CORRECT_PASSWORD = "correctPassword123";
  private static final String WRONG_PASSWORD = "wrongPassword";
  private static final String ACCESS_TOKEN = "jwt-access-token";
  private static final String REFRESH_TOKEN_ID = "rt-001";

  /**
   * Property: For ANY number of failed attempts strictly less than 5, the account SHALL NOT be
   * locked.
   *
   * <p>Validates: Requirement 1.11 (threshold is exactly 5)
   */
  @Property(tries = 100)
  void should_notLockAccount_when_failedAttemptsAreBelowThreshold(
      @ForAll("failedAttemptsBelowThreshold") int failedAttempts) {

    // Arrange: simulate a state where the user has had `failedAttempts` failures
    // and then one more failure occurs (total = failedAttempts + 1, still < 5 if we start below 4)
    // We test that at `failedAttempts` attempts, the account is not locked
    var tracker = new InMemoryAccountLockoutPort(failedAttempts);
    var useCase = buildUseCase(tracker, false);

    var command = new AuthenticateCommand(USER_EMAIL, WRONG_PASSWORD, TENANT_ID);

    // Act: attempt authentication with wrong password
    Throwable thrown = catchThrowable(() -> useCase.execute(command));

    // Assert: should get UnauthorizedException (not TooManyRequests) since we're below threshold
    assertThat(thrown).isInstanceOf(UnauthorizedException.class);

    // The account should not be locked (lockAccount not called) because
    // after increment, total = failedAttempts + 1, which is still < 5
    assertThat(tracker.isAccountLocked()).isFalse();
  }

  /**
   * Property: After exactly 5 consecutive failures, the account SHALL be locked.
   *
   * <p>Validates: Requirement 1.11 (lock after 5 consecutive failures within 15 minutes)
   */
  @Property(tries = 100)
  void should_lockAccount_when_failedAttemptsReachThreshold(
      @ForAll("emailVariations") String email) {

    // Arrange: simulate exactly 4 failed attempts already recorded
    // so the next failure (the 5th) triggers the lock
    var tracker = new InMemoryAccountLockoutPort(4);
    var useCase = buildUseCaseWithEmail(tracker, false, email);

    var command = new AuthenticateCommand(email, WRONG_PASSWORD, TENANT_ID);

    // Act: 5th consecutive failure
    Throwable thrown = catchThrowable(() -> useCase.execute(command));

    // Assert: should throw UnauthorizedException (it's the auth failure itself)
    assertThat(thrown).isInstanceOf(UnauthorizedException.class);

    // After the 5th failure, the account MUST be locked
    assertThat(tracker.isAccountLocked()).isTrue();
    assertThat(tracker.getLockedUntil()).isNotNull();
    // Locked for 15 minutes from now
    assertThat(tracker.getLockedUntil()).isEqualTo(NOW.plusSeconds(900));
  }

  /**
   * Property: After a successful authentication, the failed attempt counter SHALL be reset to zero,
   * regardless of how many previous failures existed (below lock threshold).
   *
   * <p>Validates: Requirement 1.12 (reset counter on success)
   */
  @Property(tries = 100)
  void should_resetFailedAttemptCounter_when_authenticationSucceeds(
      @ForAll("failedAttemptsBelowThreshold") int previousFailures) {

    // Arrange: user has some previous failures but account is NOT locked
    var tracker = new InMemoryAccountLockoutPort(previousFailures);
    var useCase = buildUseCase(tracker, true);

    var command = new AuthenticateCommand(USER_EMAIL, CORRECT_PASSWORD, TENANT_ID);

    // Act: successful authentication
    var result = useCase.execute(command);

    // Assert: authentication succeeds
    assertThat(result).isNotNull();
    assertThat(result.accessToken()).isEqualTo(ACCESS_TOKEN);

    // Failed attempts counter must have been reset
    assertThat(tracker.wasResetCalled()).isTrue();
  }

  // ---- Custom Arbitraries ----

  @Provide
  Arbitrary<Integer> failedAttemptsBelowThreshold() {
    return Arbitraries.integers().between(0, 3);
  }

  @Provide
  Arbitrary<String> emailVariations() {
    return Arbitraries.strings()
        .withCharRange('a', 'z')
        .ofMinLength(3)
        .ofMaxLength(20)
        .map(local -> local + "@test.com");
  }

  // ---- Helper methods ----

  private AuthenticateUserUseCase buildUseCase(
      InMemoryAccountLockoutPort lockoutPort, boolean correctPassword) {
    return buildUseCaseWithEmail(lockoutPort, correctPassword, USER_EMAIL);
  }

  private AuthenticateUserUseCase buildUseCaseWithEmail(
      InMemoryAccountLockoutPort lockoutPort, boolean correctPassword, String email) {

    AuthUserPort authUserPort =
        (e, t) -> {
          if (e.equals(email) && t.equals(TENANT_ID)) {
            return Optional.of(
                new AuthUserData(USER_ID, email, PASSWORD_HASH, "ANALYST", TENANT_ID, true));
          }
          return Optional.empty();
        };

    PasswordHashPort passwordHashPort =
        new PasswordHashPort() {
          @Override
          public String hash(String rawPassword) {
            return PASSWORD_HASH;
          }

          @Override
          public boolean verify(String rawPassword, String hashedPassword) {
            return correctPassword && rawPassword.equals(CORRECT_PASSWORD);
          }
        };

    JwtTokenPort jwtTokenPort =
        new JwtTokenPort() {
          @Override
          public String generateAccessToken(String userId, String tenantId, Role role) {
            return ACCESS_TOKEN;
          }

          @Override
          public com.atlasops.auth.domain.JwtClaims validateToken(String token) {
            return null;
          }
        };

    RefreshTokenRepository refreshTokenRepository =
        new RefreshTokenRepository() {
          @Override
          public void save(com.atlasops.auth.domain.RefreshToken token) {}

          @Override
          public Optional<com.atlasops.auth.domain.RefreshToken> findByTokenHash(String tokenHash) {
            return Optional.empty();
          }

          @Override
          public void revokeByTokenHash(String tokenHash) {}

          @Override
          public void revokeAllByUserId(String userId) {}
        };

    IdGenerator idGenerator = () -> REFRESH_TOKEN_ID;

    Clock clock = () -> NOW;

    return new AuthenticateUserUseCase(
        authUserPort,
        lockoutPort,
        passwordHashPort,
        jwtTokenPort,
        refreshTokenRepository,
        idGenerator,
        clock);
  }

  // ---- In-Memory Test Double ----

  /**
   * In-memory implementation of AccountLockoutPort that tracks state for property verification
   * without mocking framework.
   */
  private static class InMemoryAccountLockoutPort implements AccountLockoutPort {

    private final AtomicInteger failedAttempts;
    private volatile Instant lockedUntil;
    private volatile boolean resetCalled;

    InMemoryAccountLockoutPort(int initialFailedAttempts) {
      this.failedAttempts = new AtomicInteger(initialFailedAttempts);
      this.lockedUntil = null;
      this.resetCalled = false;
    }

    @Override
    public AccountLockout getAttempts(String email) {
      return new AccountLockout(email, failedAttempts.get(), lockedUntil);
    }

    @Override
    public void incrementFailedAttempts(String email) {
      failedAttempts.incrementAndGet();
    }

    @Override
    public void resetFailedAttempts(String email) {
      failedAttempts.set(0);
      resetCalled = true;
    }

    @Override
    public void lockAccount(String email, Instant lockedUntilInstant) {
      this.lockedUntil = lockedUntilInstant;
    }

    boolean isAccountLocked() {
      return lockedUntil != null;
    }

    Instant getLockedUntil() {
      return lockedUntil;
    }

    boolean wasResetCalled() {
      return resetCalled;
    }
  }
}
