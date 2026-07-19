package com.atlasops.auth.domain;

import java.time.Instant;
import java.util.Objects;

/**
 * Value object tracking failed authentication attempts for an email.
 *
 * @param email the email being tracked
 * @param failedAttempts the number of consecutive failed attempts
 * @param lockedUntil the instant until which the account is locked (nullable)
 */
public record AccountLockout(String email, int failedAttempts, Instant lockedUntil) {

  public AccountLockout {
    Objects.requireNonNull(email, "email must not be null");
    if (failedAttempts < 0) {
      throw new IllegalArgumentException("failedAttempts must not be negative");
    }
  }

  private static final int MAX_FAILED_ATTEMPTS = 5;

  /** Returns true if the account is currently locked at the given instant. */
  public boolean isLocked(Instant now) {
    return lockedUntil != null && now.isBefore(lockedUntil);
  }

  /** Returns true if the number of failed attempts has reached the lockout threshold. */
  public boolean shouldLock() {
    return failedAttempts >= MAX_FAILED_ATTEMPTS;
  }
}
