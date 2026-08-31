package com.atlasops.auth.application;

import com.atlasops.auth.domain.AuthenticationResult;
import com.atlasops.auth.domain.RefreshToken;
import com.atlasops.auth.domain.Role;
import com.atlasops.auth.domain.ports.JwtTokenPort;
import com.atlasops.auth.domain.ports.RefreshTokenRepository;
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
 * Use case for refreshing an access token using a valid refresh token.
 * Implements token rotation with replay detection via token families.
 *
 * <p>On each refresh:
 * <ol>
 *   <li>Validate the current refresh token (not expired, not revoked)</li>
 *   <li>Revoke the old refresh token</li>
 *   <li>Issue a new refresh token in the same family</li>
 *   <li>Issue a new access token</li>
 * </ol>
 *
 * <p>If a revoked/expired token is presented, ALL tokens in the family are revoked
 * (replay detection — a stolen token was likely reused after the legitimate user
 * already rotated it).
 */
@Service
public class RefreshTokenUseCase {

  private static final Duration DEFAULT_REFRESH_TOKEN_TTL = Duration.ofDays(7);
  private static final long DEFAULT_ACCESS_TOKEN_EXPIRES_IN_SECONDS = 900L;

  private final RefreshTokenRepository refreshTokenRepository;
  private final JwtTokenPort jwtTokenPort;
  private final IdGenerator idGenerator;
  private final Clock clock;

  public RefreshTokenUseCase(
      RefreshTokenRepository refreshTokenRepository,
      JwtTokenPort jwtTokenPort,
      IdGenerator idGenerator,
      Clock clock) {
    this.refreshTokenRepository = Objects.requireNonNull(refreshTokenRepository);
    this.jwtTokenPort = Objects.requireNonNull(jwtTokenPort);
    this.idGenerator = Objects.requireNonNull(idGenerator);
    this.clock = Objects.requireNonNull(clock);
  }

  /**
   * Validates and rotates a refresh token, issuing new access and refresh tokens.
   *
   * @param refreshTokenValue the raw refresh token value from the client
   * @return a new AuthenticationResult with rotated tokens
   * @throws UnauthorizedException if the refresh token is not found, expired, or revoked
   */
  public AuthenticationResult execute(String refreshTokenValue) {
    Objects.requireNonNull(refreshTokenValue, "Refresh token value must not be null");

    String tokenHash = hashToken(refreshTokenValue);

    Optional<RefreshToken> optionalToken = refreshTokenRepository.findByTokenHash(tokenHash);

    if (optionalToken.isEmpty()) {
      throw new UnauthorizedException("Invalid refresh token");
    }

    RefreshToken existingToken = optionalToken.get();
    Instant now = clock.now();

    // Replay detection: if token is already revoked or expired,
    // someone is trying to reuse a token that was already rotated.
    // Revoke ALL tokens for this user (the whole family is compromised).
    if (!existingToken.isValid(now)) {
      refreshTokenRepository.revokeAllByUserId(existingToken.getUserId());
      throw new UnauthorizedException("Refresh token expired or revoked — all sessions invalidated");
    }

    // Revoke the old token (rotation)
    refreshTokenRepository.revokeByTokenHash(tokenHash);

    // Determine role from existing token (stored during login)
    String roleName = existingToken.getRole() != null ? existingToken.getRole() : Role.CLIENT.name();
    Role role = Role.valueOf(roleName);

    // Preserve the token family for replay detection chain
    String familyId =
        existingToken.getFamilyId() != null ? existingToken.getFamilyId() : idGenerator.generate();

    // Generate new refresh token in the same family
    String newRawToken = UUID.randomUUID().toString();
    String newTokenHash = hashToken(newRawToken);
    Instant expiresAt = now.plus(DEFAULT_REFRESH_TOKEN_TTL);

    RefreshToken newRefreshToken =
        new RefreshToken(
            idGenerator.generate(),
            newTokenHash,
            existingToken.getUserId(),
            existingToken.getTenantId(),
            roleName,
            familyId,
            expiresAt,
            false,
            now);
    refreshTokenRepository.save(newRefreshToken);

    // Generate new access token
    String accessToken =
        jwtTokenPort.generateAccessToken(existingToken.getUserId(), existingToken.getTenantId(), role);

    return AuthenticationResult.of(accessToken, newRawToken, DEFAULT_ACCESS_TOKEN_EXPIRES_IN_SECONDS);
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
