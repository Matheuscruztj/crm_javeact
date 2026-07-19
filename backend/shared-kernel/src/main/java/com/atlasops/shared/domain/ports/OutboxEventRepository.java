package com.atlasops.shared.domain.ports;

import com.atlasops.shared.domain.OutboxEvent;
import java.util.List;

/**
 * Port for persisting and querying outbox events.
 * Implementations must participate in the same database transaction
 * as the business operation to guarantee transactional consistency.
 */
public interface OutboxEventRepository {

  void save(OutboxEvent event);

  List<OutboxEvent> findPendingEvents(int limit);

  void markPublished(String eventId);

  void markFailed(String eventId, String error);
}
