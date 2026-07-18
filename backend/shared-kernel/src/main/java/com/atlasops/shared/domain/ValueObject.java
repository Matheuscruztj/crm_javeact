package com.atlasops.shared.domain;

/**
 * Base class for value objects. Equality is based on structural content, not identity. Subclasses
 * must implement {@link #equals} and {@link #hashCode}.
 */
public abstract class ValueObject {

  @Override
  public abstract boolean equals(Object o);

  @Override
  public abstract int hashCode();

  @Override
  public abstract String toString();
}
