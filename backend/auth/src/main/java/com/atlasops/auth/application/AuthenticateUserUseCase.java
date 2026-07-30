package com.atlasops.auth.application;

import com.atlasops.auth.domain.AccountLockout;
import com.atlasops.auth.domain.AuthenticationResult;
import com.atlasops.auth.domain.RefreshToken;
import com.atlasops.auth.domain.Role;
import com.atlasops.auth.domain.ports.AccountLockoutPort;
import com.atlasops.auth.domain.ports.AuthUserPort;
import com.atlasops.auth.domain.ports.AuthUserPort.AuthUserData;
import com.atlasops.auth.domain.ports.JwtTokenPort;
import com.atlasops.auth.domain.ports.PasswordHashPort;
import com.atlasops.auth.domain.ports.RefreshTokenRepository;
import com.atlasops.shared.domain.exceptions.TooManyRequestsException;
import com.atlasops.shared.domain.exceptions.UnauthorizedException;
import com.atlasops.shared.domain.ports.Clock;
import com.atlasops.shared.domain.ports.IdGenerator;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Use case for authenticating a user with email and password.
 *
 * <p>Flow:
 *
 * <ol>
 *   <li>Check if the account is locked (via AccountLockoutPort)
 *   <li>Find user by email and tenant (via AuthUserPort)
 *   <li>Verify password (via PasswordHashPort)
 *   <li>On success: reset failed attempts, generate JWT, create refresh token, return result
 *   <li>On failure: increment failed attempts, lock if threshold reached, return generic 401
 *   <li>If locked: return 429 Too Many Requests
 * </ol>
 *
 * <p>Requirements: 1.1, 1.2, 1.3, 1.10, 1.11, 1.12
 */
@Service
public class AuthenticateUserUseCase {

  private static final Duration LOCKOUT_DURATION = Duration.ofMinutes(15);
  private static final Duration DEFAULT_REFRESH_TOKEN_TTL = Duration.ofDays(7);
  private static final long DEFAULT_ACCESS_TOKEN_EXPIRES_IN_SECONDS = 900L;
  private static final int MAX_FAILED_ATTEMPTS = 5;
  private static final String GENERIC_AUTH_ERROR_MESSAGE = "Invalid email or password";

  private final AuthUserPort authUserPort;
  private final AccountLockoutPort accountLockoutPort;
  private final PasswordHashPort passwordHashPort;
  private final JwtTokenPort jwtTokenPort;
  private final RefreshTokenRepository refreshTokenRepository;
  private final IdGenerator idGenerator;
  private final Clock clock;

  public AuthenticateUserUseCase(
      AuthUserPort authUserPort,
      AccountLockoutPort accountLockoutPort,
      PasswordHashPort passwordHashPort,
      JwtTokenPort jwtTokenPort,
      RefreshTokenRepository refreshTokenRepository,
      IdGenerator idGenerator,
      Clock clock) {
    this.authUserPort = Objects.requireNonNull(authUserPort);
    this.accountLockoutPort = Objects.requireNonNull(accountLockoutPort);
    this.passwordHashPort = Objects.requireNonNull(passwordHashPort);
    this.jwtTokenPort = Objects.requireNonNull(jwtTokenPort);
    this.refreshTokenRepository = Objects.requireNonNull(refreshTokenRepository);
    this.idGenerator = Objects.requireNonNull(idGenerator);
    this.clock = Objects.requireNonNull(clock);
  }

  /**
   * Authenticates a user with credentials.
   *
   * @param command the authentication command containing email, password, and tenantId
   * @return AuthenticationResult with access token, refresh token, and expiration metadata
   * @throws TooManyRequestsException if the account is locked due to too many failed attempts
   * @throws UnauthorizedException if credentials are invalid (generic message for both wrong email
   *     and password)
   */
  public AuthenticationResult execute(AuthenticateCommand command) {
    Objects.requireNonNull(command, "command must not be null");

    String email = command.email();
    Instant now = clock.now();

    // 1. Check if account is locked
    AccountLockout lockout = accountLockoutPort.getAttempts(email);
    if (lockout.isLocked(now)) {
      throw new TooManyRequestsException(
          "Account temporarily locked due to too many failed attempts");
    }

    // 2. Find user by email and tenant
    Optional<AuthUserData> optionalUser =
        authUserPort.findByEmailAndTenantId(email, command.tenantId());
    if (optionalUser.isEmpty()) {
      handleFailedAttempt(email, now);
      throw new UnauthorizedException(GENERIC_AUTH_ERROR_MESSAGE);
    }

    AuthUserData userData = optionalUser.get();

    // Check if user is active
    if (!userData.active()) {
      handleFailedAttempt(email, now);
      throw new UnauthorizedException(GENERIC_AUTH_ERROR_MESSAGE);
    }

    // 3. Verify password
    if (!passwordHashPort.verify(command.password(), userData.passwordHash())) {
      handleFailedAttempt(email, now);
      throw new UnauthorizedException(GENERIC_AUTH_ERROR_MESSAGE);
    }

    // 4. On success: reset failed attempts
    accountLockoutPort.resetFailedAttempts(email);

    // 5. Generate JWT access token with claims (userId, tenantId, role)
    Role role = Role.valueOf(userData.role());
    String accessToken =
        jwtTokenPort.generateAccessToken(userData.userId(), userData.tenantId(), role);

    // 6. Create and persist refresh token (with role and family ID for rotation)
    String rawRefreshToken = UUID.randomUUID().toString();
    String refreshTokenHash = hashToken(rawRefreshToken);
    Instant expiresAt = now.plus(DEFAULT_REFRESH_TOKEN_TTL);
    String familyId = idGenerator.generate();

    RefreshToken refreshToken =
        new RefreshToken(
            idGenerator.generate(),
            refreshTokenHash,
            userData.userId(),
            userData.tenantId(),
            role.name(),
            familyId,
            expiresAt,
            false,
            now);
    refreshTokenRepository.save(refreshToken);

    // 7. Return authentication result
    return AuthenticationResult.of(
        accessToken, rawRefreshToken, DEFAULT_ACCESS_TOKEN_EXPIRES_IN_SECONDS);
  }

  private void handleFailedAttempt(String email, Instant now) {
    accountLockoutPort.incrementFailedAttempts(email);

    // Re-read lockout state after increment to check if we've hit the threshold
    AccountLockout updatedLockout = accountLockoutPort.getAttempts(email);
    if (updatedLockout.shouldLock()) {
      Instant lockedUntil = now.plus(LOCKOUT_DURATION);
      accountLockoutPort.lockAccount(email, lockedUntil);
    }
  }

  static String hashToken(String rawToken) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(hash);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 algorithm not available", e);
    }
  }
}
