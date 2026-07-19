package com.atlasops.worker.infrastructure.redis;

import java.util.Map;
import java.util.Objects;

/**
 * Represents a message read from a Redis Stream.
 *
 * @param streamKey the Redis stream key
 * @param messageId the unique message ID assigned by Redis
 * @param payload the message payload as key-value pairs
 */
public record StreamMessage(String streamKey, String messageId, Map<String, String> payload) {

  public StreamMessage {
    Objects.requireNonNull(streamKey, "StreamKey must not be null");
    Objects.requireNonNull(messageId, "MessageId must not be null");
    Objects.requireNonNull(payload, "Payload must not be null");
  }

  /**
   * Gets a value from the payload.
   *
   * @param key the payload key
   * @return the value or null if not present
   */
  public String get(String key) {
    return payload.get(key);
  }

  /**
   * Gets a required value from the payload.
   *
   * @param key the payload key
   * @return the value
   * @throws IllegalArgumentException if the key is not present
   */
  public String getRequired(String key) {
    String value = payload.get(key);
    if (value == null) {
      throw new IllegalArgumentException("Required field '" + key + "' not found in message");
    }
    return value;
  }
}
