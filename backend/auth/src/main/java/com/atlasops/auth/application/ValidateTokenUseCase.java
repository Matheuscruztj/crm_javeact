package com.atlasops.auth.application;

import com.atlasops.auth.domain.JwtClaims;
import com.atlasops.auth.domain.ports.JwtTokenPort;

/**
 * Use case for validating JWT tokens. Used by security filters to authenticate requests.
 *
 * <p>Validates: Requirements 1.9, 2.7
 */
public class ValidateTokenUseCase {

  private final JwtTokenPort jwtTokenPort;

  public ValidateTokenUseCase(JwtTokenPort jwtTokenPort) {
    this.jwtTokenPort = jwtTokenPort;
  }

  /**
   * Validates the given JWT token and returns the claims.
   *
   * @param token the JWT token to validate
   * @return the JWT claims if the token is valid
   * @throws com.atlasops.auth.domain.TokenExpiredException if the token has expired
   * @throws IllegalArgumentException if the token is invalid or malformed
   */
  public JwtClaims execute(String token) {
    if (token == null || token.isBlank()) {
      throw new IllegalArgumentException("Token must not be null or empty");
    }
    return jwtTokenPort.validateToken(token);
  }
}
