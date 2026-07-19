package com.atlasops.users.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** JPA entity mapping to the "users" database table. */
@Entity
@Table(name = "users")
public class UserJpaEntity {

  @Id
  @Column(name = "id", nullable = false, updatable = false)
  private String id;

  @Column(name = "tenant_id", nullable = false, updatable = false)
  private String tenantId;

  @Column(name = "email", nullable = false)
  private String email;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

  @Column(name = "role", nullable = false)
  private String role;

  @Column(name = "status", nullable = false)
  private String status;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected UserJpaEntity() {
    // JPA requires no-arg constructor
  }

  public UserJpaEntity(
      String id,
      String tenantId,
      String email,
      String name,
      String passwordHash,
      String role,
      String status,
      Instant createdAt,
      Instant updatedAt) {
    this.id = id;
    this.tenantId = tenantId;
    this.email = email;
    this.name = name;
    this.passwordHash = passwordHash;
    this.role = role;
    this.status = status;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public String getId() {
    return id;
  }

  public String getTenantId() {
    return tenantId;
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

  public String getRole() {
    return role;
  }

  public String getStatus() {
    return status;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }
}
