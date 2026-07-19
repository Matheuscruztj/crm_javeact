package com.atlasops.audit.application;

import com.atlasops.audit.domain.AuditEntry;
import com.atlasops.audit.domain.ports.AuditRepository;
import com.atlasops.shared.domain.ports.Clock;
import com.atlasops.shared.domain.ports.IdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * Use case for appending a new audit entry to the ledger. Includes retry logic: on first failure,
 * waits 1 second and retries once. If both attempts fail, logs an ERROR and proceeds without
 * throwing — audit failures must not block business operations.
 */
public class WriteAuditEntryUseCase {

  private static final Logger log = LoggerFactory.getLogger(WriteAuditEntryUseCase.class);
  private static final String MDC_CORRELATION_ID_KEY = "correlationId";
  private static final long RETRY_DELAY_MS = 1000L;

  private final AuditRepository auditRepository;
  private final IdGenerator idGenerator;
  private final Clock clock;

  public WriteAuditEntryUseCase(
      AuditRepository auditRepository, IdGenerator idGenerator, Clock clock) {
    this.auditRepository = auditRepository;
    this.idGenerator = idGenerator;
    this.clock = clock;
  }

  /**
   * Writes an audit entry to the ledger. Resolves the correlationId from MDC; generates a UUID v4
   * via IdGenerator if absent.
   *
   * @param command the write command with audit entry data
   */
  public void execute(WriteAuditEntryCommand command) {
    String id = idGenerator.generate();
    String correlationId = resolveCorrelationId();

    AuditEntry entry =
        AuditEntry.create(
            id,
            command.actionType(),
            command.actorId(),
            command.tenantId(),
            command.entityType(),
            command.entityId(),
            correlationId,
            command.details(),
            clock.now());

    try {
      auditRepository.append(entry);
    } catch (Exception firstAttemptException) {
      log.warn(
          "First audit write attempt failed for entry {}, retrying in {}ms",
          id,
          RETRY_DELAY_MS,
          firstAttemptException);
      sleep(RETRY_DELAY_MS);
      try {
        auditRepository.append(entry);
      } catch (Exception secondAttemptException) {
        log.error(
            "Audit write failed after retry for entry {}. "
                + "Action: {}, Actor: {}, Entity: {}/{}. Proceeding without audit.",
            id,
            command.actionType(),
            command.actorId(),
            command.entityType(),
            command.entityId(),
            secondAttemptException);
      }
    }
  }

  private String resolveCorrelationId() {
    String mdcValue = MDC.get(MDC_CORRELATION_ID_KEY);
    if (mdcValue != null && !mdcValue.isBlank()) {
      return mdcValue;
    }
    return idGenerator.generate();
  }

  void sleep(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
