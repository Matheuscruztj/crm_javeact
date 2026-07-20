package com.atlasops.shared.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Base class for all domain events. Each event captures when it occurred and carries a unique event
 * identifier, along with tenant and correlation context for multi-tenant tracing.
 *
 * <p>Subclasses should override {@link #getEventType()} to return a versioned type identifier
 * in kebab-case format: {@code entity.action.vN} (e.g., {@code customer.created.v1}).
 * Validates: P0.P.2 — domain events versionados.
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

  /**
   * Returns a versioned event type identifier in the format {@code entity.action.vN}.
   * Default implementation converts the simple class name to kebab-case with {@code .v1} suffix.
   * Subclasses should override to provide a stable, explicit type string.
   *
   * @return versioned type string, e.g. {@code "customer.created.v1"}
   */
  public String getEventType() {
    // Convert "CustomerCreatedEvent" → "customer.created.v1"
    String simpleName = getClass().getSimpleName().replace("Event", "");
    // Insert dots before uppercase letters, then lowercase
    String kebab = simpleName.replaceAll("([A-Z])", ".$1").toLowerCase().replaceFirst("^\\.", "");
    return kebab + ".v1";
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
