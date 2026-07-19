package com.atlasops.requests.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** JPA entity mapping to the {@code request_comments} database table. */
@Entity
@Table(name = "request_comments")
public class CommentJpaEntity {

  @Id
  @Column(name = "id", nullable = false, updatable = false)
  private String id;

  @Column(name = "text", nullable = false, length = 2000)
  private String text;

  @Column(name = "author_id", nullable = false)
  private String authorId;

  @Column(name = "request_id", nullable = false)
  private String requestId;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  protected CommentJpaEntity() {
    // Required by JPA
  }

  public CommentJpaEntity(
      String id, String text, String authorId, String requestId, Instant createdAt) {
    this.id = id;
    this.text = text;
    this.authorId = authorId;
    this.requestId = requestId;
    this.createdAt = createdAt;
  }

  public String getId() {
    return id;
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
