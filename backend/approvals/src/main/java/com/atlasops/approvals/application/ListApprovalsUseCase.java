package com.atlasops.approvals.application;

import com.atlasops.approvals.domain.Approval;
import com.atlasops.approvals.domain.ApprovalStatus;
import com.atlasops.approvals.domain.ports.ApprovalRepository;
import java.util.Objects;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Use case for listing approvals with optional status filter and pagination.
 *
 * <p>Validates: Requirements 13.2
 */
public class ListApprovalsUseCase {

    private final ApprovalRepository approvalRepository;

    public ListApprovalsUseCase(ApprovalRepository approvalRepository) {
        this.approvalRepository = Objects.requireNonNull(approvalRepository, "approvalRepository must not be null");
    }

    /**
     * Executes the list query.
     *
     * @param tenantId the tenant identifier
     * @param status   optional status filter; if null/blank, returns all pending approvals
     * @param pageable pagination parameters
     * @return paginated approvals
     */
    public Page<Approval> execute(String tenantId, String status, Pageable pageable) {
        Objects.requireNonNull(tenantId, "tenantId must not be null");

        if (status != null && !status.isBlank()) {
            ApprovalStatus statusFilter = ApprovalStatus.valueOf(status);
            return approvalRepository.findByTenantIdAndStatus(tenantId, statusFilter, pageable);
        }
        return approvalRepository.findPendingByTenantId(tenantId, pageable);
    }
}
