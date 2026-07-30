package com.atlasops.auth.application;

import com.atlasops.auth.domain.ports.RefreshTokenRepository;
import java.util.Objects;
import org.springframework.stereotype.Service;

/**
 * Use case for revoking all active sessions of a user.
 * Invalidates all refresh tokens associated with the given userId,
 * effectively forcing re-authentication on all devices.
 */
@Service
public class RevokeAllSessionsUseCase {

  private final RefreshTokenRepository refreshTokenRepository;

  public RevokeAllSessionsUseCase(RefreshTokenRepository refreshTokenRepository) {
    this.refreshTokenRepository = Objects.requireNonNull(refreshTokenRepository);
  }

  /**
   * Revokes all refresh tokens for the specified user.
   *
   * @param userId the ID of the user whose sessions should be revoked
   */
  public void execute(String userId) {
    Objects.requireNonNull(userId, "userId must not be null");
    refreshTokenRepository.revokeAllByUserId(userId);
  }
}
