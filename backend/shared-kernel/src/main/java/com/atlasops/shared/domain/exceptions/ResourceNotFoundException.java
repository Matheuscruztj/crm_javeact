package com.atlasops.shared.domain.exceptions;

/** Thrown when a requested resource cannot be found. Maps to HTTP 404 Not Found. */
public class ResourceNotFoundException extends RuntimeException {

  public ResourceNotFoundException(String message) {
    super(message);
  }

  public ResourceNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}
