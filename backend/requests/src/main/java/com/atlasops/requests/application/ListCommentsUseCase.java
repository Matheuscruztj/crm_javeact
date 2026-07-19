package com.atlasops.requests.application;

import com.atlasops.requests.domain.Comment;
import com.atlasops.requests.domain.ports.CommentRepository;
import com.atlasops.requests.domain.ports.ServiceRequestRepository;
import com.atlasops.shared.domain.exceptions.ResourceNotFoundException;
import java.util.List;
import java.util.Objects;

/**
 * Use case for listing comments of a service request. Verifies the request exists within the tenant
 * before returning comments.
 */
public class ListCommentsUseCase {

  private final CommentRepository commentRepository;
  private final ServiceRequestRepository serviceRequestRepository;

  public ListCommentsUseCase(
      CommentRepository commentRepository, ServiceRequestRepository serviceRequestRepository) {
    this.commentRepository = commentRepository;
    this.serviceRequestRepository = serviceRequestRepository;
  }

  /**
   * Lists all comments for a request within the specified tenant.
   *
   * @param requestId the request identifier
   * @param tenantId the tenant identifier
   * @return the list of comments ordered by creation time ascending
   * @throws ResourceNotFoundException if the request is not found
   */
  public List<Comment> execute(String requestId, String tenantId) {
    Objects.requireNonNull(requestId, "Request id must not be null");
    Objects.requireNonNull(tenantId, "Tenant id must not be null");

    serviceRequestRepository
        .findByIdAndTenantId(requestId, tenantId)
        .orElseThrow(
            () -> new ResourceNotFoundException("Request with id '" + requestId + "' not found"));

    return commentRepository.findByRequestId(requestId);
  }
}
