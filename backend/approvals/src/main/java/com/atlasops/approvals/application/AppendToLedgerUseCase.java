package com.atlasops.approvals.application;

import com.atlasops.approvals.domain.Approval;
import com.atlasops.approvals.domain.ApprovalLedgerEntry;
import com.atlasops.approvals.domain.ports.ApprovalLedgerRepository;
import java.util.Objects;

/**
 * Use case for appending an approval decision to the immutable ledger.
 *
 * <p>Called after every approval decision (approve, reject, cancel) to
 * maintain an auditable hash chain.
 *
 * <p>Validates: P0.F.1 — Approval Ledger (append-only hash chain)
 */
public class AppendToLedgerUseCase {

    private final ApprovalLedgerRepository ledgerRepository;

    public AppendToLedgerUseCase(ApprovalLedgerRepository ledgerRepository) {
        this.ledgerRepository = Objects.requireNonNull(ledgerRepository);
    }

    /**
     * Appends the current state of an approval decision to the ledger.
     *
     * @param approval the approval with its final decision
     * @return the created ledger entry
     */
    public ApprovalLedgerEntry execute(Approval approval) {
        Objects.requireNonNull(approval, "Approval must not be null");

        String previousHash = ledgerRepository.findLastByTenantId(approval.getTenantId())
                .map(ApprovalLedgerEntry::entryHash)
                .orElse(ApprovalLedgerEntry.GENESIS_HASH);

        long sequence = ledgerRepository.nextSequenceNumber(approval.getTenantId());

        ApprovalLedgerEntry entry = ApprovalLedgerEntry.create(
                sequence,
                approval.getId(),
                approval.getStatus().name(),
                approval.getDecisionBy() != null ? approval.getDecisionBy() : "system",
                approval.getDecidedAt() != null ? approval.getDecidedAt() : approval.getCreatedAt(),
                approval.getTenantId(),
                previousHash);

        return ledgerRepository.append(entry);
    }
}
