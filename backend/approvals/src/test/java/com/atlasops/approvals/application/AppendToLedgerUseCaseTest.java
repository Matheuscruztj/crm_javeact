package com.atlasops.approvals.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlasops.approvals.domain.Approval;
import com.atlasops.approvals.domain.ApprovalLedgerEntry;
import com.atlasops.approvals.domain.ports.ApprovalLedgerRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AppendToLedgerUseCaseTest {

  private static final Instant CREATED_AT = Instant.parse("2025-01-15T09:00:00Z");
  private static final Instant DECIDED_AT = Instant.parse("2025-01-15T10:00:00Z");
  private static final String TENANT_ID = "tenant-alpha";

  @Mock private ApprovalLedgerRepository ledgerRepository;

  private AppendToLedgerUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new AppendToLedgerUseCase(ledgerRepository);
  }

  @Test
  void should_appendLedgerEntry_when_approvalDecisionMade() {
    Approval approved = Approval.createPending("appr-001", TENANT_ID, "doc-001", CREATED_AT);
    approved.approve("analyst-001", "corr-001", DECIDED_AT);

    when(ledgerRepository.findLastByTenantId(TENANT_ID)).thenReturn(Optional.empty());
    when(ledgerRepository.nextSequenceNumber(TENANT_ID)).thenReturn(1L);
    when(ledgerRepository.append(any())).thenAnswer(i -> i.getArgument(0));

    ApprovalLedgerEntry entry = useCase.execute(approved);

    assertThat(entry.approvalId()).isEqualTo("appr-001");
    assertThat(entry.status()).isEqualTo("APPROVED");
    assertThat(entry.decisionBy()).isEqualTo("analyst-001");
    assertThat(entry.tenantId()).isEqualTo(TENANT_ID);
    assertThat(entry.sequenceNumber()).isEqualTo(1L);
    assertThat(entry.previousHash()).isEqualTo(ApprovalLedgerEntry.GENESIS_HASH);
    assertThat(entry.entryHash()).isNotBlank();
    assertThat(entry.isValid()).isTrue();
  }

  @Test
  void should_chainHashes_when_previousEntryExists() {
    Approval approved = Approval.createPending("appr-002", TENANT_ID, "doc-002", CREATED_AT);
    approved.approve("analyst-001", "corr-002", DECIDED_AT);

    ApprovalLedgerEntry previous = ApprovalLedgerEntry.create(
        1L, "appr-001", "APPROVED", "analyst-001", CREATED_AT, TENANT_ID,
        ApprovalLedgerEntry.GENESIS_HASH);

    when(ledgerRepository.findLastByTenantId(TENANT_ID)).thenReturn(Optional.of(previous));
    when(ledgerRepository.nextSequenceNumber(TENANT_ID)).thenReturn(2L);
    when(ledgerRepository.append(any())).thenAnswer(i -> i.getArgument(0));

    ApprovalLedgerEntry entry = useCase.execute(approved);

    assertThat(entry.previousHash()).isEqualTo(previous.entryHash());
    assertThat(entry.sequenceNumber()).isEqualTo(2L);
    assertThat(entry.isValid()).isTrue();
  }

  @Test
  void should_throwNullPointer_when_approvalIsNull() {
    assertThatThrownBy(() -> useCase.execute(null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void should_persistToRepository_when_entryCreated() {
    Approval approved = Approval.createPending("appr-003", TENANT_ID, "doc-003", CREATED_AT);
    approved.approve("analyst-001", "corr-003", DECIDED_AT);

    when(ledgerRepository.findLastByTenantId(TENANT_ID)).thenReturn(Optional.empty());
    when(ledgerRepository.nextSequenceNumber(TENANT_ID)).thenReturn(1L);
    when(ledgerRepository.append(any())).thenAnswer(i -> i.getArgument(0));

    useCase.execute(approved);

    ArgumentCaptor<ApprovalLedgerEntry> captor = ArgumentCaptor.forClass(ApprovalLedgerEntry.class);
    verify(ledgerRepository).append(captor.capture());
    assertThat(captor.getValue().isValid()).isTrue();
  }
}
