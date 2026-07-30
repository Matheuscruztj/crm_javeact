package com.atlasops.auth.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

class RedisRateLimiterTest {

  @Test
  @DisplayName("should_failClosed_when_redisIncrementReturnsNull")
  void should_failClosed_when_redisIncrementReturnsNull() {
    StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
    ValueOperations<String, String> values = mock(ValueOperations.class);
    when(redisTemplate.opsForValue()).thenReturn(values);
    when(values.increment("rate_limit:login:127.0.0.1")).thenReturn(null);

    RedisRateLimiter rateLimiter = new RedisRateLimiter(redisTemplate);

    assertThat(rateLimiter.isAllowed("login:127.0.0.1", 5, Duration.ofMinutes(1))).isFalse();
  }

  @Test
  @DisplayName("should_trackRemainingRequests_when_counterExists")
  void should_trackRemainingRequests_when_counterExists() {
    StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
    ValueOperations<String, String> values = mock(ValueOperations.class);
    when(redisTemplate.opsForValue()).thenReturn(values);
    when(values.get("rate_limit:login:127.0.0.1")).thenReturn("3");

    RedisRateLimiter rateLimiter = new RedisRateLimiter(redisTemplate);

    assertThat(rateLimiter.remaining("login:127.0.0.1", 5)).isEqualTo(2);
  }
}
