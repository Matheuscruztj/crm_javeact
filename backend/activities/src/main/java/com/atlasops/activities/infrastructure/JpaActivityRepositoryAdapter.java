package com.atlasops.activities.infrastructure;

import com.atlasops.activities.domain.Activity;
import com.atlasops.activities.domain.ports.ActivityRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/**
 * JPA adapter implementing the ActivityRepository port. Provides persistence operations for
 * Activity entities with tenant isolation.
 */
@Component
public class JpaActivityRepositoryAdapter implements ActivityRepository {

  private final SpringDataActivityRepository springDataRepository;

  public JpaActivityRepositoryAdapter(SpringDataActivityRepository springDataRepository) {
    this.springDataRepository = springDataRepository;
  }

  @Override
  public Activity save(Activity activity) {
    ActivityJpaEntity entity = toEntity(activity);
    ActivityJpaEntity saved = springDataRepository.save(entity);
    return toDomain(saved);
  }

  @Override
  public boolean existsByEventId(String eventId) {
    return springDataRepository.existsByEventId(eventId);
  }

  @Override
  public Optional<Activity> findById(String id, String tenantId) {
    return springDataRepository.findByIdAndTenantId(id, tenantId).map(this::toDomain);
  }

  @Override
  public Page<Activity> findByEntityAndTenantId(
      String entityType, String entityId, String tenantId, Pageable pageable) {
    return springDataRepository
        .findByEntityTypeAndEntityIdAndTenantIdOrderByTimestampDesc(
            entityType, entityId, tenantId, pageable)
        .map(this::toDomain);
  }

  @Override
  public Page<Activity> findByTenantId(String tenantId, Pageable pageable) {
    return springDataRepository
        .findByTenantIdOrderByTimestampDesc(tenantId, pageable)
        .map(this::toDomain);
  }

  @Override
  public Page<Activity> findByTenantIdAndEntityIds(
      String tenantId, List<String> entityIds, Pageable pageable) {
    return springDataRepository
        .findByTenantIdAndEntityIdInOrderByTimestampDesc(tenantId, entityIds, pageable)
        .map(this::toDomain);
  }

  private ActivityJpaEntity toEntity(Activity activity) {
    return new ActivityJpaEntity(
        activity.getId(),
        activity.getEntityType(),
        activity.getEntityId(),
        activity.getActionType(),
        activity.getActorId(),
        activity.getTenantId(),
        activity.getSummary(),
        activity.getEventId(),
        activity.getTimestamp());
  }

  private Activity toDomain(ActivityJpaEntity entity) {
    return Activity.reconstitute(
        entity.getId(),
        entity.getEntityType(),
        entity.getEntityId(),
        entity.getActionType(),
        entity.getActorId(),
        entity.getTenantId(),
        entity.getSummary(),
        entity.getEventId(),
        entity.getTimestamp());
  }
}
