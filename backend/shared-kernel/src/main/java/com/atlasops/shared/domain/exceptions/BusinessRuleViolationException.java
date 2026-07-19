package com.atlasops.shared.domain.exceptions;

/** Thrown when a business rule is violated. Maps to HTTP 422 Unprocessable Entity. */
public class BusinessRuleViolationException extends RuntimeException {

  public BusinessRuleViolationException(String message) {
    super(message);
  }

  public BusinessRuleViolationException(String message, Throwable cause) {
    super(message, cause);
  }
}
