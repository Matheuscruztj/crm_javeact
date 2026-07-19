package com.atlasops.users.domain.ports;

import com.atlasops.users.domain.User;
import java.util.Optional;

/** Port defining persistence operations for User aggregates. */
public interface UserRepository {

  /**
   * Finds a user by their unique identifier.
   *
   * @param id the user identifier
   * @return the user if found
   */
  Optional<User> findById(String id);

  /**
   * Finds a user by email within a specific tenant (case-insensitive).
   *
   * @param email the user email address
   * @param tenantId the tenant identifier
   * @return the user if found
   */
  Optional<User> findByEmailAndTenantId(String email, String tenantId);

  /**
   * Checks whether a user with the given email already exists within a tenant.
   *
   * @param email the user email address
   * @param tenantId the tenant identifier
   * @return true if a user with that email exists in the tenant
   */
  boolean existsByEmailAndTenantId(String email, String tenantId);

  /**
   * Persists a user (create or update).
   *
   * @param user the user to persist
   * @return the persisted user
   */
  User save(User user);
}
