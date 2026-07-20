package com.atlasops.approvals.domain.ports;

import com.atlasops.approvals.domain.ApprovalLedgerEntry;
import java.util.List;
import java.util.Optional;

/**
 * Port for the append-only approval ledger.
 * Validates: P0.F.1 — Approval Ledger hash chain repository
 */
public interface ApprovalLedgerRepository {

    /** Appends a new entry to the ledger. The entry must have a valid hash. */
    ApprovalLedgerEntry append(ApprovalLedgerEntry entry);

    /** Returns the last entry for a tenant, used to chain the next hash. */
    Optional<ApprovalLedgerEntry> findLastByTenantId(String tenantId);

    /** Returns all entries for an approval, in sequence order. */
    List<ApprovalLedgerEntry> findByApprovalId(String approvalId, String tenantId);

    /** Returns the next sequence number for a tenant. */
    long nextSequenceNumber(String tenantId);
}
