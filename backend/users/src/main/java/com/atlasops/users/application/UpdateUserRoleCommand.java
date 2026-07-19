package com.atlasops.users.application;

/**
 * Command for updating a user's role.
 *
 * @param userId the user identifier
 * @param newRole the new role to assign (ADMIN, ANALYST, or CLIENT)
 */
public record UpdateUserRoleCommand(String userId, String newRole) {}
