package com.atlasops.shared.domain.events;

import com.atlasops.shared.domain.DomainEvent;
import java.util.Objects;

/** Published when a new customer is created. Triggers activity recording. */
public final class CustomerCreatedEvent extends DomainEvent {

  private final String customerId;
  private final String name;
  private final String actorId;

  public CustomerCreatedEvent(
      String customerId, String name, String tenantId, String actorId, String correlationId) {
    super(tenantId, correlationId);
    this.customerId = Objects.requireNonNull(customerId, "customerId must not be null");
    this.name = Objects.requireNonNull(name, "name must not be null");
    this.actorId = Objects.requireNonNull(actorId, "actorId must not be null");
  }

  public String getCustomerId() {
    return customerId;
  }

  public String getName() {
    return name;
  }

  public String getActorId() {
    return actorId;
  }
}
