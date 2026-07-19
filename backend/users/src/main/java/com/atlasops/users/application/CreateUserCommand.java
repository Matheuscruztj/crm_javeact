package com.atlasops.users.application;

/**
 * Command for creating a new user within a tenant.
 *
 * @param email the user email address
 * @param name the user display name
 * @param password the raw password (will be hashed)
 * @param role the role to assign (ADMIN, ANALYST, or CLIENT)
 * @param tenantId the tenant this user belongs to
 */
public record CreateUserCommand(
    String email, String name, String password, String role, String tenantId) {}
