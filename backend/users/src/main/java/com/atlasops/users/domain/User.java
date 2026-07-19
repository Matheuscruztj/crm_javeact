package com.atlasops.users.domain;

import com.atlasops.shared.domain.AggregateRoot;
import java.time.Instant;
import java.util.Objects;

/** User aggregate root representing a user within a tenant. Identity is a String UUID. */
public final class User extends AggregateRoot<String> {

  private final String email;
  private final String name;
  private final String passwordHash;
  private UserRole role;
  private final String tenantId;
  private UserStatus status;
  private final Instant createdAt;
  private Instant updatedAt;

  private User(
      String id,
      String email,
      String name,
      String passwordHash,
      UserRole role,
      String tenantId,
      UserStatus status,
      Instant createdAt,
      Instant updatedAt) {
    super(id);
    this.email = Objects.requireNonNull(email, "Email must not be null");
    this.name = Objects.requireNonNull(name, "Name must not be null");
    this.passwordHash = Objects.requireNonNull(passwordHash, "PasswordHash must not be null");
    this.role = Objects.requireNonNull(role, "Role must not be null");
    this.tenantId = Objects.requireNonNull(tenantId, "TenantId must not be null");
    this.status = Objects.requireNonNull(status, "Status must not be null");
    this.createdAt = Objects.requireNonNull(createdAt, "CreatedAt must not be null");
    this.updatedAt = Objects.requireNonNull(updatedAt, "UpdatedAt must not be null");
  }

  /**
   * Factory method to create a new User with ACTIVE status.
   *
   * @param id unique identifier (UUID string)
   * @param email user email address
   * @param name user display name
   * @param passwordHash bcrypt-hashed password
   * @param role user role within the tenant
   * @param tenantId tenant this user belongs to
   * @param now current timestamp for createdAt and updatedAt
   * @return a new User instance with ACTIVE status
   */
  public static User create(
      String id,
      String email,
      String name,
      String passwordHash,
      UserRole role,
      String tenantId,
      Instant now) {
    return new User(id, email, name, passwordHash, role, tenantId, UserStatus.ACTIVE, now, now);
  }

  /**
   * Reconstitutes a User from persisted data, preserving all fields as-is.
   *
   * @param id unique identifier
   * @param email user email address
   * @param name user display name
   * @param passwordHash bcrypt-hashed password
   * @param role user role
   * @param tenantId tenant identifier
   * @param status user status
   * @param createdAt creation timestamp
   * @param updatedAt last update timestamp
   * @return a reconstituted User instance
   */
  public static User reconstitute(
      String id,
      String email,
      String name,
      String passwordHash,
      UserRole role,
      String tenantId,
      UserStatus status,
      Instant createdAt,
      Instant updatedAt) {
    return new User(id, email, name, passwordHash, role, tenantId, status, createdAt, updatedAt);
  }

  /**
   * Updates the user's role.
   *
   * @param newRole the new role to assign
   * @param now current timestamp
   */
  public void updateRole(UserRole newRole, Instant now) {
    Objects.requireNonNull(newRole, "New role must not be null");
    Objects.requireNonNull(now, "Timestamp must not be null");
    this.role = newRole;
    this.updatedAt = now;
  }

  /**
   * Deactivates this user, preventing authentication.
   *
   * @param now current timestamp
   */
  public void deactivate(Instant now) {
    Objects.requireNonNull(now, "Timestamp must not be null");
    this.status = UserStatus.INACTIVE;
    this.updatedAt = now;
  }

  /**
   * Checks whether this user is currently active.
   *
   * @return true if the user status is ACTIVE
   */
  public boolean isActive() {
    return this.status == UserStatus.ACTIVE;
  }

  public String getEmail() {
    return email;
  }

  public String getName() {
    return name;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public UserRole getRole() {
    return role;
  }

  public String getTenantId() {
    return tenantId;
  }

  public UserStatus getStatus() {
    return status;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
