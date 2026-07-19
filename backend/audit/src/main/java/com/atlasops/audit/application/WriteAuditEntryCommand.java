package com.atlasops.audit.application;

/**
 * Command DTO for creating a new audit entry.
 *
 * @param actionType the type of action performed (e.g., LOGIN, CREATE_CUSTOMER)
 * @param actorId the identifier of the user who performed the action
 * @param tenantId the tenant this entry belongs to
 * @param entityType the type of entity affected (e.g., CUSTOMER, DOCUMENT)
 * @param entityId the identifier of the affected entity
 * @param details JSON string with action details (max 10 KB)
 */
public record WriteAuditEntryCommand(
    String actionType,
    String actorId,
    String tenantId,
    String entityType,
    String entityId,
    String details) {}
