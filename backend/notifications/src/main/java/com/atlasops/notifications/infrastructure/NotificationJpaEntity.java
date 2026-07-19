package com.atlasops.notifications.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** JPA entity mapping to the "notifications" database table. */
@Entity
@Table(name = "notifications")
public class NotificationJpaEntity {

  @Id
  @Column(name = "id", nullable = false, updatable = false)
  private String id;

  @Column(name = "tenant_id", nullable = false, updatable = false)
  private String tenantId;

  @Column(name = "recipient_user_id", nullable = false, updatable = false)
  private String recipientUserId;

  @Column(name = "title", nullable = false, length = 150)
  private String title;

  @Column(name = "message", nullable = false, length = 500)
  private String message;

  @Column(name = "read", nullable = false)
  private boolean read;

  @Column(name = "link")
  private String link;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected NotificationJpaEntity() {
    // JPA requires no-arg constructor
  }

  public NotificationJpaEntity(
      String id,
      String tenantId,
      String recipientUserId,
      String title,
      String message,
      boolean read,
      String link,
      Instant createdAt,
      Instant updatedAt) {
    this.id = id;
    this.tenantId = tenantId;
    this.recipientUserId = recipientUserId;
    this.title = title;
    this.message = message;
    this.read = read;
    this.link = link;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public String getId() {
    return id;
  }

  public String getTenantId() {
    return tenantId;
  }

  public String getRecipientUserId() {
    return recipientUserId;
  }

  public String getTitle() {
    return title;
  }

  public String getMessage() {
    return message;
  }

  public boolean isRead() {
    return read;
  }

  public String getLink() {
    return link;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setRead(boolean read) {
    this.read = read;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }
}
