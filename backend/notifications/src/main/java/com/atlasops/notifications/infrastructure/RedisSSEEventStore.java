package com.atlasops.notifications.infrastructure;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Redis-backed SSE event store for Last-Event-ID replay support.
 *
 * <p>Events are stored in a Redis Sorted Set with the event sequence as score,
 * allowing efficient range queries for replay since a given event ID.
 * TTL: 24 hours per event.
 *
 * <p>Validates: P0.H.2 — SSE Event Replay (Last-Event-ID)
 */
@Component
public class RedisSSEEventStore {

  private static final Logger log = LoggerFactory.getLogger(RedisSSEEventStore.class);

  /**
   * Key pattern: sse:events:{userId}:{tenantId} → Sorted Set (score=epochMilli, value=json)
   * Separate key: sse:eventid:{eventId} → maps eventId to epochMilli score for range queries
   */
  private static final String EVENTS_KEY_PREFIX = "sse:events:";
  private static final String EVENT_META_PREFIX = "sse:event:";
  private static final long TTL_SECONDS = 24 * 60 * 60L; // 24 hours

  private final StringRedisTemplate redisTemplate;

  public RedisSSEEventStore(StringRedisTemplate redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  /**
   * Persists an SSE event for later replay.
   *
   * @param userId    the user this event belongs to
   * @param tenantId  the tenant context
   * @param eventType the event type
   * @param payload   the JSON payload
   * @return the generated eventId
   */
  public String storeEvent(String userId, String tenantId, String eventType, String payload) {
    String eventId = UUID.randomUUID().toString();
    long score = System.currentTimeMillis();
    String key = buildKey(userId, tenantId);

    String value = eventId + "|" + eventType + "|" + payload;

    try {
      redisTemplate.opsForZSet().add(key, value, score);
      redisTemplate.expire(key, Duration.ofSeconds(TTL_SECONDS));
      // Also store eventId → score mapping for range lookup
      redisTemplate.opsForValue().set(
          EVENT_META_PREFIX + eventId,
          String.valueOf(score),
          Duration.ofSeconds(TTL_SECONDS));

      log.debug("SSE event stored: id={}, user={}, type={}", eventId, userId, eventType);
      return eventId;
    } catch (Exception e) {
      log.warn("Failed to store SSE event for user {}: {}", userId, e.getMessage());
      return eventId;
    }
  }

  /**
   * Retrieves events for a user since the given lastEventId (exclusive).
   *
   * @param userId      the user
   * @param tenantId    the tenant
   * @param lastEventId the last event ID received by the client
   * @return list of [eventId, eventType, payload] maps
   */
  public List<Map<String, String>> getEventsSince(
      String userId, String tenantId, String lastEventId) {

    try {
      String scoreStr = redisTemplate.opsForValue().get(EVENT_META_PREFIX + lastEventId);
      if (scoreStr == null) {
        log.debug("Last event ID {} not found in store, no replay", lastEventId);
        return Collections.emptyList();
      }

      double minScore = Double.parseDouble(scoreStr) + 1;
      String key = buildKey(userId, tenantId);

      var rawEvents = redisTemplate.opsForZSet()
          .rangeByScore(key, minScore, Double.MAX_VALUE);

      if (rawEvents == null || rawEvents.isEmpty()) {
        return Collections.emptyList();
      }

      return rawEvents.stream()
          .map(raw -> {
            String[] parts = raw.split("\\|", 3);
            if (parts.length < 3) return null;
            return Map.of("eventId", parts[0], "eventType", parts[1], "payload", parts[2]);
          })
          .filter(m -> m != null)
          .toList();
    } catch (Exception e) {
      log.warn("Failed to replay SSE events for user {}: {}", userId, e.getMessage());
      return Collections.emptyList();
    }
  }

  private String buildKey(String userId, String tenantId) {
    return EVENTS_KEY_PREFIX + userId + ":" + tenantId;
  }
}
