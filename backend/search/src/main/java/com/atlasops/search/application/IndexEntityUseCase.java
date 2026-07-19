package com.atlasops.search.application;

import com.atlasops.search.domain.ports.SearchIndexUpdatePort;
import java.util.Objects;

/**
 * Use case for updating the search index when entities change. Delegates indexing operations to the
 * SearchIndexUpdatePort which updates the underlying tsvector content.
 *
 * <p>Validates: Requirements 18.2
 */
public class IndexEntityUseCase {

  private final SearchIndexUpdatePort searchIndexUpdatePort;

  public IndexEntityUseCase(SearchIndexUpdatePort searchIndexUpdatePort) {
    this.searchIndexUpdatePort =
        Objects.requireNonNull(searchIndexUpdatePort, "SearchIndexUpdatePort must not be null");
  }

  /**
   * Indexes or re-indexes an entity in the search index.
   *
   * @param command the index entity command containing entity details and content
   * @throws IllegalArgumentException if required fields are missing or blank
   */
  public void execute(IndexEntityCommand command) {
    Objects.requireNonNull(command, "Command must not be null");

    searchIndexUpdatePort.indexEntity(
        command.entityType(), command.entityId(), command.content(), command.tenantId());
  }

  /**
   * Removes an entity from the search index.
   *
   * @param entityType the type of entity
   * @param entityId the entity identifier
   * @param tenantId the tenant identifier
   */
  public void remove(String entityType, String entityId, String tenantId) {
    Objects.requireNonNull(entityType, "EntityType must not be null");
    Objects.requireNonNull(entityId, "EntityId must not be null");
    Objects.requireNonNull(tenantId, "TenantId must not be null");

    searchIndexUpdatePort.removeEntity(entityType, entityId, tenantId);
  }
}
