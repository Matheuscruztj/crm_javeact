package com.atlasops.users.presentation;

import java.time.Instant;

/**
 * Response DTO representing a user.
 *
 * @param id the user identifier
 * @param email the user email address
 * @param name the user display name
 * @param role the user role (ADMIN, ANALYST, CLIENT)
 * @param tenantId the tenant this user belongs to
 * @param status the user status (ACTIVE, INACTIVE)
 * @param createdAt the creation timestamp
 * @param updatedAt the last update timestamp
 */
public record UserResponse(
    String id,
    String email,
    String name,
    String role,
    String tenantId,
    String status,
    Instant createdAt,
    Instant updatedAt) {}
