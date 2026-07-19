package com.atlasops.customers.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** JPA entity mapping to the {@code user_customer_associations} junction table. */
@Entity
@Table(name = "user_customer_associations")
public class UserCustomerAssociationJpaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", nullable = false, updatable = false)
  private String id;

  @Column(name = "user_id", nullable = false)
  private String userId;

  @Column(name = "customer_id", nullable = false)
  private String customerId;

  @Column(name = "tenant_id", nullable = false)
  private String tenantId;

  protected UserCustomerAssociationJpaEntity() {
    // Required by JPA
  }

  public UserCustomerAssociationJpaEntity(String userId, String customerId, String tenantId) {
    this.userId = userId;
    this.customerId = customerId;
    this.tenantId = tenantId;
  }

  public String getId() {
    return id;
  }

  public String getUserId() {
    return userId;
  }

  public String getCustomerId() {
    return customerId;
  }

  public String getTenantId() {
    return tenantId;
  }
}
