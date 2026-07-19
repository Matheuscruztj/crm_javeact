package com.atlasops.search.domain.ports;

/**
 * Port defining the contract for updating the search index when entities change. Implementations
 * update the underlying search index (e.g., PostgreSQL tsvector).
 */
public interface SearchIndexUpdatePort {

  /**
   * Indexes or re-indexes an entity in the search index.
   *
   * @param entityType the type of entity (e.g., CUSTOMER, DOCUMENT, REQUEST)
   * @param entityId the unique identifier of the entity
   * @param content the searchable text content to index
   * @param tenantId the tenant that owns this entity
   */
  void indexEntity(String entityType, String entityId, String content, String tenantId);

  /**
   * Removes an entity from the search index.
   *
   * @param entityType the type of entity
   * @param entityId the unique identifier of the entity
   * @param tenantId the tenant that owns this entity
   */
  void removeEntity(String entityType, String entityId, String tenantId);
}
