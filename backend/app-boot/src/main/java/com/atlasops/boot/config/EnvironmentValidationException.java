package com.atlasops.boot.config;

/**
 * Thrown when required environment variables are missing or blank at application startup.
 *
 * <p>This exception causes fail-fast behavior, preventing the application from starting with an
 * incomplete configuration.
 */
public class EnvironmentValidationException extends RuntimeException {

  public EnvironmentValidationException(String message) {
    super(message);
  }
}
