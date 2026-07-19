package com.atlasops.requests.domain;

import com.atlasops.shared.domain.AggregateRoot;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Aggregate root representing a service request. Manages status transitions, priority, analyst
 * assignment, comments, and document associations.
 */
public final class ServiceRequest extends AggregateRoot<String> {

  private static final int MIN_TITLE_LENGTH = 1;
  private static final int MAX_TITLE_LENGTH = 200;
  private static final int MIN_DESCRIPTION_LENGTH = 1;
  private static final int MAX_DESCRIPTION_LENGTH = 5000;

  private final String title;
  private final String description;
  private RequestStatus status;
  private RequestPriority priority;
  private final String customerId;
  private String assignedAnalystId;
  private final String tenantId;
  private final Instant createdAt;
  private Instant assignedAt;
  private final List<String> documentIds;

  private ServiceRequest(
      String id,
      String title,
      String description,
      RequestStatus status,
      RequestPriority priority,
      String customerId,
      String assignedAnalystId,
      String tenantId,
      Instant createdAt,
      Instant assignedAt,
      List<String> documentIds) {
    super(id);
    this.title = validateTitle(title);
    this.description = validateDescription(description);
    this.status = Objects.requireNonNull(status, "Status must not be null");
    this.priority = Objects.requireNonNull(priority, "Priority must not be null");
    this.customerId = Objects.requireNonNull(customerId, "Customer id must not be null");
    this.assignedAnalystId = assignedAnalystId;
    this.tenantId = Objects.requireNonNull(tenantId, "Tenant id must not be null");
    this.createdAt = Objects.requireNonNull(createdAt, "Created at must not be null");
    this.assignedAt = assignedAt;
    this.documentIds = new ArrayList<>(documentIds != null ? documentIds : List.of());
  }

  /** Creates a new service request with OPEN status and the specified priority. */
  public static ServiceRequest create(
      String id,
      String title,
      String description,
      RequestPriority priority,
      String customerId,
      String tenantId,
      Instant createdAt) {
    return new ServiceRequest(
        id,
        title,
        description,
        RequestStatus.OPEN,
        priority,
        customerId,
        null,
        tenantId,
        createdAt,
        null,
        null);
  }

  /** Reconstitutes a service request from persisted state. */
  public static ServiceRequest reconstitute(
      String id,
      String title,
      String description,
      RequestStatus status,
      RequestPriority priority,
      String customerId,
      String assignedAnalystId,
      String tenantId,
      Instant createdAt,
      Instant assignedAt,
      List<String> documentIds) {
    return new ServiceRequest(
        id,
        title,
        description,
        status,
        priority,
        customerId,
        assignedAnalystId,
        tenantId,
        createdAt,
        assignedAt,
        documentIds);
  }

  /**
   * Transitions the request to a new status, validating the state machine.
   *
   * @param targetStatus the target status
   * @throws IllegalStateException if the transition is not allowed
   */
  public void transitionTo(RequestStatus targetStatus) {
    this.status.validateTransitionTo(targetStatus);
    this.status = targetStatus;
  }

  /**
   * Assigns an analyst to this request and transitions from OPEN to IN_PROGRESS.
   *
   * @param analystId the analyst's user identifier
   * @param assignedAt the assignment timestamp
   * @throws IllegalStateException if the request is not in OPEN status
   */
  public void assignAnalyst(String analystId, Instant assignedAt) {
    Objects.requireNonNull(analystId, "Analyst id must not be null");
    Objects.requireNonNull(assignedAt, "Assigned at must not be null");
    if (this.status != RequestStatus.OPEN) {
      throw new IllegalStateException(
          "Can only assign analyst to a request in OPEN status, current status: " + this.status);
    }
    this.assignedAnalystId = analystId;
    this.assignedAt = assignedAt;
    this.status = RequestStatus.IN_PROGRESS;
  }

  /**
   * Associates a document with this request.
   *
   * @param documentId the document identifier
   */
  public void associateDocument(String documentId) {
    Objects.requireNonNull(documentId, "Document id must not be null");
    if (!this.documentIds.contains(documentId)) {
      this.documentIds.add(documentId);
    }
  }

  private static String validateTitle(String title) {
    if (title == null || title.isBlank()) {
      throw new IllegalArgumentException("Request title must not be blank");
    }
    if (title.length() > MAX_TITLE_LENGTH) {
      throw new IllegalArgumentException(
          "Request title must not exceed "
              + MAX_TITLE_LENGTH
              + " characters, got "
              + title.length());
    }
    return title;
  }

  private static String validateDescription(String description) {
    if (description == null || description.isBlank()) {
      throw new IllegalArgumentException("Request description must not be blank");
    }
    if (description.length() > MAX_DESCRIPTION_LENGTH) {
      throw new IllegalArgumentException(
          "Request description must not exceed "
              + MAX_DESCRIPTION_LENGTH
              + " characters, got "
              + description.length());
    }
    return description;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public RequestStatus getStatus() {
    return status;
  }

  public RequestPriority getPriority() {
    return priority;
  }

  public String getCustomerId() {
    return customerId;
  }

  public String getAssignedAnalystId() {
    return assignedAnalystId;
  }

  public String getTenantId() {
    return tenantId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getAssignedAt() {
    return assignedAt;
  }

  public List<String> getDocumentIds() {
    return Collections.unmodifiableList(documentIds);
  }
}
