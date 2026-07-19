package com.atlasops.shared.domain.exceptions;

/**
 * Thrown when an account is temporarily locked due to excessive failed authentication attempts.
 * Maps to HTTP 429 Too Many Requests.
 */
public class TooManyRequestsException extends RuntimeException {

  public TooManyRequestsException(String message) {
    super(message);
  }

  public TooManyRequestsException(String message, Throwable cause) {
    super(message, cause);
  }
}
