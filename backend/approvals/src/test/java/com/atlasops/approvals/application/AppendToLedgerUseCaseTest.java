package com.atlasops.approvals.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlasops.approvals.domain.Approval;
import com.atlasops.approvals.domain.ApprovalLedgerEntry;
import com.atlasops.approvals.domain.ApprovalStatus;
import com.atlasops.approvals.domain.ports.ApprovalLedgerRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link AppendToLedgerUseCase}.
 * Validates: P0.F.1 — Approval Ledger (append-only hash chain)
 */
@ExtendWith(MockitoExtension.class)
class AppendToLedgerUseCaseTest {

    @Mock
    private ApprovalLedgerRepository ledgerRepository;

    @InjectMocks
    private AppendToLedgerUseCase useCase;

    private static final Instant CREATED = Instant.parse("2025-01-15T09:00:00Z");
    private static final Instant DECIDED = Instant.parse("2025-01-15T10:00:00Z");

    @Test
    void should_appendGenesisEntry_when_noLedgerEntriesExist() {
        Approval approval = buildApprovedApproval("appr-001", "tenant-alpha");
        when(ledgerRepository.findLastByTenantId("tenant-alpha")).thenReturn(Optional.empty());
        when(ledgerRepository.nextSequenceNumber("tenant-alpha")).thenReturn(1L);
        when(ledgerRepository.append(any())).thenAnswer(inv -> inv.getArgument(0));

        ApprovalLedgerEntry result = useCase.execute(approval);

        assertThat(result.sequenceNumber()).isEqualTo(1L);
        assertThat(result.approvalId()).isEqualTo("appr-001");
        assertThat(result.previousHash()).isEqualTo(ApprovalLedgerEntry.GENESIS_HASH);
        assertThat(result.isValid()).isTrue();
        verify(ledgerRepository).append(any(ApprovalLedgerEntry.class));
    }

    @Test
    void should_appendWithPreviousHash_when_ledgerHasEntries() {
        Approval approval = buildApprovedApproval("appr-002", "tenant-alpha");

        ApprovalLedgerEntry previous = ApprovalLedgerEntry.create(
                1L, "appr-001", "APPROVED", "analyst-1", DECIDED, "tenant-alpha",
                ApprovalLedgerEntry.GENESIS_HASH);

        when(ledgerRepository.findLastByTenantId("tenant-alpha")).thenReturn(Optional.of(previous));
        when(ledgerRepository.nextSequenceNumber("tenant-alpha")).thenReturn(2L);
        when(ledgerRepository.append(any())).thenAnswer(inv -> inv.getArgument(0));

        ApprovalLedgerEntry result = useCase.execute(approval);

        assertThat(result.sequenceNumber()).isEqualTo(2L);
        assertThat(result.previousHash()).isEqualTo(previous.entryHash());
        assertThat(result.isValid()).isTrue();
    }

    @Test
    void should_computeValidHash_when_appendingEntry() {
        Approval approval = buildApprovedApproval("appr-hash", "tenant-beta");
        when(ledgerRepository.findLastByTenantId("tenant-beta")).thenReturn(Optional.empty());
        when(ledgerRepository.nextSequenceNumber("tenant-beta")).thenReturn(1L);
        when(ledgerRepository.append(any())).thenAnswer(inv -> inv.getArgument(0));

        ApprovalLedgerEntry result = useCase.execute(approval);

        // Hash must be 64 hex characters (SHA-256)
        assertThat(result.entryHash()).matches("[0-9a-f]{64}");
        assertThat(result.isValid()).isTrue();
    }

    @Test
    void should_throwException_when_approvalIsNull() {
        assertThatThrownBy(() -> useCase.execute(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Approval must not be null");
    }

    @Test
    void should_captureDecisionByFromApproval_when_decisionIsSet() {
        Approval approval = buildApprovedApproval("appr-capture", "tenant-alpha");
        when(ledgerRepository.findLastByTenantId("tenant-alpha")).thenReturn(Optional.empty());
        when(ledgerRepository.nextSequenceNumber("tenant-alpha")).thenReturn(1L);
        when(ledgerRepository.append(any())).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(approval);

        ArgumentCaptor<ApprovalLedgerEntry> captor = ArgumentCaptor.forClass(ApprovalLedgerEntry.class);
        verify(ledgerRepository).append(captor.capture());

        assertThat(captor.getValue().decisionBy()).isEqualTo("analyst-001");
        assertThat(captor.getValue().status()).isEqualTo(ApprovalStatus.APPROVED.name());
        assertThat(captor.getValue().tenantId()).isEqualTo("tenant-alpha");
    }

    private Approval buildApprovedApproval(String id, String tenantId) {
        Approval approval = Approval.createPending(id, tenantId, "doc-001", CREATED);
        approval.approve("analyst-001", "corr-001", DECIDED);
        return approval;
    }
}
