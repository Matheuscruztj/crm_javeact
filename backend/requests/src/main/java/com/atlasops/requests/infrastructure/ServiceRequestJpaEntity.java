package com.atlasops.requests.infrastructure;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** JPA entity mapping to the {@code service_requests} database table. */
@Entity
@Table(name = "service_requests")
public class ServiceRequestJpaEntity {

  @Id
  @Column(name = "id", nullable = false, updatable = false)
  private String id;

  @Column(name = "title", nullable = false)
  private String title;

  @Column(name = "description", nullable = false, length = 5000)
  private String description;

  @Column(name = "status", nullable = false)
  private String status;

  @Column(name = "priority", nullable = false)
  private String priority;

  @Column(name = "customer_id", nullable = false)
  private String customerId;

  @Column(name = "assigned_analyst_id")
  private String assignedAnalystId;

  @Column(name = "tenant_id", nullable = false)
  private String tenantId;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "assigned_at")
  private Instant assignedAt;

  @ElementCollection
  @CollectionTable(
      name = "service_request_documents",
      joinColumns = @JoinColumn(name = "request_id"))
  @Column(name = "document_id")
  private List<String> documentIds = new ArrayList<>();

  protected ServiceRequestJpaEntity() {
    // Required by JPA
  }

  public ServiceRequestJpaEntity(
      String id,
      String title,
      String description,
      String status,
      String priority,
      String customerId,
      String assignedAnalystId,
      String tenantId,
      Instant createdAt,
      Instant assignedAt,
      List<String> documentIds) {
    this.id = id;
    this.title = title;
    this.description = description;
    this.status = status;
    this.priority = priority;
    this.customerId = customerId;
    this.assignedAnalystId = assignedAnalystId;
    this.tenantId = tenantId;
    this.createdAt = createdAt;
    this.assignedAt = assignedAt;
    this.documentIds = documentIds != null ? new ArrayList<>(documentIds) : new ArrayList<>();
  }

  public String getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getPriority() {
    return priority;
  }

  public void setPriority(String priority) {
    this.priority = priority;
  }

  public String getCustomerId() {
    return customerId;
  }

  public String getAssignedAnalystId() {
    return assignedAnalystId;
  }

  public void setAssignedAnalystId(String assignedAnalystId) {
    this.assignedAnalystId = assignedAnalystId;
  }

  public String getTenantId() {
    return tenantId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getAssignedAt() {
    return assignedAt;
  }

  public void setAssignedAt(Instant assignedAt) {
    this.assignedAt = assignedAt;
  }

  public List<String> getDocumentIds() {
    return documentIds;
  }

  public void setDocumentIds(List<String> documentIds) {
    this.documentIds = documentIds;
  }
}
