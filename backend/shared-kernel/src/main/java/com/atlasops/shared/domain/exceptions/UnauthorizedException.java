package com.atlasops.shared.domain.exceptions;

/** Thrown when authentication is required but missing or invalid. Maps to HTTP 401 Unauthorized. */
public class UnauthorizedException extends RuntimeException {

  public UnauthorizedException(String message) {
    super(message);
  }

  public UnauthorizedException(String message, Throwable cause) {
    super(message, cause);
  }
}
