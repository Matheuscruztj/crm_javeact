package com.atlasops.auth.infrastructure;

import com.atlasops.auth.domain.AccountLockout;
import com.atlasops.auth.domain.ports.AccountLockoutPort;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Redis-backed implementation of AccountLockoutPort.
 *
 * <p>Key pattern: {@code lockout:{email}} → Hash with fields: attempts, lockedUntil. TTL: 15
 * minutes (reset on each failed attempt).
 */
@Component
public class RedisAccountLockoutAdapter implements AccountLockoutPort {

  private static final String LOCKOUT_KEY_PREFIX = "lockout:";
  private static final Duration LOCKOUT_TTL = Duration.ofMinutes(15);
  private static final String FIELD_ATTEMPTS = "attempts";
  private static final String FIELD_LOCKED_UNTIL = "lockedUntil";

  private final StringRedisTemplate redisTemplate;

  public RedisAccountLockoutAdapter(StringRedisTemplate redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  @Override
  public AccountLockout getAttempts(String email) {
    String key = LOCKOUT_KEY_PREFIX + email;
    Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);

    if (entries.isEmpty()) {
      return new AccountLockout(email, 0, null);
    }

    int attempts = 0;
    Instant lockedUntil = null;

    Object attemptsValue = entries.get(FIELD_ATTEMPTS);
    if (attemptsValue != null) {
      attempts = Integer.parseInt(attemptsValue.toString());
    }

    Object lockedUntilValue = entries.get(FIELD_LOCKED_UNTIL);
    if (lockedUntilValue != null && !lockedUntilValue.toString().isEmpty()) {
      lockedUntil = Instant.parse(lockedUntilValue.toString());
    }

    return new AccountLockout(email, attempts, lockedUntil);
  }

  @Override
  public void incrementFailedAttempts(String email) {
    String key = LOCKOUT_KEY_PREFIX + email;
    redisTemplate.opsForHash().increment(key, FIELD_ATTEMPTS, 1);
    redisTemplate.expire(key, LOCKOUT_TTL);
  }

  @Override
  public void resetFailedAttempts(String email) {
    String key = LOCKOUT_KEY_PREFIX + email;
    redisTemplate.delete(key);
  }

  @Override
  public void lockAccount(String email, Instant lockedUntil) {
    String key = LOCKOUT_KEY_PREFIX + email;
    redisTemplate.opsForHash().put(key, FIELD_LOCKED_UNTIL, lockedUntil.toString());
    redisTemplate.expire(key, LOCKOUT_TTL);
  }
}
