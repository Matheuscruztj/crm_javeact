package com.atlasops.shared.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base class for aggregate roots. Extends {@link Entity} with domain event registration.
 *
 * @param <ID> the type of the aggregate root identifier
 */
public abstract class AggregateRoot<ID> extends Entity<ID> {

  private final List<DomainEvent> domainEvents = new ArrayList<>();

  protected AggregateRoot(ID id) {
    super(id);
  }

  protected void registerEvent(DomainEvent event) {
    if (event == null) {
      throw new IllegalArgumentException("Domain event must not be null");
    }
    domainEvents.add(event);
  }

  public List<DomainEvent> getDomainEvents() {
    return Collections.unmodifiableList(domainEvents);
  }

  public void clearDomainEvents() {
    domainEvents.clear();
  }
}
