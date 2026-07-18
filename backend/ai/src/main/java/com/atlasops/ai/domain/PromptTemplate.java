package com.atlasops.ai.domain;

import com.atlasops.shared.domain.Entity;
import java.time.Instant;
import java.util.Objects;

/**
 * Domain entity representing a versioned prompt template. Templates are identified by name and
 * sequential numeric version, with an active flag to indicate which version is currently in use.
 *
 * <p>Validates: Requirements 4.9
 */
public class PromptTemplate extends Entity<String> {

  private final String name;
  private final int version;
  private final String content;
  private final boolean active;
  private final Instant createdAt;

  public PromptTemplate(
      String id, String name, int version, String content, boolean active, Instant createdAt) {
    super(id);

    Objects.requireNonNull(name, "name must not be null");
    Objects.requireNonNull(content, "content must not be null");
    Objects.requireNonNull(createdAt, "createdAt must not be null");

    if (name.isBlank()) {
      throw new IllegalArgumentException("name must not be blank");
    }
    if (content.isBlank()) {
      throw new IllegalArgumentException("content must not be blank");
    }
    if (version < 1) {
      throw new IllegalArgumentException("version must be >= 1, got: " + version);
    }

    this.name = name;
    this.version = version;
    this.content = content;
    this.active = active;
    this.createdAt = createdAt;
  }

  public String getName() {
    return name;
  }

  public int getVersion() {
    return version;
  }

  public String getContent() {
    return content;
  }

  public boolean isActive() {
    return active;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  /**
   * Returns the prompt version identifier in the format "name:vN". Example: "document-analysis:v3"
   */
  public String getVersionIdentifier() {
    return name + ":v" + version;
  }

  @Override
  public String toString() {
    return "PromptTemplate{"
        + "id="
        + getId()
        + ", name='"
        + name
        + '\''
        + ", version="
        + version
        + ", active="
        + active
        + ", createdAt="
        + createdAt
        + '}';
  }
}
