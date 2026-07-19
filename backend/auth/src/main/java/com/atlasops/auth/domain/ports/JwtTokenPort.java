package com.atlasops.auth.domain.ports;

import com.atlasops.auth.domain.JwtClaims;
import com.atlasops.auth.domain.Role;

/** Port for JWT token generation and validation. */
public interface JwtTokenPort {

  String generateAccessToken(String userId, String tenantId, Role role);

  JwtClaims validateToken(String token);
}
