package com.atlasops.users.presentation;

/**
 * Request DTO for updating a user's role.
 *
 * @param role the new role to assign (ADMIN, ANALYST, or CLIENT)
 */
public record UpdateUserRoleRequest(String role) {}
