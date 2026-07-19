package com.atlasops.requests.domain;

import com.atlasops.shared.domain.Entity;
import java.time.Instant;
import java.util.Objects;

/**
 * Entity representing a comment on a service request. Comments have text content validated to be
 * between 1 and 2000 characters.
 */
public final class Comment extends Entity<String> {

  private static final int MIN_TEXT_LENGTH = 1;
  private static final int MAX_TEXT_LENGTH = 2000;

  private final String text;
  private final String authorId;
  private final String requestId;
  private final Instant createdAt;

  private Comment(String id, String text, String authorId, String requestId, Instant createdAt) {
    super(id);
    this.text = validateText(text);
    this.authorId = Objects.requireNonNull(authorId, "Author id must not be null");
    this.requestId = Objects.requireNonNull(requestId, "Request id must not be null");
    this.createdAt = Objects.requireNonNull(createdAt, "Created at must not be null");
  }

  /**
   * Factory method to create a new comment.
   *
   * @param id the comment identifier
   * @param text the comment text (1-2000 characters)
   * @param authorId the author's user identifier
   * @param requestId the associated request identifier
   * @param createdAt the creation timestamp
   * @return a new Comment instance
   */
  public static Comment create(
      String id, String text, String authorId, String requestId, Instant createdAt) {
    Objects.requireNonNull(id, "Comment id must not be null");
    return new Comment(id, text, authorId, requestId, createdAt);
  }

  /**
   * Reconstitutes a comment from persisted state.
   *
   * @param id the comment identifier
   * @param text the comment text
   * @param authorId the author's user identifier
   * @param requestId the associated request identifier
   * @param createdAt the creation timestamp
   * @return a reconstituted Comment instance
   */
  public static Comment reconstitute(
      String id, String text, String authorId, String requestId, Instant createdAt) {
    return new Comment(id, text, authorId, requestId, createdAt);
  }

  private static String validateText(String text) {
    if (text == null || text.isBlank()) {
      throw new IllegalArgumentException("Comment text must not be blank");
    }
    if (text.length() > MAX_TEXT_LENGTH) {
      throw new IllegalArgumentException(
          "Comment text must not exceed " + MAX_TEXT_LENGTH + " characters, got " + text.length());
    }
    return text;
  }

  public String getText() {
    return text;
  }

  public String getAuthorId() {
    return authorId;
  }

  public String getRequestId() {
    return requestId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
