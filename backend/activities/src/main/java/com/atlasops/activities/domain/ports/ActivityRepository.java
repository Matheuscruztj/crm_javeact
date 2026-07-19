package com.atlasops.activities.domain.ports;

import com.atlasops.activities.domain.Activity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Port defining persistence operations for Activity entities. All query methods require tenant
 * context for data isolation. Supports deduplication via eventId uniqueness.
 */
public interface ActivityRepository {

  /**
   * Persists a new activity record.
   *
   * @param activity the activity to persist
   * @return the persisted activity
   */
  Activity save(Activity activity);

  /**
   * Checks whether an activity with the given eventId already exists. Used for deduplication to
   * prevent duplicate activity records from the same domain event.
   *
   * @param eventId the unique event identifier to check
   * @return true if an activity with this eventId exists, false otherwise
   */
  boolean existsByEventId(String eventId);

  /**
   * Finds an activity by its unique identifier within a tenant.
   *
   * @param id the activity identifier
   * @param tenantId the tenant identifier
   * @return the activity if found
   */
  Optional<Activity> findById(String id, String tenantId);

  /**
   * Finds all activities for a given entity within a tenant, ordered by timestamp descending.
   *
   * @param entityType the type of entity
   * @param entityId the entity identifier
   * @param tenantId the tenant identifier
   * @param pageable pagination parameters
   * @return a page of activities related to the entity
   */
  Page<Activity> findByEntityAndTenantId(
      String entityType, String entityId, String tenantId, Pageable pageable);

  /**
   * Finds all activities for a tenant, ordered by timestamp descending. Global tenant activity
   * feed.
   *
   * @param tenantId the tenant identifier
   * @param pageable pagination parameters
   * @return a page of activities for the tenant
   */
  Page<Activity> findByTenantId(String tenantId, Pageable pageable);

  /**
   * Finds activities for a tenant restricted to specific entity identifiers. Used to filter
   * activities for CLIENT users who can only see their associated customers' entities.
   *
   * @param tenantId the tenant identifier
   * @param entityIds the list of entity identifiers to include
   * @param pageable pagination parameters
   * @return a page of activities matching the entity filter
   */
  Page<Activity> findByTenantIdAndEntityIds(
      String tenantId, List<String> entityIds, Pageable pageable);
}
