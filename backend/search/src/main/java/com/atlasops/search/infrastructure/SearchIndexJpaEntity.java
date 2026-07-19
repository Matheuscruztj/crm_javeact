package com.atlasops.search.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * JPA entity mapping to the "search_index" database table. Stores pre-computed tsvector content for
 * full-text search across entities.
 */
@Entity
@Table(name = "search_index")
public class SearchIndexJpaEntity {

  @Id
  @Column(name = "id", nullable = false, updatable = false)
  private String id;

  @Column(name = "tenant_id", nullable = false)
  private String tenantId;

  @Column(name = "entity_type", nullable = false, length = 50)
  private String entityType;

  @Column(name = "entity_id", nullable = false)
  private String entityId;

  @Column(name = "title", nullable = false)
  private String title;

  @Column(name = "content_snippet", columnDefinition = "text")
  private String contentSnippet;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected SearchIndexJpaEntity() {
    // JPA requires no-arg constructor
  }

  public SearchIndexJpaEntity(
      String id,
      String tenantId,
      String entityType,
      String entityId,
      String title,
      String contentSnippet,
      Instant updatedAt) {
    this.id = id;
    this.tenantId = tenantId;
    this.entityType = entityType;
    this.entityId = entityId;
    this.title = title;
    this.contentSnippet = contentSnippet;
    this.updatedAt = updatedAt;
  }

  public String getId() {
    return id;
  }

  public String getTenantId() {
    return tenantId;
  }

  public String getEntityType() {
    return entityType;
  }

  public String getEntityId() {
    return entityId;
  }

  public String getTitle() {
    return title;
  }

  public String getContentSnippet() {
    return contentSnippet;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void setContentSnippet(String contentSnippet) {
    this.contentSnippet = contentSnippet;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }
}
