package com.atlasops.boot.infrastructure.outbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlasops.shared.domain.OutboxEvent;
import com.atlasops.shared.domain.ports.OutboxEventRepository;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

class OutboxDispatcherTest {

  private OutboxEventRepository outboxEventRepository;
  private StringRedisTemplate redisTemplate;
  @SuppressWarnings("unchecked")
  private StreamOperations<String, Object, Object> streamOperations;
  private OutboxDispatcher dispatcher;

  @BeforeEach
  void setUp() {
    outboxEventRepository = mock(OutboxEventRepository.class);
    redisTemplate = mock(StringRedisTemplate.class);
    streamOperations = mock(StreamOperations.class);
    when(redisTemplate.opsForStream()).thenReturn(streamOperations);
    dispatcher = new OutboxDispatcher(outboxEventRepository, redisTemplate);
  }

  @Test
  void should_dispatchPendingEvent_when_publishSucceeds() {
    OutboxEvent event =
        new OutboxEvent(
            "outbox-1",
            "DocumentUploadedEvent",
            "event-1",
            "tenant-1",
            "corr-1",
            "{\"foo\":\"bar\"}",
            "document-events",
            Instant.parse("2026-07-27T10:00:00Z"));
    when(outboxEventRepository.findPendingEvents(50)).thenReturn(List.of(event));

    dispatcher.dispatchPendingEvents();

    verify(streamOperations).add(any(MapRecord.class));
    verify(outboxEventRepository).markPublished("event-1");
  }

  @Test
  void should_markEventFailed_when_publishThrows() {
    OutboxEvent event =
        new OutboxEvent(
            "outbox-2",
            "DocumentAnalyzedEvent",
            "event-2",
            "tenant-1",
            null,
            "{\"baz\":\"qux\"}",
            "analysis-events",
            Instant.parse("2026-07-27T10:05:00Z"));
    when(outboxEventRepository.findPendingEvents(50)).thenReturn(List.of(event));
    doThrow(new RuntimeException("boom")).when(streamOperations).add(any(MapRecord.class));

    dispatcher.dispatchPendingEvents();

    verify(outboxEventRepository).markFailed(eq("event-2"), eq("boom"));
  }

  @Test
  void should_skipWhenNoPendingEvents() {
    when(outboxEventRepository.findPendingEvents(50)).thenReturn(List.of());

    dispatcher.dispatchPendingEvents();

    assertThat(Mockito.mockingDetails(streamOperations).getInvocations()).isEmpty();
  }
}
