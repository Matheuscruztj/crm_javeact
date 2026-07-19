package com.atlasops.users.domain;

/** Enum representing the permission role assigned to a user within a tenant. */
public enum UserRole {
  ADMIN,
  ANALYST,
  CLIENT;

  /**
   * Converts a string value to a UserRole enum constant (case-insensitive).
   *
   * @param role the string representation of the role
   * @return the matching UserRole
   * @throws IllegalArgumentException if the role string does not match any valid role
   */
  public static UserRole fromString(String role) {
    if (role == null || role.isBlank()) {
      throw new IllegalArgumentException("Role must not be null or empty");
    }
    try {
      return UserRole.valueOf(role.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(
          "Invalid role: " + role + ". Must be one of: ADMIN, ANALYST, CLIENT");
    }
  }
}
