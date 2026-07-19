package com.atlasops.requests.domain;

import com.atlasops.shared.domain.Entity;
import java.time.Instant;
import java.util.Objects;

/**
 * Entity representing a single status transition in a service request lifecycle.
 *
 * <p>Every time a {@link ServiceRequest} transitions to a new status, an immutable
 * history record is appended. Together, these records form the full audit trail.
 */
public final class RequestStatusHistory extends Entity<String> {

  private final String requestId;
  private final RequestStatus fromStatus;
  private final RequestStatus toStatus;
  private final String reason;
  private final String actorId;
  private final Instant occurredAt;

  private RequestStatusHistory(
      String id,
      String requestId,
      RequestStatus fromStatus,
      RequestStatus toStatus,
      String reason,
      String actorId,
      Instant occurredAt) {
    super(id);
    this.requestId = Objects.requireNonNull(requestId, "RequestId must not be null");
    this.fromStatus = fromStatus; // null allowed for initial creation
    this.toStatus = Objects.requireNonNull(toStatus, "ToStatus must not be null");
    this.reason = reason;
    this.actorId = actorId;
    this.occurredAt = Objects.requireNonNull(occurredAt, "OccurredAt must not be null");
  }

  /**
   * Creates a new status history record for a transition.
   *
   * @param id unique identifier
   * @param requestId the request this history belongs to
   * @param fromStatus the previous status (null if this is the initial record)
   * @param toStatus the new status
   * @param reason optional reason for the transition
   * @param actorId the user or system that caused the transition
   * @param occurredAt when the transition occurred
   */
  public static RequestStatusHistory create(
      String id,
      String requestId,
      RequestStatus fromStatus,
      RequestStatus toStatus,
      String reason,
      String actorId,
      Instant occurredAt) {
    return new RequestStatusHistory(id, requestId, fromStatus, toStatus, reason, actorId, occurredAt);
  }

  /** Reconstitutes from persisted state. */
  public static RequestStatusHistory reconstitute(
      String id,
      String requestId,
      RequestStatus fromStatus,
      RequestStatus toStatus,
      String reason,
      String actorId,
      Instant occurredAt) {
    return new RequestStatusHistory(id, requestId, fromStatus, toStatus, reason, actorId, occurredAt);
  }

  public String getRequestId() { return requestId; }
  public RequestStatus getFromStatus() { return fromStatus; }
  public RequestStatus getToStatus() { return toStatus; }
  public String getReason() { return reason; }
  public String getActorId() { return actorId; }
  public Instant getOccurredAt() { return occurredAt; }
}
