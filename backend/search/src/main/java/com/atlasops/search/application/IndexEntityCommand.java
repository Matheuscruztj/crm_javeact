package com.atlasops.search.application;

import java.util.Objects;

/**
 * Command object for the index entity use case. Contains the entity details and searchable content
 * to be indexed.
 *
 * @param entityType the type of entity (e.g., CUSTOMER, REQUEST, DOCUMENT)
 * @param entityId the unique identifier of the entity
 * @param content the searchable text content to index
 * @param tenantId the tenant that owns this entity
 */
public record IndexEntityCommand(
    String entityType, String entityId, String content, String tenantId) {

  public IndexEntityCommand {
    Objects.requireNonNull(entityType, "EntityType must not be null");
    Objects.requireNonNull(entityId, "EntityId must not be null");
    Objects.requireNonNull(content, "Content must not be null");
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
  }
}
