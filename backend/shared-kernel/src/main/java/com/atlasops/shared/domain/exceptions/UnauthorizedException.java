package com.atlasops.shared.domain.exceptions;

/** Thrown when authentication is required but missing or invalid. Maps to HTTP 401 Unauthorized. */
public class UnauthorizedException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public UnauthorizedException(String message) {
    super(message);
  }

  public UnauthorizedException(String message, Throwable cause) {
    super(message, cause);
  }
}
