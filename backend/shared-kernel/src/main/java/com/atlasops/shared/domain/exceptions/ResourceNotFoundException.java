package com.atlasops.shared.domain.exceptions;

/** Thrown when a requested resource cannot be found. Maps to HTTP 404 Not Found. */
public class ResourceNotFoundException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public ResourceNotFoundException(String message) {
    super(message);
  }

  public ResourceNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}
