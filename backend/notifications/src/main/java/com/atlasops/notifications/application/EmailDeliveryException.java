package com.atlasops.notifications.application;

/**
 * Thrown when email delivery fails after all retry attempts. Maps to HTTP 503 Service Unavailable.
 */
public class EmailDeliveryException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public EmailDeliveryException(String message, Throwable cause) {
    super(message, cause);
  }
}
