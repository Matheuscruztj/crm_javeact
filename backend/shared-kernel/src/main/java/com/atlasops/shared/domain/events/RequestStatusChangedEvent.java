package com.atlasops.shared.domain.events;

import com.atlasops.shared.domain.DomainEvent;
import java.util.Objects;

/**
 * Published when a service request transitions between statuses. Triggers activity recording and
 * notification for the CLIENT user.
 */
public final class RequestStatusChangedEvent extends DomainEvent {

  private final String requestId;
  private final String previousStatus;
  private final String newStatus;
  private final String actorId;

  public RequestStatusChangedEvent(
      String requestId,
      String previousStatus,
      String newStatus,
      String actorId,
      String tenantId,
      String correlationId) {
    super(tenantId, correlationId);
    this.requestId = Objects.requireNonNull(requestId, "requestId must not be null");
    this.previousStatus = Objects.requireNonNull(previousStatus, "previousStatus must not be null");
    this.newStatus = Objects.requireNonNull(newStatus, "newStatus must not be null");
    this.actorId = Objects.requireNonNull(actorId, "actorId must not be null");
  }

  public String getRequestId() {
    return requestId;
  }

  public String getPreviousStatus() {
    return previousStatus;
  }

  public String getNewStatus() {
    return newStatus;
  }

  public String getActorId() {
    return actorId;
  }
}
