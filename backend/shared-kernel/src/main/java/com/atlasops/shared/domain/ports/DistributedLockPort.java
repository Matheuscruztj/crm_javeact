package com.atlasops.shared.domain.ports;

import java.time.Duration;
import java.util.Optional;

/**
 * Port for acquiring and releasing distributed locks.
 *
 * <p>Implementations must guarantee:
 * <ul>
 *   <li>Mutual exclusion — at most one holder at a time for a given key
 *   <li>Safe release — a lock can only be released by its owner (fencing token check)
 *   <li>Automatic expiry — locks expire after TTL even if the holder crashes
 *   <li>Fencing tokens — monotonically increasing tokens to detect stale writers
 * </ul>
 *
 * <p>Usage pattern:
 * <pre>{@code
 * Optional<LockHandle> lock = lockPort.tryAcquire("upload:doc-123", Duration.ofSeconds(30));
 * if (lock.isEmpty()) {
 *   throw new BusinessRuleViolationException("Upload already in progress");
 * }
 * try {
 *   // critical section
 * } finally {
 *   lockPort.release(lock.get());
 * }
 * }</pre>
 *
 * <p>Validates: P0.O.2 — Distributed Locking
 */
public interface DistributedLockPort {

  /**
   * Attempts to acquire a lock for the given key.
   *
   * @param key unique identifier for the resource being locked
   * @param ttl how long the lock remains valid (auto-expires after this duration)
   * @return a {@link LockHandle} if the lock was acquired, or empty if already held
   */
  Optional<LockHandle> tryAcquire(String key, Duration ttl);

  /**
   * Releases a previously acquired lock. Safe to call multiple times — subsequent calls are no-ops.
   *
   * @param handle the handle returned by {@link #tryAcquire}
   */
  void release(LockHandle handle);

  /**
   * Handle representing an acquired lock.
   *
   * @param key the lock key
   * @param ownerToken a unique token identifying the lock owner (for safe release)
   * @param fencingToken a monotonically increasing token for stale writer detection
   */
  record LockHandle(String key, String ownerToken, long fencingToken) {}
}
