package com.atlasops.auth.infrastructure;

import java.time.Duration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Redis-backed sliding window rate limiter.
 * Uses a simple counter with TTL per key (IP or user identifier).
 */
@Component
public class RedisRateLimiter {

  private static final String RATE_LIMIT_KEY_PREFIX = "rate_limit:";

  private final StringRedisTemplate redisTemplate;

  public RedisRateLimiter(StringRedisTemplate redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  /**
   * Checks if the request is allowed and increments the counter.
   *
   * @param key the rate limit key (e.g., IP address or endpoint:IP combination)
   * @param maxRequests maximum allowed requests in the window
   * @param window the time window duration
   * @return true if the request is allowed, false if rate limit is exceeded
   */
  public boolean isAllowed(String key, int maxRequests, Duration window) {
    String redisKey = RATE_LIMIT_KEY_PREFIX + key;

    Long currentCount = redisTemplate.opsForValue().increment(redisKey);
    if (currentCount == null) {
      return false;
    }

    if (currentCount == 1L) {
      redisTemplate.expire(redisKey, window);
    }

    return currentCount <= maxRequests;
  }

  /**
   * Returns remaining requests for the given key.
   *
   * @param key the rate limit key
   * @param maxRequests maximum allowed requests
   * @return remaining requests, or maxRequests if no counter exists
   */
  public long remaining(String key, int maxRequests) {
    String redisKey = RATE_LIMIT_KEY_PREFIX + key;
    String value = redisTemplate.opsForValue().get(redisKey);
    if (value == null) {
      return maxRequests;
    }
    long used = Long.parseLong(value);
    return Math.max(0, maxRequests - used);
  }
}
