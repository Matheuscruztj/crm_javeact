package com.atlasops.approvals.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * JPA entity for the approval_ledger table.
 * Validates: P0.F.1 — Approval Ledger (append-only hash chain)
 */
@Entity
@Table(name = "approval_ledger")
public class ApprovalLedgerJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sequence_number", nullable = false)
    private long sequenceNumber;

    @Column(name = "approval_id", nullable = false, length = 36)
    private String approvalId;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "decision_by", nullable = false)
    private String decisionBy;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "tenant_id", nullable = false, length = 100)
    private String tenantId;

    @Column(name = "previous_hash", nullable = false, length = 64)
    private String previousHash;

    @Column(name = "entry_hash", nullable = false, length = 64, unique = true)
    private String entryHash;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected ApprovalLedgerJpaEntity() {}

    public ApprovalLedgerJpaEntity(
            long sequenceNumber,
            String approvalId,
            String status,
            String decisionBy,
            Instant occurredAt,
            String tenantId,
            String previousHash,
            String entryHash) {
        this.sequenceNumber = sequenceNumber;
        this.approvalId = approvalId;
        this.status = status;
        this.decisionBy = decisionBy;
        this.occurredAt = occurredAt;
        this.tenantId = tenantId;
        this.previousHash = previousHash;
        this.entryHash = entryHash;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public String getApprovalId() {
        return approvalId;
    }

    public String getStatus() {
        return status;
    }

    public String getDecisionBy() {
        return decisionBy;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public String getEntryHash() {
        return entryHash;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
