package com.atlasops.shared.domain.types;

import com.atlasops.shared.domain.ValueObject;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Value object representing a correlation ID in UUID format. Used to trace requests across service
 * boundaries.
 */
public final class CorrelationId extends ValueObject {

  private static final Pattern UUID_PATTERN =
      Pattern.compile(
          "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

  private final String value;

  public CorrelationId(String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("CorrelationId must not be null or empty");
    }
    if (!UUID_PATTERN.matcher(value).matches()) {
      throw new IllegalArgumentException("CorrelationId must be in UUID format: " + value);
    }
    this.value = value;
  }

  /** Factory method to generate a new random correlation ID. */
  public static CorrelationId generate() {
    return new CorrelationId(UUID.randomUUID().toString());
  }

  public String getValue() {
    return value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CorrelationId that = (CorrelationId) o;
    return Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }

  @Override
  public String toString() {
    return "CorrelationId{" + value + "}";
  }
}
