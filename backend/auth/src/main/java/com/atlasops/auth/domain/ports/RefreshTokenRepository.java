package com.atlasops.auth.domain.ports;

import com.atlasops.auth.domain.RefreshToken;
import java.util.Optional;

/** Port for refresh token persistence (Redis-backed). */
public interface RefreshTokenRepository {

  void save(RefreshToken token);

  Optional<RefreshToken> findByTokenHash(String tokenHash);

  void revokeByTokenHash(String tokenHash);

  void revokeAllByUserId(String userId);
}
