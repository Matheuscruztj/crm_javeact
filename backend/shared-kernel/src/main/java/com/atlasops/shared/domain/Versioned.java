package com.atlasops.shared.domain;

/**
 * Interface for entities that support optimistic concurrency control.
 * The version number is used as ETag value in HTTP responses
 * and validated via If-Match header on updates.
 */
public interface Versioned {

  /**
   * Returns the current version of the entity.
   * This value is incremented on each successful update.
   *
   * @return the version number, starting at 0
   */
  long getVersion();
}
