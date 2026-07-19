package com.atlasops.requests.presentation;

import com.atlasops.requests.domain.RequestStatusHistory;
import java.time.Instant;

/**
 * Response DTO for a {@link RequestStatusHistory} entry.
 */
public record StatusHistoryResponse(
    String id,
    String requestId,
    String fromStatus,
    String toStatus,
    String reason,
    String actorId,
    Instant occurredAt) {

  public static StatusHistoryResponse from(RequestStatusHistory history) {
    return new StatusHistoryResponse(
        history.getId(),
        history.getRequestId(),
        history.getFromStatus() != null ? history.getFromStatus().name() : null,
        history.getToStatus().name(),
        history.getReason(),
        history.getActorId(),
        history.getOccurredAt());
  }
}
