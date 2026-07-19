package com.atlasops.shared.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Base class for all domain events. Each event captures when it occurred and carries a unique event
 * identifier, along with tenant and correlation context for multi-tenant tracing.
 */
public abstract class DomainEvent {

  private final Instant occurredAt;
  private final String eventId;
  private final String tenantId;
  private final String correlationId;

  protected DomainEvent() {
    this.occurredAt = Instant.now();
    this.eventId = UUID.randomUUID().toString();
    this.tenantId = null;
    this.correlationId = null;
  }

  protected DomainEvent(Instant occurredAt, String eventId) {
    this.occurredAt = occurredAt != null ? occurredAt : Instant.now();
    this.eventId = eventId != null ? eventId : UUID.randomUUID().toString();
    this.tenantId = null;
    this.correlationId = null;
  }

  protected DomainEvent(String tenantId, String correlationId) {
    this.occurredAt = Instant.now();
    this.eventId = UUID.randomUUID().toString();
    this.tenantId = tenantId;
    this.correlationId = correlationId;
  }

  protected DomainEvent(Instant occurredAt, String eventId, String tenantId, String correlationId) {
    this.occurredAt = occurredAt != null ? occurredAt : Instant.now();
    this.eventId = eventId != null ? eventId : UUID.randomUUID().toString();
    this.tenantId = tenantId;
    this.correlationId = correlationId;
  }

  public Instant getOccurredAt() {
    return occurredAt;
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

  @Override
  public String toString() {
    return getClass().getSimpleName()
        + "{eventId="
        + eventId
        + ", tenantId="
        + tenantId
        + ", correlationId="
        + correlationId
        + ", occurredAt="
        + occurredAt
        + "}";
  }
}
