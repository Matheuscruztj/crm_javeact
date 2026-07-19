package com.atlasops.audit.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlasops.audit.domain.AuditEntry;
import com.atlasops.audit.domain.ports.AuditRepository;
import com.atlasops.shared.domain.ports.Clock;
import com.atlasops.shared.domain.ports.IdGenerator;
import java.time.Instant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

@ExtendWith(MockitoExtension.class)
class WriteAuditEntryUseCaseTest {

  private static final Instant FIXED_NOW = Instant.parse("2025-01-15T10:30:00Z");
  private static final String GENERATED_ID = "audit-generated-001";
  private static final String GENERATED_CORRELATION_ID = "correlation-generated-001";

  @Mock private AuditRepository auditRepository;

  @Mock private IdGenerator idGenerator;

  @Mock private Clock clock;

  private WriteAuditEntryUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new TestableWriteAuditEntryUseCase(auditRepository, idGenerator, clock);
  }

  @AfterEach
  void tearDown() {
    MDC.clear();
  }

  @Test
  @DisplayName("should write audit entry when repository succeeds on first attempt")
  void should_writeAuditEntry_when_repositorySucceedsOnFirstAttempt() {
    when(idGenerator.generate()).thenReturn(GENERATED_ID, GENERATED_CORRELATION_ID);
    when(clock.now()).thenReturn(FIXED_NOW);

    WriteAuditEntryCommand command =
        new WriteAuditEntryCommand("LOGIN", "user-123", "tenant-alpha", "USER", "user-123", "{}");

    useCase.execute(command);

    ArgumentCaptor<AuditEntry> captor = ArgumentCaptor.forClass(AuditEntry.class);
    verify(auditRepository, times(1)).append(captor.capture());

    AuditEntry captured = captor.getValue();
    assertThat(captured.getId()).isEqualTo(GENERATED_ID);
    assertThat(captured.getActionType()).isEqualTo("LOGIN");
    assertThat(captured.getActorId()).isEqualTo("user-123");
    assertThat(captured.getTenantId()).isEqualTo("tenant-alpha");
    assertThat(captured.getEntityType()).isEqualTo("USER");
    assertThat(captured.getEntityId()).isEqualTo("user-123");
    assertThat(captured.getDetails()).isEqualTo("{}");
    assertThat(captured.getTimestamp()).isEqualTo(FIXED_NOW);
  }

  @Test
  @DisplayName("should use correlationId from MDC when present")
  void should_useCorrelationIdFromMdc_when_mdcHasCorrelationId() {
    String mdcCorrelationId = "mdc-correlation-abc-123";
    MDC.put("correlationId", mdcCorrelationId);

    when(idGenerator.generate()).thenReturn(GENERATED_ID);
    when(clock.now()).thenReturn(FIXED_NOW);

    WriteAuditEntryCommand command =
        new WriteAuditEntryCommand(
            "CREATE_CUSTOMER", "user-456", "tenant-beta", "CUSTOMER", "cust-789", "{}");

    useCase.execute(command);

    ArgumentCaptor<AuditEntry> captor = ArgumentCaptor.forClass(AuditEntry.class);
    verify(auditRepository).append(captor.capture());

    assertThat(captor.getValue().getCorrelationId()).isEqualTo(mdcCorrelationId);
  }

  @Test
  @DisplayName("should generate correlationId when MDC value is absent")
  void should_generateCorrelationId_when_mdcValueIsAbsent() {
    // MDC is empty by default after tearDown
    when(idGenerator.generate()).thenReturn(GENERATED_ID, GENERATED_CORRELATION_ID);
    when(clock.now()).thenReturn(FIXED_NOW);

    WriteAuditEntryCommand command =
        new WriteAuditEntryCommand("LOGIN", "user-123", "tenant-alpha", "USER", "user-123", "{}");

    useCase.execute(command);

    ArgumentCaptor<AuditEntry> captor = ArgumentCaptor.forClass(AuditEntry.class);
    verify(auditRepository).append(captor.capture());

    assertThat(captor.getValue().getCorrelationId()).isEqualTo(GENERATED_CORRELATION_ID);
  }

  @Test
  @DisplayName("should generate correlationId when MDC value is blank")
  void should_generateCorrelationId_when_mdcValueIsBlank() {
    MDC.put("correlationId", "   ");

    when(idGenerator.generate()).thenReturn(GENERATED_ID, GENERATED_CORRELATION_ID);
    when(clock.now()).thenReturn(FIXED_NOW);

    WriteAuditEntryCommand command =
        new WriteAuditEntryCommand("LOGIN", "user-123", "tenant-alpha", "USER", "user-123", "{}");

    useCase.execute(command);

    ArgumentCaptor<AuditEntry> captor = ArgumentCaptor.forClass(AuditEntry.class);
    verify(auditRepository).append(captor.capture());

    assertThat(captor.getValue().getCorrelationId()).isEqualTo(GENERATED_CORRELATION_ID);
  }

  @Test
  @DisplayName("should retry and succeed when first attempt fails but second succeeds")
  void should_retryAndSucceed_when_firstAttemptFailsButSecondSucceeds() {
    when(idGenerator.generate()).thenReturn(GENERATED_ID, GENERATED_CORRELATION_ID);
    when(clock.now()).thenReturn(FIXED_NOW);

    doThrow(new RuntimeException("Connection timeout"))
        .doAnswer(invocation -> null)
        .when(auditRepository)
        .append(any(AuditEntry.class));

    WriteAuditEntryCommand command =
        new WriteAuditEntryCommand("LOGIN", "user-123", "tenant-alpha", "USER", "user-123", "{}");

    useCase.execute(command);

    verify(auditRepository, times(2)).append(any(AuditEntry.class));
  }

  @Test
  @DisplayName("should log error and not throw when both attempts fail")
  void should_logErrorAndNotThrow_when_bothAttemptsFail() {
    when(idGenerator.generate()).thenReturn(GENERATED_ID, GENERATED_CORRELATION_ID);
    when(clock.now()).thenReturn(FIXED_NOW);

    doThrow(new RuntimeException("Database unavailable"))
        .when(auditRepository)
        .append(any(AuditEntry.class));

    WriteAuditEntryCommand command =
        new WriteAuditEntryCommand("LOGIN", "user-123", "tenant-alpha", "USER", "user-123", "{}");

    // Should NOT throw — audit failures don't block business operations
    useCase.execute(command);

    verify(auditRepository, times(2)).append(any(AuditEntry.class));
  }

  @Test
  @DisplayName("should not retry when first attempt succeeds")
  void should_notRetry_when_firstAttemptSucceeds() {
    when(idGenerator.generate()).thenReturn(GENERATED_ID, GENERATED_CORRELATION_ID);
    when(clock.now()).thenReturn(FIXED_NOW);

    WriteAuditEntryCommand command =
        new WriteAuditEntryCommand(
            "UPLOAD_DOCUMENT",
            "user-789",
            "tenant-gamma",
            "DOCUMENT",
            "doc-001",
            "{\"filename\":\"report.pdf\"}");

    useCase.execute(command);

    verify(auditRepository, times(1)).append(any(AuditEntry.class));
  }

  @Test
  @DisplayName("should use idGenerator for entry id")
  void should_useIdGenerator_when_creatingEntry() {
    String customId = "custom-audit-id-999";
    when(idGenerator.generate()).thenReturn(customId, GENERATED_CORRELATION_ID);
    when(clock.now()).thenReturn(FIXED_NOW);

    WriteAuditEntryCommand command =
        new WriteAuditEntryCommand("LOGIN", "user-123", "tenant-alpha", "USER", "user-123", "{}");

    useCase.execute(command);

    ArgumentCaptor<AuditEntry> captor = ArgumentCaptor.forClass(AuditEntry.class);
    verify(auditRepository).append(captor.capture());

    assertThat(captor.getValue().getId()).isEqualTo(customId);
  }

  @Test
  @DisplayName("should use clock for timestamp")
  void should_useClockForTimestamp_when_creatingEntry() {
    Instant customTime = Instant.parse("2025-06-20T14:00:00Z");
    when(idGenerator.generate()).thenReturn(GENERATED_ID, GENERATED_CORRELATION_ID);
    when(clock.now()).thenReturn(customTime);

    WriteAuditEntryCommand command =
        new WriteAuditEntryCommand("LOGIN", "user-123", "tenant-alpha", "USER", "user-123", "{}");

    useCase.execute(command);

    ArgumentCaptor<AuditEntry> captor = ArgumentCaptor.forClass(AuditEntry.class);
    verify(auditRepository).append(captor.capture());

    assertThat(captor.getValue().getTimestamp()).isEqualTo(customTime);
  }

  /** Testable subclass that skips the actual Thread.sleep() to speed up tests. */
  private static class TestableWriteAuditEntryUseCase extends WriteAuditEntryUseCase {

    TestableWriteAuditEntryUseCase(
        AuditRepository auditRepository, IdGenerator idGenerator, Clock clock) {
      super(auditRepository, idGenerator, clock);
    }

    @Override
    void sleep(long millis) {
      // No-op for fast tests
    }
  }
}
