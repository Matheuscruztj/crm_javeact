package com.atlasops.requests.domain.ports;

import com.atlasops.requests.domain.RequestStatusHistory;
import java.util.List;

/**
 * Port for persisting and querying {@link RequestStatusHistory} records.
 */
public interface RequestStatusHistoryRepository {

  /** Appends a new history record. */
  void save(RequestStatusHistory history);

  /**
   * Returns all history records for a request, ordered by {@code occurredAt} ascending.
   *
   * @param requestId the request identifier
   * @param tenantId  the tenant identifier (for isolation)
   * @return list of history records, oldest first
   */
  List<RequestStatusHistory> findByRequestId(String requestId, String tenantId);
}
