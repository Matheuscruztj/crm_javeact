package com.atlasops.search.application;

import java.util.Objects;

/**
 * Command object for the unified search use case. Contains the search query, pagination parameters,
 * and caller context for role-based filtering.
 *
 * @param query the search text (2-200 characters)
 * @param entityTypeFilter optional filter by entity type (CUSTOMER, REQUEST, DOCUMENT); null means
 *     all types
 * @param tenantId the authenticated user's tenant identifier (required)
 * @param userId the authenticated user's identifier (required for CLIENT filtering)
 * @param role the authenticated user's role (ADMIN, ANALYST, or CLIENT)
 * @param page zero-based page number
 * @param size page size (default 20, max 50)
 */
public record UnifiedSearchCommand(
    String query,
    String entityTypeFilter,
    String tenantId,
    String userId,
    String role,
    int page,
    int size) {

  public UnifiedSearchCommand {
    Objects.requireNonNull(query, "Query must not be null");
    Objects.requireNonNull(tenantId, "TenantId must not be null");
    Objects.requireNonNull(userId, "UserId must not be null");
    Objects.requireNonNull(role, "Role must not be null");

    if (tenantId.isBlank()) {
      throw new IllegalArgumentException("TenantId must not be blank");
    }
    if (userId.isBlank()) {
      throw new IllegalArgumentException("UserId must not be blank");
    }
    if (role.isBlank()) {
      throw new IllegalArgumentException("Role must not be blank");
    }
  }

  /**
   * Creates a command without entity type filter using default pagination.
   *
   * @param query the search text
   * @param tenantId the tenant identifier
   * @param userId the user identifier
   * @param role the user's role
   * @return a UnifiedSearchCommand with defaults
   */
  public static UnifiedSearchCommand withDefaults(
      String query, String tenantId, String userId, String role) {
    return new UnifiedSearchCommand(query, null, tenantId, userId, role, 0, 20);
  }
}
