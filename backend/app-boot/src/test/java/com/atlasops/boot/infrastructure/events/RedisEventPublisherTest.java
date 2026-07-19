package com.atlasops.boot.infrastructure.events;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlasops.shared.domain.DomainEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

@ExtendWith(MockitoExtension.class)
class RedisEventPublisherTest {

  @Mock private StringRedisTemplate redisTemplate;

  @Mock private ObjectMapper objectMapper;

  @Mock private StreamOperations<String, String, String> streamOperations;

  private RedisEventPublisher publisher;

  @BeforeEach
  void setUp() {
    publisher = new RedisEventPublisher(redisTemplate, objectMapper);
  }

  @AfterEach
  void tearDown() {
    MDC.clear();
  }

  @Test
  @SuppressWarnings("unchecked")
  void should_publishToCorrectStream_when_knownEventType() throws Exception {
    // Arrange
    var event = new CustomerCreatedEvent("tenant-001", "corr-001");
    when(objectMapper.writeValueAsString(event)).thenReturn("{\"test\":\"payload\"}");
    doReturn(streamOperations).when(redisTemplate).opsForStream();
    when(streamOperations.add(any(MapRecord.class))).thenReturn(RecordId.autoGenerate());

    // Act
    publisher.publish(event);

    // Assert
    ArgumentCaptor<MapRecord<String, String, String>> captor =
        ArgumentCaptor.forClass(MapRecord.class);
    verify(streamOperations).add(captor.capture());

    var record = captor.getValue();
    assertThat(record.getStream()).isEqualTo("activities.events");
  }

  @Test
  @SuppressWarnings("unchecked")
  void should_publishToUnroutedStream_when_unknownEventType() throws Exception {
    // Arrange
    var event = new UnknownTestEvent("tenant-002", "corr-002");
    when(objectMapper.writeValueAsString(event)).thenReturn("{\"test\":\"payload\"}");
    doReturn(streamOperations).when(redisTemplate).opsForStream();
    when(streamOperations.add(any(MapRecord.class))).thenReturn(RecordId.autoGenerate());

    // Act
    publisher.publish(event);

    // Assert
    ArgumentCaptor<MapRecord<String, String, String>> captor =
        ArgumentCaptor.forClass(MapRecord.class);
    verify(streamOperations).add(captor.capture());

    var record = captor.getValue();
    assertThat(record.getStream()).isEqualTo("events.unrouted");
  }

  @Test
  @SuppressWarnings("unchecked")
  void should_enrichCorrelationIdFromMDC_when_eventHasNoCorrelationId() throws Exception {
    // Arrange
    MDC.put("correlationId", "mdc-corr-123");
    var event = new CustomerCreatedEvent("tenant-003", null);
    when(objectMapper.writeValueAsString(event)).thenReturn("{\"test\":\"payload\"}");
    doReturn(streamOperations).when(redisTemplate).opsForStream();
    when(streamOperations.add(any(MapRecord.class))).thenReturn(RecordId.autoGenerate());

    // Act
    publisher.publish(event);

    // Assert
    ArgumentCaptor<MapRecord<String, String, String>> captor =
        ArgumentCaptor.forClass(MapRecord.class);
    verify(streamOperations).add(captor.capture());

    Map<String, String> value = captor.getValue().getValue();
    assertThat(value.get("correlationId")).isEqualTo("mdc-corr-123");
  }

  @Test
  @SuppressWarnings("unchecked")
  void should_useEventCorrelationId_when_eventAlreadyHasCorrelationId() throws Exception {
    // Arrange
    MDC.put("correlationId", "mdc-corr-should-be-ignored");
    var event = new CustomerCreatedEvent("tenant-004", "event-corr-456");
    when(objectMapper.writeValueAsString(event)).thenReturn("{\"test\":\"payload\"}");
    doReturn(streamOperations).when(redisTemplate).opsForStream();
    when(streamOperations.add(any(MapRecord.class))).thenReturn(RecordId.autoGenerate());

    // Act
    publisher.publish(event);

    // Assert
    ArgumentCaptor<MapRecord<String, String, String>> captor =
        ArgumentCaptor.forClass(MapRecord.class);
    verify(streamOperations).add(captor.capture());

    Map<String, String> value = captor.getValue().getValue();
    assertThat(value.get("correlationId")).isEqualTo("event-corr-456");
  }

  @Test
  void should_handleGracefully_when_serializationFails() throws Exception {
    // Arrange
    var event = new CustomerCreatedEvent("tenant-005", "corr-005");
    when(objectMapper.writeValueAsString(event))
        .thenThrow(new JsonProcessingException("Serialization error") {});

    // Act — should not throw
    publisher.publish(event);

    // Assert — no interaction with Redis
    verify(redisTemplate, never()).opsForStream();
  }

  @Test
  @SuppressWarnings("unchecked")
  void should_handleGracefully_when_redisPublishFails() throws Exception {
    // Arrange
    var event = new CustomerCreatedEvent("tenant-006", "corr-006");
    when(objectMapper.writeValueAsString(event)).thenReturn("{\"test\":\"payload\"}");
    doReturn(streamOperations).when(redisTemplate).opsForStream();
    when(streamOperations.add(any(MapRecord.class)))
        .thenThrow(new RuntimeException("Redis unavailable"));

    // Act — should not throw
    publisher.publish(event);

    // Assert — exception is handled gracefully (logged, not propagated)
  }

  // --- Test event classes ---

  static class CustomerCreatedEvent extends DomainEvent {
    CustomerCreatedEvent(String tenantId, String correlationId) {
      super(Instant.now(), null, tenantId, correlationId);
    }
  }

  static class UnknownTestEvent extends DomainEvent {
    UnknownTestEvent(String tenantId, String correlationId) {
      super(Instant.now(), null, tenantId, correlationId);
    }
  }
}
