package com.atlasops.auth.domain.ports;

import com.atlasops.auth.domain.AccountLockout;
import java.time.Instant;

/** Port for tracking failed authentication attempts and account lockout (Redis-backed). */
public interface AccountLockoutPort {

  AccountLockout getAttempts(String email);

  void incrementFailedAttempts(String email);

  void resetFailedAttempts(String email);

  void lockAccount(String email, Instant lockedUntil);
}
