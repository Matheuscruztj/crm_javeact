package com.atlasops.worker.infrastructure.redis;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Handles messages that failed processing repeatedly, routing them to a Dead Letter Queue (DLQ).
 *
 * <p>Messages that fail {@code maxDeliveryCount} times are moved to the DLQ stream
 * {@code atlasops-events-dlq} for manual inspection and replay.
 *
 * <p>Validates: P0.P.1 — DLQ handling (poison messages → atlasops-events-dlq)
 */
@Component
public class DeadLetterQueueHandler {

    private static final Logger log = LoggerFactory.getLogger(DeadLetterQueueHandler.class);
    static final String DLQ_STREAM = "atlasops-events-dlq";
    static final int MAX_DELIVERY_COUNT = 5;

    private final StringRedisTemplate redisTemplate;

    public DeadLetterQueueHandler(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Routes a poisoned message to the DLQ, enriching it with failure metadata.
     *
     * @param originalStream the source stream
     * @param messageId the original message ID
     * @param payload the message payload
     * @param cause the failure reason
     */
    public void sendToDlq(String originalStream, String messageId, Map<String, String> payload, String cause) {
        try {
            Map<String, String> dlqPayload = new java.util.HashMap<>(payload);
            dlqPayload.put("__dlq_original_stream", originalStream);
            dlqPayload.put("__dlq_original_id", messageId);
            dlqPayload.put("__dlq_cause", cause != null ? cause : "unknown");
            dlqPayload.put("__dlq_timestamp", java.time.Instant.now().toString());

            RecordId recordId = redisTemplate.opsForStream()
                    .add(MapRecord.create(DLQ_STREAM, dlqPayload));

            log.warn("Message {} from stream '{}' sent to DLQ with record id {}. Cause: {}",
                    messageId, originalStream, recordId, cause);

        } catch (Exception e) {
            log.error("Failed to send message {} to DLQ: {}", messageId, e.getMessage(), e);
        }
    }

    /**
     * Checks if a message has exceeded the maximum delivery count threshold.
     *
     * @param deliveryCount current delivery count
     * @return true if the message should be sent to the DLQ
     */
    public static boolean shouldSendToDlq(long deliveryCount) {
        return deliveryCount >= MAX_DELIVERY_COUNT;
    }
}
