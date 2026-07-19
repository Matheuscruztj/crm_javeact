package com.atlasops.boot.infrastructure.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** JPA entity for the outbox_events table. */
@Entity
@Table(name = "outbox_events")
public class OutboxEventEntity {

  @Id private String id;

  @Column(name = "event_type", nullable = false)
  private String eventType;

  @Column(name = "event_id", nullable = false, unique = true)
  private String eventId;

  @Column(name = "tenant_id")
  private String tenantId;

  @Column(name = "correlation_id")
  private String correlationId;

  @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
  private String payload;

  @Column(name = "stream_name", nullable = false)
  private String streamName;

  @Column(name = "status", nullable = false)
  private String status;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "published_at")
  private Instant publishedAt;

  @Column(name = "retry_count", nullable = false)
  private int retryCount;

  @Column(name = "last_error")
  private String lastError;

  protected OutboxEventEntity() {}

  public OutboxEventEntity(
      String id,
      String eventType,
      String eventId,
      String tenantId,
      String correlationId,
      String payload,
      String streamName,
      String status,
      Instant createdAt,
      int retryCount) {
    this.id = id;
    this.eventType = eventType;
    this.eventId = eventId;
    this.tenantId = tenantId;
    this.correlationId = correlationId;
    this.payload = payload;
    this.streamName = streamName;
    this.status = status;
    this.createdAt = createdAt;
    this.retryCount = retryCount;
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

  public void setStatus(String status) {
    this.status = status;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getPublishedAt() {
    return publishedAt;
  }

  public void setPublishedAt(Instant publishedAt) {
    this.publishedAt = publishedAt;
  }

  public int getRetryCount() {
    return retryCount;
  }

  public void setRetryCount(int retryCount) {
    this.retryCount = retryCount;
  }

  public String getLastError() {
    return lastError;
  }

  public void setLastError(String lastError) {
    this.lastError = lastError;
  }
}
