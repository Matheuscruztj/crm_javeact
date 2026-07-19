package com.atlasops.audit.domain;

import java.time.Instant;
import java.util.Objects;

/**
 * Filter criteria for querying audit entries. TenantId is required (mandatory for multi-tenant
 * isolation). All other fields are optional additional filters.
 *
 * @param tenantId the tenant to query (required)
 * @param actorId optional filter by actor
 * @param entityType optional filter by entity type
 * @param entityId optional filter by entity identifier
 * @param actionType optional filter by action type
 * @param fromTimestamp optional lower bound for timestamp (inclusive)
 * @param toTimestamp optional upper bound for timestamp (inclusive)
 */
public record AuditQueryFilters(
    String tenantId,
    String actorId,
    String entityType,
    String entityId,
    String actionType,
    Instant fromTimestamp,
    Instant toTimestamp) {

  public AuditQueryFilters {
    Objects.requireNonNull(tenantId, "TenantId must not be null");
    if (tenantId.isBlank()) {
      throw new IllegalArgumentException("TenantId must not be blank");
    }
    if (fromTimestamp != null && toTimestamp != null && fromTimestamp.isAfter(toTimestamp)) {
      throw new IllegalArgumentException("fromTimestamp must not be after toTimestamp");
    }
  }

  /**
   * Creates filters with only the required tenantId.
   *
   * @param tenantId the tenant to query
   * @return a new AuditQueryFilters with only tenantId set
   */
  public static AuditQueryFilters ofTenant(String tenantId) {
    return new AuditQueryFilters(tenantId, null, null, null, null, null, null);
  }
}
