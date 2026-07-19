package com.atlasops.tenants.domain;

import com.atlasops.shared.domain.AggregateRoot;
import java.time.Instant;
import java.util.Objects;

/**
 * Tenant aggregate root. Represents an isolated organizational unit within the multi-tenant
 * platform.
 */
public final class Tenant extends AggregateRoot<String> {

  private final TenantName name;
  private TenantStatus status;
  private final Instant createdAt;
  private Instant updatedAt;

  private Tenant(
      String id, TenantName name, TenantStatus status, Instant createdAt, Instant updatedAt) {
    super(id);
    this.name = Objects.requireNonNull(name, "Tenant name must not be null");
    this.status = Objects.requireNonNull(status, "Tenant status must not be null");
    this.createdAt = Objects.requireNonNull(createdAt, "Created at must not be null");
    this.updatedAt = Objects.requireNonNull(updatedAt, "Updated at must not be null");
  }

  /**
   * Factory method to create a new tenant with ACTIVE status.
   *
   * @param id the tenant identifier (UUID string)
   * @param name the tenant name value object
   * @param now the current timestamp
   * @return a new Tenant instance with ACTIVE status
   */
  public static Tenant create(String id, TenantName name, Instant now) {
    Objects.requireNonNull(id, "Tenant id must not be null");
    Objects.requireNonNull(name, "Tenant name must not be null");
    Objects.requireNonNull(now, "Timestamp must not be null");
    return new Tenant(id, name, TenantStatus.ACTIVE, now, now);
  }

  /**
   * Reconstitutes an existing tenant from persisted state.
   *
   * @param id the tenant identifier
   * @param name the tenant name value object
   * @param status the tenant status
   * @param createdAt creation timestamp
   * @param updatedAt last update timestamp
   * @return a reconstituted Tenant instance
   */
  public static Tenant reconstitute(
      String id, TenantName name, TenantStatus status, Instant createdAt, Instant updatedAt) {
    Objects.requireNonNull(id, "Tenant id must not be null");
    return new Tenant(id, name, status, createdAt, updatedAt);
  }

  /**
   * Deactivates this tenant, setting its status to INACTIVE.
   *
   * @param now the current timestamp
   */
  public void deactivate(Instant now) {
    Objects.requireNonNull(now, "Timestamp must not be null");
    this.status = TenantStatus.INACTIVE;
    this.updatedAt = now;
  }

  public TenantName getName() {
    return name;
  }

  public TenantStatus getStatus() {
    return status;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
