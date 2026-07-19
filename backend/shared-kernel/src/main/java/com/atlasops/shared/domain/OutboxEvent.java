package com.atlasops.shared.domain;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a domain event stored in the transactional outbox table.
 * Events are persisted in the same database transaction as the business operation,
 * guaranteeing that they are only published if the transaction commits.
 */
public class OutboxEvent {

  private final String id;
  private final String eventType;
  private final String eventId;
  private final String tenantId;
  private final String correlationId;
  private final String payload;
  private final String streamName;
  private String status;
  private final Instant createdAt;
  private Instant publishedAt;
  private int retryCount;
  private String lastError;

  public OutboxEvent(
      String id,
      String eventType,
      String eventId,
      String tenantId,
      String correlationId,
      String payload,
      String streamName,
      Instant createdAt) {
    this.id = Objects.requireNonNull(id, "id must not be null");
    this.eventType = Objects.requireNonNull(eventType, "eventType must not be null");
    this.eventId = Objects.requireNonNull(eventId, "eventId must not be null");
    this.tenantId = tenantId;
    this.correlationId = correlationId;
    this.payload = Objects.requireNonNull(payload, "payload must not be null");
    this.streamName = Objects.requireNonNull(streamName, "streamName must not be null");
    this.status = "PENDING";
    this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
    this.retryCount = 0;
  }

  public void markPublished(Instant publishedAt) {
    this.status = "PUBLISHED";
    this.publishedAt = publishedAt;
  }

  public void markFailed(String error) {
    this.retryCount++;
    this.lastError = error;
    if (this.retryCount >= 5) {
      this.status = "FAILED";
    }
  }

  public boolean isPending() {
    return "PENDING".equals(status);
  }

  public String getId() {
    return id;
  }

  public String getEventType() {
    return eventType;
  }

  public String getEventId() {
    return eventId;
  }

  public String getTenantId() {
    return tenantId;
  }

  public String getCorrelationId() {
    return correlationId;
  }

  public String getPayload() {
    return payload;
  }

  public String getStreamName() {
    return streamName;
  }

  public String getStatus() {
    return status;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getPublishedAt() {
    return publishedAt;
  }

  public int getRetryCount() {
    return retryCount;
  }

  public String getLastError() {
    return lastError;
  }
}
