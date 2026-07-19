package com.atlasops.activities.application;

import com.atlasops.activities.domain.Activity;
import com.atlasops.activities.domain.ports.ActivityRepository;
import java.util.Objects;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * Use case for querying activities by entity within a tenant. Results are ordered by timestamp
 * descending with configurable pagination.
 */
public class GetEntityActivitiesUseCase {

  private static final int DEFAULT_PAGE_SIZE = 20;
  private static final int MAX_PAGE_SIZE = 100;

  private final ActivityRepository activityRepository;

  public GetEntityActivitiesUseCase(ActivityRepository activityRepository) {
    this.activityRepository = activityRepository;
  }

  /**
   * Queries activities for a specific entity within a tenant.
   *
   * @param entityType the type of entity to query activities for
   * @param entityId the entity identifier
   * @param tenantId the tenant identifier
   * @param page zero-based page number
   * @param size page size (min 1, default 20, max 100)
   * @return a page of activities ordered by timestamp descending
   */
  public Page<Activity> execute(
      String entityType, String entityId, String tenantId, int page, int size) {
    Objects.requireNonNull(entityType, "EntityType must not be null");
    Objects.requireNonNull(entityId, "EntityId must not be null");
    Objects.requireNonNull(tenantId, "TenantId must not be null");

    if (entityType.isBlank()) {
      throw new IllegalArgumentException("EntityType must not be blank");
    }
    if (entityId.isBlank()) {
      throw new IllegalArgumentException("EntityId must not be blank");
    }
    if (tenantId.isBlank()) {
      throw new IllegalArgumentException("TenantId must not be blank");
    }

    if (page < 0) {
      page = 0;
    }
    if (size < 1) {
      size = DEFAULT_PAGE_SIZE;
    }
    if (size > MAX_PAGE_SIZE) {
      size = MAX_PAGE_SIZE;
    }

    return activityRepository.findByEntityAndTenantId(
        entityType, entityId, tenantId, PageRequest.of(page, size));
  }
}
