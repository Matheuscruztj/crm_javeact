package com.atlasops.shared.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Base class for all domain events. Each event captures when it occurred and carries a unique event
 * identifier.
 */
public abstract class DomainEvent {

  private final Instant occurredAt;
  private final String eventId;

  protected DomainEvent() {
    this.occurredAt = Instant.now();
    this.eventId = UUID.randomUUID().toString();
  }

  protected DomainEvent(Instant occurredAt, String eventId) {
    this.occurredAt = occurredAt != null ? occurredAt : Instant.now();
    this.eventId = eventId != null ? eventId : UUID.randomUUID().toString();
  }

  public Instant getOccurredAt() {
    return occurredAt;
  }

  public String getEventId() {
    return eventId;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{eventId=" + eventId + ", occurredAt=" + occurredAt + "}";
  }
}
