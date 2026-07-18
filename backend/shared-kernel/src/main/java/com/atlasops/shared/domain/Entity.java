package com.atlasops.shared.domain;

import java.util.Objects;

/**
 * Base class for all domain entities. Identity is defined by the {@code id} field.
 *
 * @param <ID> the type of the entity identifier
 */
public abstract class Entity<ID> {

  private final ID id;

  protected Entity(ID id) {
    this.id = Objects.requireNonNull(id, "Entity id must not be null");
  }

  public ID getId() {
    return id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Entity<?> entity = (Entity<?>) o;
    return Objects.equals(id, entity.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{id=" + id + "}";
  }
}
