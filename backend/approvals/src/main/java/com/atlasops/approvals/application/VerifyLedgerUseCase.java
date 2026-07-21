package com.atlasops.approvals.application;

import com.atlasops.approvals.domain.ApprovalLedgerEntry;
import com.atlasops.approvals.domain.ports.ApprovalLedgerRepository;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Use case for verifying the integrity of the approval ledger hash chain
 * for a specific approval.
 *
 * <p>Validates: Requirements 13.8
 */
public class VerifyLedgerUseCase {

    private final ApprovalLedgerRepository ledgerRepository;

    public VerifyLedgerUseCase(ApprovalLedgerRepository ledgerRepository) {
        this.ledgerRepository = Objects.requireNonNull(ledgerRepository, "ledgerRepository must not be null");
    }

    /**
     * Verifies ledger integrity for a given approval.
     *
     * @param approvalId the approval identifier
     * @param tenantId   the tenant context
     * @return a map with verification results: approvalId, entriesCount, integrityValid, tamperingDetected
     */
    public Map<String, Object> execute(String approvalId, String tenantId) {
        Objects.requireNonNull(approvalId, "approvalId must not be null");
        Objects.requireNonNull(tenantId, "tenantId must not be null");

        List<ApprovalLedgerEntry> entries = ledgerRepository.findByApprovalId(approvalId, tenantId);
        boolean valid = entries.stream().allMatch(ApprovalLedgerEntry::isValid);

        return Map.of(
                "approvalId", approvalId,
                "entriesCount", entries.size(),
                "integrityValid", valid,
                "tamperingDetected", !valid);
    }
}
