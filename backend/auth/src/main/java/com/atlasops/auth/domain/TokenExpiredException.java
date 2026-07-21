package com.atlasops.auth.domain;

/**
 * Thrown when a JWT access token has expired. Maps to HTTP 401 with code TOKEN_EXPIRED (distinct
 * from generic UNAUTHORIZED).
 */
public class TokenExpiredException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public TokenExpiredException(String message) {
    super(message);
  }

  public TokenExpiredException(String message, Throwable cause) {
    super(message, cause);
  }
}
