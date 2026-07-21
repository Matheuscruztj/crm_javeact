package com.atlasops.auth.domain.ports;

import java.time.Duration;

/**
 * Port defining the contract for rate limiting operations.
 * Implementations may use Redis, in-memory, or other backends.
 *
 * <p>Validates: P0.D.1 — Rate Limiting (auth endpoints)
 */
public interface RateLimiterPort {

    /**
     * Checks whether the request identified by {@code key} is within the allowed rate.
     * Increments the counter if allowed.
     *
     * @param key         the rate limit key (e.g., endpoint:clientIp)
     * @param maxRequests maximum allowed requests in the window
     * @param window      the time window duration
     * @return {@code true} if the request is allowed; {@code false} if the limit is exceeded
     */
    boolean isAllowed(String key, int maxRequests, Duration window);

    /**
     * Returns the number of remaining requests for the given key within the current window.
     *
     * @param key         the rate limit key
     * @param maxRequests maximum allowed requests
     * @return remaining requests, or {@code maxRequests} if no counter exists yet
     */
    long remaining(String key, int maxRequests);
}
