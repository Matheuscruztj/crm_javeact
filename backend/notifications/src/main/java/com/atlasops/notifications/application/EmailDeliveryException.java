package com.atlasops.notifications.application;

/**
 * Thrown when email delivery fails after all retry attempts. Maps to HTTP 503 Service Unavailable.
 */
public class EmailDeliveryException extends RuntimeException {

  public EmailDeliveryException(String message, Throwable cause) {
    super(message, cause);
  }
}
