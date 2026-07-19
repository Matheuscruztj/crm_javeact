package com.atlasops.auth.application;

import com.atlasops.auth.domain.ports.RefreshTokenRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

/** Use case for logging out a user by invalidating their refresh token in Redis. */
public class LogoutUseCase {

  private final RefreshTokenRepository refreshTokenRepository;

  public LogoutUseCase(RefreshTokenRepository refreshTokenRepository) {
    this.refreshTokenRepository = Objects.requireNonNull(refreshTokenRepository);
  }

  /**
   * Invalidates the given refresh token, preventing further use.
   *
   * @param refreshTokenValue the raw refresh token value from the client
   */
  public void execute(String refreshTokenValue) {
    Objects.requireNonNull(refreshTokenValue, "Refresh token value must not be null");

    String tokenHash = hashToken(refreshTokenValue);
    refreshTokenRepository.revokeByTokenHash(tokenHash);
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
