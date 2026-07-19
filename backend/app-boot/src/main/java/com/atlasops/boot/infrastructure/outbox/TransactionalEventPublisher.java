package com.atlasops.boot.infrastructure.outbox;

import com.atlasops.shared.domain.DomainEvent;
import com.atlasops.shared.domain.OutboxEvent;
import com.atlasops.shared.domain.ports.EventPublisher;
import com.atlasops.shared.domain.ports.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Transactional event publisher that stores events in the outbox table
 * within the same database transaction as the business operation.
 *
 * <p>This replaces direct Redis publishing to guarantee at-least-once delivery.
 * Events stored in the outbox are later dispatched to Redis Streams by the
 * {@link OutboxDispatcher}.
 *
 * <p>Validates: Requirements 10.4, 11.1 (Transactional Consistency)
 */
@Component
@Primary
public class TransactionalEventPublisher implements EventPublisher {

  private static final Logger log = LoggerFactory.getLogger(TransactionalEventPublisher.class);
  private static final String MDC_CORRELATION_ID_KEY = "correlationId";

  private static final Map<String, String> EVENT_STREAM_MAPPING =
      Map.of(
          "DocumentUploadedEvent", "documents.uploaded",
          "DocumentReadyForAnalysisEvent", "documents.ready_for_analysis",
          "DocumentAnalyzedEvent", "documents.analyzed",
          "ApprovalDecisionEvent", "approvals.decided",
          "RequestStatusChangedEvent", "activities.events",
          "CustomerCreatedEvent", "activities.events");

  private static final String DEFAULT_STREAM = "events.unrouted";

  private final OutboxEventRepository outboxEventRepository;
  private final ObjectMapper objectMapper;

  public TransactionalEventPublisher(
      OutboxEventRepository outboxEventRepository, ObjectMapper objectMapper) {
    this.outboxEventRepository = outboxEventRepository;
    this.objectMapper = objectMapper;
  }

  @Override
  public void publish(DomainEvent event) {
    String eventType = event.getClass().getSimpleName();
    String streamName = EVENT_STREAM_MAPPING.getOrDefault(eventType, DEFAULT_STREAM);
    String correlationId = resolveCorrelationId(event);

    try {
      String payload = objectMapper.writeValueAsString(event);

      OutboxEvent outboxEvent =
          new OutboxEvent(
              UUID.randomUUID().toString(),
              eventType,
              event.getEventId(),
              event.getTenantId(),
              correlationId,
              payload,
              streamName,
              Instant.now());

      outboxEventRepository.save(outboxEvent);

      log.debug(
          "Event [{}] with eventId [{}] saved to outbox for stream [{}]",
          eventType,
          event.getEventId(),
          streamName);
    } catch (JsonProcessingException e) {
      log.error(
          "Failed to serialize event [{}] with eventId [{}]: {}",
          eventType,
          event.getEventId(),
          e.getMessage());
    }
  }

  private String resolveCorrelationId(DomainEvent event) {
    if (event.getCorrelationId() != null && !event.getCorrelationId().isBlank()) {
      return event.getCorrelationId();
    }
    return MDC.get(MDC_CORRELATION_ID_KEY);
  }
}
