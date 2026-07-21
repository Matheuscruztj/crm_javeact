package com.atlasops.operations.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * JPA entity for the jobs table.
 * Validates: P0.F.3, P0.I.1 — Operations module
 */
@Entity
@Table(name = "jobs")
public class JobJpaEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "type", nullable = false, length = 100)
    private String type;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "tenant_id", nullable = false, length = 100)
    private String tenantId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "progress_percent")
    private Integer progressPercent;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "reference_id", length = 36)
    private String referenceId;

    protected JobJpaEntity() {}

    public JobJpaEntity(String id, String type, String status, String tenantId,
            Instant createdAt, Instant startedAt, Instant completedAt,
            Integer progressPercent, String errorMessage, String referenceId) {
        this.id = id;
        this.type = type;
        this.status = status;
        this.tenantId = tenantId;
        this.createdAt = createdAt;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.progressPercent = progressPercent;
        this.errorMessage = errorMessage;
        this.referenceId = referenceId;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
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

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public Integer getProgressPercent() {
        return progressPercent;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public void setProgressPercent(Integer progressPercent) {
        this.progressPercent = progressPercent;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
