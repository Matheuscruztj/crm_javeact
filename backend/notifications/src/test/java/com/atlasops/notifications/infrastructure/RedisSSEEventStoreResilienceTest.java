package com.atlasops.notifications.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;

class RedisSSEEventStoreResilienceTest {

  @Test
  @DisplayName("should_returnEventId_when_redisWriteFails")
  void should_returnEventId_when_redisWriteFails() {
    StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
    ZSetOperations<String, String> zSetOperations = mock(ZSetOperations.class);
    ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
    when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    doThrow(new RuntimeException("redis unavailable"))
        .when(zSetOperations)
        .add(any(), any(), anyDouble());

    RedisSSEEventStore store = new RedisSSEEventStore(redisTemplate);

    String eventId = store.storeEvent("user-001", "tenant-alpha", "notification.created", "{}");

    assertThat(eventId).isNotBlank();
  }

  @Test
  @DisplayName("should_returnEmptyReplay_when_redisReplayFails")
  void should_returnEmptyReplay_when_redisReplayFails() {
    StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
    ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.get("sse:event:event-001")).thenThrow(new RuntimeException("redis unavailable"));

    RedisSSEEventStore store = new RedisSSEEventStore(redisTemplate);

    List<Map<String, String>> events = store.getEventsSince("user-001", "tenant-alpha", "event-001");

    assertThat(events).isEmpty();
  }
}
