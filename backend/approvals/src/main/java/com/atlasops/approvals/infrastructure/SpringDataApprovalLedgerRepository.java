package com.atlasops.approvals.infrastructure;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Spring Data JPA repository for approval ledger entries.
 */
public interface SpringDataApprovalLedgerRepository
        extends JpaRepository<ApprovalLedgerJpaEntity, Long> {

    @Query("SELECT e FROM ApprovalLedgerJpaEntity e WHERE e.tenantId = :tenantId "
            + "ORDER BY e.sequenceNumber DESC LIMIT 1")
    Optional<ApprovalLedgerJpaEntity> findLastByTenantId(@Param("tenantId") String tenantId);

    @Query("SELECT e FROM ApprovalLedgerJpaEntity e WHERE e.approvalId = :approvalId "
            + "AND e.tenantId = :tenantId ORDER BY e.sequenceNumber ASC")
    List<ApprovalLedgerJpaEntity> findByApprovalIdAndTenantId(
            @Param("approvalId") String approvalId,
            @Param("tenantId") String tenantId);

    @Query("SELECT COALESCE(MAX(e.sequenceNumber), 0) FROM ApprovalLedgerJpaEntity e "
            + "WHERE e.tenantId = :tenantId")
    long findMaxSequenceByTenantId(@Param("tenantId") String tenantId);
}
