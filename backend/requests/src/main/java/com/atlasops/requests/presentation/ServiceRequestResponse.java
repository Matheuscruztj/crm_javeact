package com.atlasops.requests.presentation;

import com.atlasops.requests.domain.ServiceRequest;
import java.time.Instant;
import java.util.List;

/**
 * Response DTO representing a service request.
 *
 * @param id the request identifier
 * @param title the request title
 * @param description the request description
 * @param status the current status
 * @param priority the priority level
 * @param customerId the customer identifier
 * @param assignedAnalystId the assigned analyst's user identifier (nullable)
 * @param tenantId the tenant identifier
 * @param createdAt the creation timestamp
 * @param assignedAt the assignment timestamp (nullable)
 * @param documentIds the list of associated document identifiers
 */
public record ServiceRequestResponse(
    String id,
    String title,
    String description,
    String status,
    String priority,
    String customerId,
    String assignedAnalystId,
    String tenantId,
    Instant createdAt,
    Instant assignedAt,
    List<String> documentIds) {

  /**
   * Creates a ServiceRequestResponse from a ServiceRequest domain object.
   *
   * @param request the domain service request
   * @return the response DTO
   */
  public static ServiceRequestResponse from(ServiceRequest request) {
    return new ServiceRequestResponse(
        request.getId(),
        request.getTitle(),
        request.getDescription(),
        request.getStatus().name(),
        request.getPriority().name(),
        request.getCustomerId(),
        request.getAssignedAnalystId(),
        request.getTenantId(),
        request.getCreatedAt(),
        request.getAssignedAt(),
        request.getDocumentIds());
  }
}
