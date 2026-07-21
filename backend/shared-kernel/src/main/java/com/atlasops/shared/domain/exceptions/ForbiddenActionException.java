package com.atlasops.shared.domain.exceptions;

/**
 * Thrown when the authenticated user does not have permission to perform the requested action. Maps
 * to HTTP 403 Forbidden.
 */
public class ForbiddenActionException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public ForbiddenActionException(String message) {
    super(message);
  }

  public ForbiddenActionException(String message, Throwable cause) {
    super(message, cause);
  }
}
