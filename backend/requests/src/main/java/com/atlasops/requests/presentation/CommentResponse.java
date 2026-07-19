package com.atlasops.requests.presentation;

import com.atlasops.requests.domain.Comment;
import java.time.Instant;

/**
 * Response DTO representing a comment.
 *
 * @param id the comment identifier
 * @param text the comment text
 * @param authorId the author's user identifier
 * @param requestId the associated request identifier
 * @param createdAt the creation timestamp
 */
public record CommentResponse(
    String id, String text, String authorId, String requestId, Instant createdAt) {

  /**
   * Creates a CommentResponse from a Comment domain entity.
   *
   * @param comment the domain comment
   * @return the response DTO
   */
  public static CommentResponse from(Comment comment) {
    return new CommentResponse(
        comment.getId(),
        comment.getText(),
        comment.getAuthorId(),
        comment.getRequestId(),
        comment.getCreatedAt());
  }
}
