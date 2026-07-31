package com.atlasops.notifications.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.List;

/** JPA entity mapping for per-user notification preferences. */
@Entity
@Table(name = "notification_preferences")
public class NotificationPreferencesJpaEntity {

  @EmbeddedId private NotificationPreferencesId id;

  @Column(name = "email_enabled", nullable = false)
  private boolean emailEnabled;

  @Column(name = "types_enabled", columnDefinition = "TEXT[]")
  private List<String> typesEnabled;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected NotificationPreferencesJpaEntity() {}

  public NotificationPreferencesJpaEntity(
      String userId,
      String tenantId,
      boolean emailEnabled,
      List<String> typesEnabled,
      Instant updatedAt) {
    this.id = new NotificationPreferencesId(userId, tenantId);
    this.emailEnabled = emailEnabled;
    this.typesEnabled = typesEnabled;
    this.updatedAt = updatedAt;
  }

  public String getUserId() {
    return id.userId();
  }

  public String getTenantId() {
    return id.tenantId();
  }

  public boolean isEmailEnabled() {
    return emailEnabled;
  }

  public List<String> getTypesEnabled() {
    return typesEnabled;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  @Embeddable
  public static class NotificationPreferencesId implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "user_id", nullable = false, updatable = false)
    private String userId;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private String tenantId;

    protected NotificationPreferencesId() {}

    public NotificationPreferencesId(String userId, String tenantId) {
      this.userId = userId;
      this.tenantId = tenantId;
    }

    public String userId() {
      return userId;
    }

    public String tenantId() {
      return tenantId;
    }
  }
}
