package com.atlasops.boot.infrastructure.lock;

import com.atlasops.shared.domain.ports.DistributedLockPort;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

/**
 * Redis-backed implementation of {@link DistributedLockPort} using SET NX PX.
 *
 * <p>Locking strategy:
 * <ul>
 *   <li>Acquire: SET key ownerToken NX PX ttlMs (atomic, NX = only if not exists)
 *   <li>Fencing token: stored as a separate counter key (INCR), monotonically increasing
 *   <li>Release: Lua script checks owner before DEL (safe release, no accidental unlock)
 * </ul>
 *
 * <p>Validates: P0.O.2 — Redis Distributed Lock Adapter
 */
@Component
public class RedisDistributedLockAdapter implements DistributedLockPort {

  private static final Logger log = LoggerFactory.getLogger(RedisDistributedLockAdapter.class);
  private static final String LOCK_PREFIX = "lock:";
  private static final String FENCE_PREFIX = "fence:";

  /**
   * Lua script for safe lock release.
   * Atomically checks owner token before deleting — prevents releasing another process's lock.
   */
  private static final RedisScript<Long> RELEASE_SCRIPT =
      RedisScript.of(
          """
          if redis.call('GET', KEYS[1]) == ARGV[1] then
            return redis.call('DEL', KEYS[1])
          else
            return 0
          end
          """,
          Long.class);

  private final StringRedisTemplate redisTemplate;

  public RedisDistributedLockAdapter(StringRedisTemplate redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  @Override
  public Optional<LockHandle> tryAcquire(String key, Duration ttl) {
    String lockKey = LOCK_PREFIX + key;
    String ownerToken = UUID.randomUUID().toString();

    Boolean acquired =
        redisTemplate.opsForValue().setIfAbsent(lockKey, ownerToken, ttl);

    if (!Boolean.TRUE.equals(acquired)) {
      log.debug("Could not acquire lock for key '{}': already held", key);
      return Optional.empty();
    }

    // Increment fencing counter for stale writer detection
    String fenceKey = FENCE_PREFIX + key;
    Long fencingToken = redisTemplate.opsForValue().increment(fenceKey);
    if (fencingToken == null) {
      fencingToken = 1L;
    }

    log.debug("Acquired lock for key '{}' with token '{}' (fence={})", key, ownerToken, fencingToken);
    return Optional.of(new LockHandle(key, ownerToken, fencingToken));
  }

  @Override
  public void release(LockHandle handle) {
    String lockKey = LOCK_PREFIX + handle.key();

    Long result =
        redisTemplate.execute(RELEASE_SCRIPT, java.util.List.of(lockKey), handle.ownerToken());

    if (Long.valueOf(1L).equals(result)) {
      log.debug("Released lock for key '{}'", handle.key());
    } else {
      log.warn(
          "Lock for key '{}' was not released (already expired or owner mismatch)",
          handle.key());
    }
  }
}
