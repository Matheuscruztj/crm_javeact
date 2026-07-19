package com.atlasops.requests.application;

import com.atlasops.requests.domain.Comment;
import com.atlasops.requests.domain.ports.CommentRepository;
import com.atlasops.requests.domain.ports.ServiceRequestRepository;
import com.atlasops.shared.domain.exceptions.ResourceNotFoundException;
import com.atlasops.shared.domain.ports.Clock;
import com.atlasops.shared.domain.ports.IdGenerator;
import java.util.Objects;

/**
 * Use case for adding a comment to a service request. Validates comment text length (1-2000
 * characters) and verifies the request exists within the tenant.
 */
public class AddCommentUseCase {

  private final CommentRepository commentRepository;
  private final ServiceRequestRepository serviceRequestRepository;
  private final IdGenerator idGenerator;
  private final Clock clock;

  public AddCommentUseCase(
      CommentRepository commentRepository,
      ServiceRequestRepository serviceRequestRepository,
      IdGenerator idGenerator,
      Clock clock) {
    this.commentRepository = commentRepository;
    this.serviceRequestRepository = serviceRequestRepository;
    this.idGenerator = idGenerator;
    this.clock = clock;
  }

  /**
   * Adds a comment to a service request.
   *
   * @param command the add comment command
   * @return the created comment
   * @throws ResourceNotFoundException if the request is not found
   * @throws IllegalArgumentException if the comment text is invalid
   */
  public Comment execute(AddCommentCommand command) {
    Objects.requireNonNull(command, "Command must not be null");

    serviceRequestRepository
        .findByIdAndTenantId(command.requestId(), command.tenantId())
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    "Request with id '" + command.requestId() + "' not found"));

    Comment comment =
        Comment.create(
            idGenerator.generate(),
            command.text(),
            command.authorId(),
            command.requestId(),
            clock.now());

    return commentRepository.save(comment);
  }

  /**
   * Command for adding a comment.
   *
   * @param requestId the request identifier
   * @param text the comment text (1-2000 characters)
   * @param authorId the author's user identifier
   * @param tenantId the tenant identifier
   */
  public record AddCommentCommand(String requestId, String text, String authorId, String tenantId) {

    public AddCommentCommand {
      Objects.requireNonNull(requestId, "Request id must not be null");
      Objects.requireNonNull(text, "Text must not be null");
      Objects.requireNonNull(authorId, "Author id must not be null");
      Objects.requireNonNull(tenantId, "Tenant id must not be null");
    }
  }
}
