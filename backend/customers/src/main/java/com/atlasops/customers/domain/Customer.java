package com.atlasops.customers.domain;

import com.atlasops.shared.domain.AggregateRoot;
import com.atlasops.shared.domain.types.Email;
import com.atlasops.shared.domain.types.TenantId;
import java.time.Instant;
import java.util.Objects;

/**
 * Customer aggregate root representing a business customer within a tenant. Identity is a String
 * UUID.
 */
public final class Customer extends AggregateRoot<String> {

  private static final int NAME_MIN_LENGTH = 1;
  private static final int NAME_MAX_LENGTH = 150;

  private String name;
  private Email email;
  private Address address;
  private CustomerStatus status;
  private final TenantId tenantId;
  private final Instant createdAt;
  private Instant updatedAt;

  private Customer(
      String id,
      String name,
      Email email,
      Address address,
      CustomerStatus status,
      TenantId tenantId,
      Instant createdAt,
      Instant updatedAt) {
    super(id);
    validateName(name);
    this.name = name;
    this.email = Objects.requireNonNull(email, "Email must not be null");
    this.address = address;
    this.status = Objects.requireNonNull(status, "Status must not be null");
    this.tenantId = Objects.requireNonNull(tenantId, "TenantId must not be null");
    this.createdAt = Objects.requireNonNull(createdAt, "CreatedAt must not be null");
    this.updatedAt = Objects.requireNonNull(updatedAt, "UpdatedAt must not be null");
  }

  /**
   * Factory method to create a new Customer with ACTIVE status.
   *
   * @param id unique identifier (UUID string)
   * @param name customer name (1-150 characters)
   * @param email validated email address
   * @param address optional address with optional coordinates
   * @param tenantId tenant this customer belongs to
   * @param now current timestamp for createdAt and updatedAt
   * @return a new Customer instance with ACTIVE status
   */
  public static Customer create(
      String id, String name, Email email, Address address, TenantId tenantId, Instant now) {
    return new Customer(id, name, email, address, CustomerStatus.ACTIVE, tenantId, now, now);
  }

  /**
   * Reconstitutes a Customer from persisted data, preserving all fields as-is.
   *
   * @param id unique identifier
   * @param name customer name
   * @param email customer email
   * @param address optional address
   * @param status customer status
   * @param tenantId tenant identifier
   * @param createdAt creation timestamp
   * @param updatedAt last update timestamp
   * @return a reconstituted Customer instance
   */
  public static Customer reconstitute(
      String id,
      String name,
      Email email,
      Address address,
      CustomerStatus status,
      TenantId tenantId,
      Instant createdAt,
      Instant updatedAt) {
    return new Customer(id, name, email, address, status, tenantId, createdAt, updatedAt);
  }

  /**
   * Deactivates this customer, preventing new requests from being created.
   *
   * @param now current timestamp
   * @throws IllegalStateException if the customer is already INACTIVE
   */
  public void deactivate(Instant now) {
    Objects.requireNonNull(now, "Timestamp must not be null");
    if (this.status == CustomerStatus.INACTIVE) {
      throw new IllegalStateException("Customer is already inactive");
    }
    this.status = CustomerStatus.INACTIVE;
    this.updatedAt = now;
  }

  /**
   * Activates an inactive customer.
   *
   * @param now current timestamp
   * @throws IllegalStateException if the customer is already ACTIVE
   */
  public void activate(Instant now) {
    Objects.requireNonNull(now, "Timestamp must not be null");
    if (this.status == CustomerStatus.ACTIVE) {
      throw new IllegalStateException("Customer is already active");
    }
    this.status = CustomerStatus.ACTIVE;
    this.updatedAt = now;
  }

  /**
   * Updates customer fields.
   *
   * @param name new name (1-150 characters)
   * @param email new email
   * @param address new address (nullable)
   * @param now current timestamp
   */
  public void update(String name, Email email, Address address, Instant now) {
    Objects.requireNonNull(now, "Timestamp must not be null");
    validateName(name);
    this.name = name;
    this.email = Objects.requireNonNull(email, "Email must not be null");
    this.address = address;
    this.updatedAt = now;
  }

  /**
   * Checks whether this customer is currently active.
   *
   * @return true if the customer status is ACTIVE
   */
  public boolean isActive() {
    return this.status == CustomerStatus.ACTIVE;
  }

  private static void validateName(String name) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Customer name must not be null or empty");
    }
    if (name.length() < NAME_MIN_LENGTH || name.length() > NAME_MAX_LENGTH) {
      throw new IllegalArgumentException(
          "Customer name must be between "
              + NAME_MIN_LENGTH
              + " and "
              + NAME_MAX_LENGTH
              + " characters, got: "
              + name.length());
    }
  }

  public String getName() {
    return name;
  }

  public Email getEmail() {
    return email;
  }

  public Address getAddress() {
    return address;
  }

  public CustomerStatus getStatus() {
    return status;
  }

  public TenantId getTenantId() {
    return tenantId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
