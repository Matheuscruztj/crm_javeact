package com.atlasops.approvals.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.atlasops.approvals.domain.ApprovalLedgerEntry;
import com.atlasops.approvals.domain.ports.ApprovalLedgerRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class VerifyLedgerUseCaseTest {

  @Test
  void should_returnIntegrityValid_whenAllEntriesAreValid() {
    ApprovalLedgerRepository repository = mock(ApprovalLedgerRepository.class);
    VerifyLedgerUseCase useCase = new VerifyLedgerUseCase(repository);
    ApprovalLedgerEntry entry =
        ApprovalLedgerEntry.create(
            1L,
            "approval-1",
            "APPROVED",
            "analyst-1",
            Instant.parse("2026-07-27T10:00:00Z"),
            "tenant-1",
            ApprovalLedgerEntry.GENESIS_HASH);

    when(repository.findByApprovalId("approval-1", "tenant-1")).thenReturn(List.of(entry));

    Map<String, Object> result = useCase.execute("approval-1", "tenant-1");

    assertThat(result).containsEntry("approvalId", "approval-1");
    assertThat(result).containsEntry("entriesCount", 1);
    assertThat(result).containsEntry("integrityValid", true);
    assertThat(result).containsEntry("tamperingDetected", false);
  }

  @Test
  void should_detectTampering_whenAnyEntryIsInvalid() {
    ApprovalLedgerRepository repository = mock(ApprovalLedgerRepository.class);
    VerifyLedgerUseCase useCase = new VerifyLedgerUseCase(repository);
    ApprovalLedgerEntry invalidEntry =
        new ApprovalLedgerEntry(
            1L,
            "approval-1",
            "APPROVED",
            "analyst-1",
            Instant.parse("2026-07-27T10:00:00Z"),
            "tenant-1",
            ApprovalLedgerEntry.GENESIS_HASH,
            "invalid-hash");

    when(repository.findByApprovalId("approval-1", "tenant-1")).thenReturn(List.of(invalidEntry));

    Map<String, Object> result = useCase.execute("approval-1", "tenant-1");

    assertThat(result).containsEntry("integrityValid", false);
    assertThat(result).containsEntry("tamperingDetected", true);
  }
}
