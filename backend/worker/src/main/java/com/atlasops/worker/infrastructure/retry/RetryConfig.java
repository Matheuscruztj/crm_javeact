package com.atlasops.worker.infrastructure.retry;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Configuration properties for retry and DLQ behavior. */
@ConfigurationProperties(prefix = "atlasops.worker.retry")
public record RetryConfig(
    int maxAttempts, long initialDelayMs, long maxDelayMs, double backoffMultiplier) {

  public RetryConfig {
    if (maxAttempts <= 0) {
      maxAttempts = 3;
    }
    if (initialDelayMs <= 0) {
      initialDelayMs = 1000;
    }
    if (maxDelayMs <= 0) {
      maxDelayMs = 16000;
    }
    if (backoffMultiplier <= 0) {
      backoffMultiplier = 4.0;
    }
  }

  /**
   * Creates default configuration.
   *
   * @return default RetryConfig (3 attempts, 1s/4s/16s backoff)
   */
  public static RetryConfig defaults() {
    return new RetryConfig(0, 0, 0, 0);
  }

  /**
   * Calculates the delay for a given attempt number.
   *
   * @param attempt the attempt number (1-based)
   * @return the delay in milliseconds
   */
  public long getDelayForAttempt(int attempt) {
    if (attempt <= 1) {
      return initialDelayMs;
    }
    long delay = (long) (initialDelayMs * Math.pow(backoffMultiplier, attempt - 1));
    return Math.min(delay, maxDelayMs);
  }
}
