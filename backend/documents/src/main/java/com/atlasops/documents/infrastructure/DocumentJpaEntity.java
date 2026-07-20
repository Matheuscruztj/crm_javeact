package com.atlasops.documents.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** JPA entity mapping to the "documents" database table. */
@Entity
@Table(name = "documents")
public class DocumentJpaEntity {

  @Id
  @Column(name = "id", nullable = false, updatable = false)
  private String id;

  @Column(name = "tenant_id", nullable = false, updatable = false)
  private String tenantId;

  @Column(name = "request_id")
  private String requestId;

  @Column(name = "filename", nullable = false)
  private String filename;

  @Column(name = "content_type", nullable = false, length = 100)
  private String contentType;

  @Column(name = "size_bytes", nullable = false)
  private long fileSize;

  @Column(name = "checksum_sha256", nullable = false, length = 64)
  private String checksum;

  @Column(name = "storage_path")
  private String storagePath;

  @Column(name = "status", nullable = false, length = 30)
  private String status;

  @Column(name = "analysis_result", columnDefinition = "jsonb")
  private String analysisResult;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Column(name = "legal_hold", nullable = false)
  private boolean legalHold;

  @Column(name = "legal_hold_activated_at")
  private Instant legalHoldActivatedAt;

  protected DocumentJpaEntity() {
    // JPA requires no-arg constructor
  }

  public DocumentJpaEntity(
      String id,
      String tenantId,
      String requestId,
      String filename,
      String contentType,
      long fileSize,
      String checksum,
      String storagePath,
      String status,
      String analysisResult,
      Instant createdAt,
      Instant updatedAt) {
    this.id = id;
    this.tenantId = tenantId;
    this.requestId = requestId;
    this.filename = filename;
    this.contentType = contentType;
    this.fileSize = fileSize;
    this.checksum = checksum;
    this.storagePath = storagePath;
    this.status = status;
    this.analysisResult = analysisResult;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public String getId() {
    return id;
  }

  public String getTenantId() {
    return tenantId;
  }

  public String getRequestId() {
    return requestId;
  }

  public String getFilename() {
    return filename;
  }

  public String getContentType() {
    return contentType;
  }

  public long getFileSize() {
    return fileSize;
  }

  public String getChecksum() {
    return checksum;
  }

  public String getStoragePath() {
    return storagePath;
  }

  public String getStatus() {
    return status;
  }

  public String getAnalysisResult() {
    return analysisResult;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setStoragePath(String storagePath) {
    this.storagePath = storagePath;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public void setAnalysisResult(String analysisResult) {
    this.analysisResult = analysisResult;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }

  public boolean isLegalHold() {
    return legalHold;
  }

  public void setLegalHold(boolean legalHold) {
    this.legalHold = legalHold;
  }

  public Instant getLegalHoldActivatedAt() {
    return legalHoldActivatedAt;
  }

  public void setLegalHoldActivatedAt(Instant legalHoldActivatedAt) {
    this.legalHoldActivatedAt = legalHoldActivatedAt;
  }
}
