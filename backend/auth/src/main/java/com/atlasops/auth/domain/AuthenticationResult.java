package com.atlasops.auth.domain;

/**
 * Value object representing the result of a successful authentication.
 *
 * @param accessToken the JWT access token
 * @param refreshToken the refresh token
 * @param expiresIn the access token lifetime in seconds
 * @param tokenType the token type (always "Bearer")
 */
public record AuthenticationResult(
    String accessToken, String refreshToken, long expiresIn, String tokenType) {

  /** Factory method to create an AuthenticationResult with default token type "Bearer". */
  public static AuthenticationResult of(String accessToken, String refreshToken, long expiresIn) {
    return new AuthenticationResult(accessToken, refreshToken, expiresIn, "Bearer");
  }
}
