package com.atlasops.auth.presentation;

import com.atlasops.auth.domain.AuthenticationResult;

/**
 * Response DTO for authentication token responses.
 *
 * @param accessToken the JWT access token
 * @param refreshToken the refresh token for rotation
 * @param expiresIn the access token lifetime in seconds
 * @param tokenType the token type (Bearer)
 */
public record TokenResponse(
    String accessToken, String refreshToken, long expiresIn, String tokenType) {

  /**
   * Creates a TokenResponse from an AuthenticationResult domain object.
   *
   * @param result the domain authentication result
   * @return the response DTO
   */
  public static TokenResponse from(AuthenticationResult result) {
    return new TokenResponse(
        result.accessToken(), result.refreshToken(), result.expiresIn(), result.tokenType());
  }
}
