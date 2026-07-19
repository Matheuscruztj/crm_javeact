package com.atlasops.users.presentation;

/**
 * Request DTO for creating a new user.
 *
 * @param email the user email address
 * @param name the user display name
 * @param password the raw password
 * @param role the role to assign (ADMIN, ANALYST, or CLIENT)
 */
public record CreateUserRequest(String email, String name, String password, String role) {}
