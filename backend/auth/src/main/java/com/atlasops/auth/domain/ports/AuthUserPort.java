package com.atlasops.auth.domain.ports;

import java.util.Optional;

/**
 * Port for looking up user credentials during authentication. Decouples the auth module from the
 * users module to avoid circular dependencies. The implementation adapter (in app-boot or users
 * infrastructure) bridges to the UserRepository.
 */
public interface AuthUserPort {

  /**
   * Finds a user's authentication data by email and tenant.
   *
   * @param email the user email (case-insensitive lookup)
   * @param tenantId the tenant identifier
   * @return the user authentication data if found
   */
  Optional<AuthUserData> findByEmailAndTenantId(String email, String tenantId);

  /**
   * Minimal user data needed for authentication.
   *
   * @param userId the user's unique identifier
   * @param email the user's email
   * @param passwordHash the bcrypt-hashed password
   * @param role the user's role name (ADMIN, ANALYST, CLIENT)
   * @param tenantId the tenant identifier
   * @param active whether the user is currently active
   */
  record AuthUserData(
      String userId,
      String email,
      String passwordHash,
      String role,
      String tenantId,
      boolean active) {}
}
