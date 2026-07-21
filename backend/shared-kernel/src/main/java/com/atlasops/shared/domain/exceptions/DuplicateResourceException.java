package com.atlasops.shared.domain.exceptions;

/** Thrown when attempting to create a resource that already exists. Maps to HTTP 409 Conflict. */
public class DuplicateResourceException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public DuplicateResourceException(String message) {
    super(message);
  }

  public DuplicateResourceException(String message, Throwable cause) {
    super(message, cause);
  }
}
