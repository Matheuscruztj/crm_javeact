package com.atlasops.approvals.infrastructure;

import com.atlasops.approvals.domain.ApprovalLedgerEntry;
import com.atlasops.approvals.domain.ports.ApprovalLedgerRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * JPA adapter for the approval ledger.
 * Validates: P0.F.1 — Approval Ledger (append-only hash chain)
 */
@Component
public class JpaApprovalLedgerRepositoryAdapter implements ApprovalLedgerRepository {

    private final SpringDataApprovalLedgerRepository springDataRepository;

    public JpaApprovalLedgerRepositoryAdapter(
            SpringDataApprovalLedgerRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public ApprovalLedgerEntry append(ApprovalLedgerEntry entry) {
        ApprovalLedgerJpaEntity entity = toEntity(entry);
        springDataRepository.save(entity);
        return entry;
    }

    @Override
    public Optional<ApprovalLedgerEntry> findLastByTenantId(String tenantId) {
        return springDataRepository.findLastByTenantId(tenantId).map(this::toDomain);
    }

    @Override
    public List<ApprovalLedgerEntry> findByApprovalId(String approvalId, String tenantId) {
        return springDataRepository
                .findByApprovalIdAndTenantId(approvalId, tenantId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public long nextSequenceNumber(String tenantId) {
        return springDataRepository.findMaxSequenceByTenantId(tenantId) + 1;
    }

    private ApprovalLedgerEntry toDomain(ApprovalLedgerJpaEntity entity) {
        return new ApprovalLedgerEntry(
                entity.getSequenceNumber(),
                entity.getApprovalId(),
                entity.getStatus(),
                entity.getDecisionBy(),
                entity.getOccurredAt(),
                entity.getTenantId(),
                entity.getPreviousHash(),
                entity.getEntryHash());
    }

    private ApprovalLedgerJpaEntity toEntity(ApprovalLedgerEntry entry) {
        return new ApprovalLedgerJpaEntity(
                entry.sequenceNumber(),
                entry.approvalId(),
                entry.status(),
                entry.decisionBy(),
                entry.occurredAt(),
                entry.tenantId(),
                entry.previousHash(),
                entry.entryHash());
    }
}
