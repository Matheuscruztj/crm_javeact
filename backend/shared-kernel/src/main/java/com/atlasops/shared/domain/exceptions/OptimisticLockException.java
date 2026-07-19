package com.atlasops.shared.domain.exceptions;

/**
 * Thrown when an optimistic locking conflict is detected (ETag mismatch).
 * Maps to HTTP 412 Precondition Failed or 409 Conflict.
 */
public class OptimisticLockException extends RuntimeException {

  public OptimisticLockException(String message) {
    super(message);
  }

  public OptimisticLockException(String resourceType, String resourceId) {
    super(
        "Optimistic lock conflict on "
            + resourceType
            + " with id "
            + resourceId
            + ". The resource was modified by another request.");
  }
}
