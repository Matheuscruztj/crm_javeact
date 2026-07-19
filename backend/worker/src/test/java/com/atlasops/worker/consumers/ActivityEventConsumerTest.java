package com.atlasops.worker.consumers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlasops.activities.application.RecordActivityCommand;
import com.atlasops.activities.application.RecordActivityUseCase;
import com.atlasops.activities.domain.Activity;
import com.atlasops.worker.infrastructure.redis.StreamMessage;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for ActivityEventConsumer. Validates: Requirements 14.1, 14.7, 14.8 */
@ExtendWith(MockitoExtension.class)
class ActivityEventConsumerTest {

  @Mock private RecordActivityUseCase recordActivityUseCase;

  private ActivityEventConsumer consumer;

  @BeforeEach
  void setUp() {
    consumer = new ActivityEventConsumer(recordActivityUseCase);
  }

  @Test
  void should_returnCorrectStreamKey() {
    assertThat(consumer.getStreamKey()).isEqualTo("activities.events");
  }

  @Test
  void should_recordActivity_when_eventIsNew() throws Exception {
    // Arrange
    Map<String, String> payload = new HashMap<>();
    payload.put("eventId", "evt-001");
    payload.put("entityType", "CUSTOMER");
    payload.put("entityId", "cust-123");
    payload.put("actionType", "CREATED");
    payload.put("actorId", "user-001");
    payload.put("tenantId", "tenant-alpha");
    payload.put("summary", "Customer John Doe was created");

    StreamMessage message = new StreamMessage("activities.events", "msg-1", payload);

    Activity mockActivity =
        Activity.create(
            "act-001",
            "CUSTOMER",
            "cust-123",
            "CREATED",
            "user-001",
            "tenant-alpha",
            "Customer John Doe was created",
            "evt-001",
            Instant.now());

    when(recordActivityUseCase.execute(any(RecordActivityCommand.class)))
        .thenReturn(Optional.of(mockActivity));

    // Act
    consumer.handle(message);

    // Assert
    ArgumentCaptor<RecordActivityCommand> captor =
        ArgumentCaptor.forClass(RecordActivityCommand.class);
    verify(recordActivityUseCase).execute(captor.capture());

    RecordActivityCommand capturedCommand = captor.getValue();
    assertThat(capturedCommand.eventId()).isEqualTo("evt-001");
    assertThat(capturedCommand.entityType()).isEqualTo("CUSTOMER");
    assertThat(capturedCommand.entityId()).isEqualTo("cust-123");
    assertThat(capturedCommand.actionType()).isEqualTo("CREATED");
    assertThat(capturedCommand.actorId()).isEqualTo("user-001");
    assertThat(capturedCommand.tenantId()).isEqualTo("tenant-alpha");
  }

  @Test
  void should_handleDuplicateEvent_when_eventAlreadyProcessed() throws Exception {
    // Arrange
    Map<String, String> payload = new HashMap<>();
    payload.put("eventId", "evt-001");
    payload.put("entityType", "CUSTOMER");
    payload.put("entityId", "cust-123");
    payload.put("actionType", "UPDATED");
    payload.put("actorId", "user-001");
    payload.put("tenantId", "tenant-alpha");
    payload.put("summary", "Customer updated");

    StreamMessage message = new StreamMessage("activities.events", "msg-1", payload);

    // Duplicate event returns empty Optional
    when(recordActivityUseCase.execute(any(RecordActivityCommand.class)))
        .thenReturn(Optional.empty());

    // Act
    consumer.handle(message);

    // Assert - should complete without error
    verify(recordActivityUseCase).execute(any(RecordActivityCommand.class));
  }

  @Test
  void should_useDefaultSummary_when_summaryIsNotProvided() throws Exception {
    // Arrange
    Map<String, String> payload = new HashMap<>();
    payload.put("eventId", "evt-002");
    payload.put("entityType", "REQUEST");
    payload.put("entityId", "req-456");
    payload.put("actionType", "SUBMITTED");
    payload.put("actorId", "user-002");
    payload.put("tenantId", "tenant-beta");
    // No summary field

    StreamMessage message = new StreamMessage("activities.events", "msg-2", payload);

    Activity mockActivity =
        Activity.create(
            "act-002",
            "REQUEST",
            "req-456",
            "SUBMITTED",
            "user-002",
            "tenant-beta",
            "REQUEST req-456 was submitted",
            "evt-002",
            Instant.now());

    when(recordActivityUseCase.execute(any(RecordActivityCommand.class)))
        .thenReturn(Optional.of(mockActivity));

    // Act
    consumer.handle(message);

    // Assert
    ArgumentCaptor<RecordActivityCommand> captor =
        ArgumentCaptor.forClass(RecordActivityCommand.class);
    verify(recordActivityUseCase).execute(captor.capture());

    // Default summary should be generated
    assertThat(captor.getValue().summary()).isNotBlank();
  }
}
