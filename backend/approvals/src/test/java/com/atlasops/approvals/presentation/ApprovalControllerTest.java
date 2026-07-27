package com.atlasops.approvals.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.atlasops.approvals.application.AppendToLedgerUseCase;
import com.atlasops.approvals.presentation.RejectApprovalRequest;
import com.atlasops.approvals.application.ApproveDocumentUseCase;
import com.atlasops.approvals.application.CancelApprovalUseCase;
import com.atlasops.approvals.application.RejectDocumentUseCase;
import com.atlasops.approvals.domain.Approval;
import com.atlasops.approvals.domain.ApprovalStatus;
import com.atlasops.approvals.domain.ports.ApprovalLedgerRepository;
import com.atlasops.approvals.domain.ports.ApprovalRepository;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Unit tests for ApprovalController.
 * Validates: Requirements 13.2, 13.3, 13.8
 */
@ExtendWith(MockitoExtension.class)
class ApprovalControllerTest {

    private static final String TENANT = "tenant-alpha";
    private static final Instant NOW = Instant.parse("2025-01-15T10:00:00Z");

    @Mock private ApproveDocumentUseCase approveDocumentUseCase;
    @Mock private RejectDocumentUseCase rejectDocumentUseCase;
    @Mock private CancelApprovalUseCase cancelApprovalUseCase;
    @Mock private ApprovalRepository approvalRepository;
    @Mock private ApprovalLedgerRepository approvalLedgerRepository;
    @Mock private AppendToLedgerUseCase appendToLedgerUseCase;

    private ApprovalController controller;

    @BeforeEach
    void setUp() {
        controller = new ApprovalController(
                approveDocumentUseCase, rejectDocumentUseCase, cancelApprovalUseCase,
                approvalRepository, approvalLedgerRepository, appendToLedgerUseCase);
    }

    private Approval aPendingApproval() {
        return Approval.createPending("appr-001", TENANT, "doc-001", NOW);
    }

    @Test
    void should_listPendingApprovals_when_noStatusFilter() {
        when(approvalRepository.findPendingByTenantId(any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(aPendingApproval()), PageRequest.of(0, 20), 1));

        ResponseEntity<PageResponse<ApprovalResponse>> response =
                controller.list(TENANT, null, 0, 20);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().content()).hasSize(1);
        assertThat(response.getBody().content().get(0).status()).isEqualTo("PENDING");
    }

    @Test
    void should_approveDocument_when_validRequest() {
        Approval approved = Approval.createPending("appr-001", TENANT, "doc-001", NOW);
        approved.approve("analyst-001", "ANALYST", NOW);
        when(approveDocumentUseCase.execute(any())).thenReturn(approved);

        ResponseEntity<ApprovalResponse> response =
                controller.approve(TENANT, "analyst-001", "ANALYST", "corr-001", "appr-001");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().status()).isEqualTo("APPROVED");
    }

    @Test
    void should_rejectDocument_when_reasonProvided() {
        Approval rejected = Approval.createPending("appr-001", TENANT, "doc-001", NOW);
        String reason = "Missing signature and date information";
        rejected.reject("analyst-001", reason, "corr-001", NOW);
        when(rejectDocumentUseCase.execute(any())).thenReturn(rejected);

        var request = new RejectApprovalRequest(reason);
        ResponseEntity<ApprovalResponse> response =
                controller.reject(TENANT, "analyst-001", "ANALYST", "corr-001", "appr-001", request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().status()).isEqualTo("REJECTED");
    }

    @Test
    void should_verifyLedger_when_noEntries() {
        when(approvalLedgerRepository.findByApprovalId("appr-001", TENANT)).thenReturn(List.of());

        ResponseEntity<java.util.Map<String, Object>> response =
                controller.verifyLedger(TENANT, "appr-001");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("integrityValid")).isEqualTo(true);
        assertThat(response.getBody().get("entriesCount")).isEqualTo(0);
    }
}
