package com.atlasops.audit.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlasops.audit.domain.AuditEntry;
import com.atlasops.audit.domain.LedgerEntry;
import com.atlasops.audit.domain.ports.LedgerRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AppendToGlobalLedgerUseCaseTest {

  private static final Instant NOW = Instant.parse("2025-01-15T10:00:00Z");
  private static final String TENANT = "tenant-alpha";

  @Mock private LedgerRepository ledgerRepository;

  private AppendToGlobalLedgerUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new AppendToGlobalLedgerUseCase(ledgerRepository);
  }

  @Test
  void should_appendEntry_when_firstEntryInTenant() {
    AuditEntry auditEntry = AuditEntry.create(
        "audit-001", "LOGIN", "user-001", TENANT, "USER", "user-001",
        "corr-001", "{}", NOW);
    String auditJson = "{\"action\":\"LOGIN\",\"actor\":\"user-001\"}";

    when(ledgerRepository.findLast(TENANT)).thenReturn(Optional.empty());
    when(ledgerRepository.nextSequence(TENANT)).thenReturn(1L);
    when(ledgerRepository.append(any())).thenAnswer(i -> i.getArgument(0));

    LedgerEntry result = useCase.execute(auditEntry, auditJson);

    assertThat(result.sequenceNumber()).isEqualTo(1L);
    assertThat(result.previousHash()).isEqualTo(LedgerEntry.GENESIS_HASH);
    assertThat(result.eventType()).isEqualTo("LOGIN");
    assertThat(result.tenantId()).isEqualTo(TENANT);
    assertThat(result.isValid()).isTrue();
    verify(ledgerRepository).append(any());
  }

  @Test
  void should_chainWithPreviousHash_when_previousEntryExists() {
    AuditEntry auditEntry = AuditEntry.create(
        "audit-002", "CREATE_CUSTOMER", "user-001", TENANT, "CUSTOMER",
        "cust-001", "corr-002", "{}", NOW.plusSeconds(60));

    LedgerEntry previous = LedgerEntry.create(
        1L, "LOGIN", "prev-payload-hash", LedgerEntry.GENESIS_HASH, NOW, TENANT);

    when(ledgerRepository.findLast(TENANT)).thenReturn(Optional.of(previous));
    when(ledgerRepository.nextSequence(TENANT)).thenReturn(2L);
    when(ledgerRepository.append(any())).thenAnswer(i -> i.getArgument(0));

    LedgerEntry result = useCase.execute(auditEntry, "{\"action\":\"CREATE_CUSTOMER\"}");

    assertThat(result.previousHash()).isEqualTo(previous.currentHash());
    assertThat(result.sequenceNumber()).isEqualTo(2L);
    assertThat(result.isValid()).isTrue();
  }

  @Test
  void should_useSha256OfJson_asPayloadHash() {
    AuditEntry auditEntry = AuditEntry.create(
        "audit-001", "LOGIN", "user-001", TENANT, "USER", "user-001",
        "corr-001", "{}", NOW);

    when(ledgerRepository.findLast(TENANT)).thenReturn(Optional.empty());
    when(ledgerRepository.nextSequence(TENANT)).thenReturn(1L);
    when(ledgerRepository.append(any())).thenAnswer(i -> i.getArgument(0));

    String json = "{\"action\":\"LOGIN\"}";
    useCase.execute(auditEntry, json);

    ArgumentCaptor<LedgerEntry> captor = ArgumentCaptor.forClass(LedgerEntry.class);
    verify(ledgerRepository).append(captor.capture());

    // payloadHash should be 64-char SHA-256 hex
    assertThat(captor.getValue().payloadHash()).hasSize(64);
    assertThat(captor.getValue().payloadHash()).matches("[0-9a-f]+");
  }

  @Test
  void should_throwNullPointer_when_auditEntryIsNull() {
    assertThatThrownBy(() -> useCase.execute(null, "{}"))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void should_throwNullPointer_when_auditJsonIsNull() {
    AuditEntry auditEntry = AuditEntry.create(
        "audit-001", "LOGIN", "user-001", TENANT, "USER", "user-001", "corr-001", "{}", NOW);
    assertThatThrownBy(() -> useCase.execute(auditEntry, null))
        .isInstanceOf(NullPointerException.class);
  }
}
