package com.atlasops.customers.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * JPA entity mapping to the {@code customers} database table. Stores customer data with optional
 * geospatial coordinates.
 */
@Entity
@Table(name = "customers")
public class CustomerJpaEntity {

  @Id
  @Column(name = "id", nullable = false, updatable = false)
  private String id;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "email", nullable = false)
  private String email;

  @Column(name = "street")
  private String street;

  @Column(name = "city")
  private String city;

  @Column(name = "state")
  private String state;

  @Column(name = "postal_code")
  private String postalCode;

  @Column(name = "country")
  private String country;

  @Column(name = "latitude")
  private Double latitude;

  @Column(name = "longitude")
  private Double longitude;

  @Column(name = "status", nullable = false)
  private String status;

  @Column(name = "tenant_id", nullable = false)
  private String tenantId;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected CustomerJpaEntity() {
    // Required by JPA
  }

  public CustomerJpaEntity(
      String id,
      String name,
      String email,
      String street,
      String city,
      String state,
      String postalCode,
      String country,
      Double latitude,
      Double longitude,
      String status,
      String tenantId,
      Instant createdAt,
      Instant updatedAt) {
    this.id = id;
    this.name = name;
    this.email = email;
    this.street = street;
    this.city = city;
    this.state = state;
    this.postalCode = postalCode;
    this.country = country;
    this.latitude = latitude;
    this.longitude = longitude;
    this.status = status;
    this.tenantId = tenantId;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getEmail() {
    return email;
  }

  public String getStreet() {
    return street;
  }

  public String getCity() {
    return city;
  }

  public String getState() {
    return state;
  }

  public String getPostalCode() {
    return postalCode;
  }

  public String getCountry() {
    return country;
  }

  public Double getLatitude() {
    return latitude;
  }

  public Double getLongitude() {
    return longitude;
  }

  public String getStatus() {
    return status;
  }

  public String getTenantId() {
    return tenantId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }
}
