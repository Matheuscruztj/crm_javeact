package com.atlasops.boot.infrastructure.events;

import com.atlasops.shared.domain.DomainEvent;
import com.atlasops.shared.domain.ports.EventPublisher;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Infrastructure adapter that publishes domain events to Redis Streams.
 *
 * <p>Serializes events to JSON and routes them to the appropriate stream based on event type.
 * Enriches events with correlationId from MDC before publishing.
 *
 * <p>Validates: Requirements 10.4, 11.1, 12.5, 13.6, 27.4
 */
@Component
public class RedisEventPublisher implements EventPublisher {

  private static final Logger log = LoggerFactory.getLogger(RedisEventPublisher.class);

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

  private final StringRedisTemplate redisTemplate;
  private final ObjectMapper objectMapper;

  public RedisEventPublisher(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
    this.redisTemplate = redisTemplate;
    this.objectMapper = objectMapper;
  }

  @Override
  public void publish(DomainEvent event) {
    String correlationId = resolveCorrelationId(event);
    String eventType = event.getClass().getSimpleName();
    String streamName = EVENT_STREAM_MAPPING.getOrDefault(eventType, DEFAULT_STREAM);

    try {
      String payload = objectMapper.writeValueAsString(event);

      Map<String, String> message = new HashMap<>();
      message.put("eventId", event.getEventId());
      message.put("eventType", eventType);
      message.put("payload", payload);
      message.put("tenantId", event.getTenantId() != null ? event.getTenantId() : "");
      message.put("correlationId", correlationId != null ? correlationId : "");
      message.put("timestamp", event.getOccurredAt().toString());

      redisTemplate.opsForStream().add(StreamRecords.string(message).withStreamKey(streamName));

      log.info(
          "Published event [{}] to stream [{}] with eventId [{}]",
          eventType,
          streamName,
          event.getEventId());
    } catch (JsonProcessingException e) {
      log.error(
          "Failed to serialize event [{}] with eventId [{}]: {}",
          eventType,
          event.getEventId(),
          e.getMessage());
    } catch (Exception e) {
      log.error(
          "Failed to publish event [{}] with eventId [{}] to stream [{}]: {}",
          eventType,
          event.getEventId(),
          streamName,
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
