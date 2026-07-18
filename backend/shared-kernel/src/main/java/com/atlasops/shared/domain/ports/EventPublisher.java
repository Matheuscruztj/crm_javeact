package com.atlasops.shared.domain.ports;

import com.atlasops.shared.domain.DomainEvent;

/** Port for publishing domain events to downstream consumers. */
public interface EventPublisher {

  void publish(DomainEvent event);
}
