package com.atlasops.requests.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** JPA entity mapping to the {@code request_status_history} table. */
@Entity
@Table(name = "request_status_history")
public class RequestStatusHistoryJpaEntity {

  @Id
  @Column(name = "id", nullable = false, updatable = false)
  private String id;

  @Column(name = "request_id", nullable = false)
  private String requestId;

  @Column(name = "tenant_id", nullable = false)
  private String tenantId;

  @Column(name = "from_status")
  private String fromStatus;

  @Column(name = "to_status", nullable = false)
  private String toStatus;

  @Column(name = "reason")
  private String reason;

  @Column(name = "actor_id")
  private String actorId;

  @Column(name = "occurred_at", nullable = false)
  private Instant occurredAt;

  protected RequestStatusHistoryJpaEntity() {
    // Required by JPA
  }

  public RequestStatusHistoryJpaEntity(
      String id,
      String requestId,
      String tenantId,
      String fromStatus,
      String toStatus,
      String reason,
      String actorId,
      Instant occurredAt) {
    this.id = id;
    this.requestId = requestId;
    this.tenantId = tenantId;
    this.fromStatus = fromStatus;
    this.toStatus = toStatus;
    this.reason = reason;
    this.actorId = actorId;
    this.occurredAt = occurredAt;
  }

  public String getId() {
    return id;
  }

  public String getRequestId() {
    return requestId;
  }

  public String getTenantId() {
    return tenantId;
  }

  public String getFromStatus() {
    return fromStatus;
  }

  public String getToStatus() {
    return toStatus;
  }

  public String getReason() {
    return reason;
  }

  public String getActorId() {
    return actorId;
  }

  public Instant getOccurredAt() {
    return occurredAt;
  }
}
