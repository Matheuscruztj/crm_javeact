package com.atlasops.approvals.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** JPA entity mapping to the "approvals" database table. */
@Entity
@Table(name = "approvals")
public class ApprovalJpaEntity {

  @Id
  @Column(name = "id", nullable = false, updatable = false)
  private String id;

  @Column(name = "tenant_id", nullable = false, updatable = false)
  private String tenantId;

  @Column(name = "document_id", nullable = false, updatable = false)
  private String documentId;

  @Column(name = "status", nullable = false, length = 20)
  private String status;

  @Column(name = "decision_by")
  private String decisionBy;

  @Column(name = "rejection_reason", length = 1000)
  private String rejectionReason;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "decision_at")
  private Instant decisionAt;

  protected ApprovalJpaEntity() {
    // JPA requires no-arg constructor
  }

  public ApprovalJpaEntity(
      String id,
      String tenantId,
      String documentId,
      String status,
      String decisionBy,
      String rejectionReason,
      Instant createdAt,
      Instant decisionAt) {
    this.id = id;
    this.tenantId = tenantId;
    this.documentId = documentId;
    this.status = status;
    this.decisionBy = decisionBy;
    this.rejectionReason = rejectionReason;
    this.createdAt = createdAt;
    this.decisionAt = decisionAt;
  }

  public String getId() {
    return id;
  }

  public String getTenantId() {
    return tenantId;
  }

  public String getDocumentId() {
    return documentId;
  }

  public String getStatus() {
    return status;
  }

  public String getDecisionBy() {
    return decisionBy;
  }

  public String getRejectionReason() {
    return rejectionReason;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getDecisionAt() {
    return decisionAt;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public void setDecisionBy(String decisionBy) {
    this.decisionBy = decisionBy;
  }

  public void setRejectionReason(String rejectionReason) {
    this.rejectionReason = rejectionReason;
  }

  public void setDecisionAt(Instant decisionAt) {
    this.decisionAt = decisionAt;
  }
}
