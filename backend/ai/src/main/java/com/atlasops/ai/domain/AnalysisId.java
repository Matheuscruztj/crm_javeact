package com.atlasops.ai.domain;

import com.atlasops.shared.domain.ValueObject;
import java.util.Objects;

/** Value object representing the unique identifier for an AI analysis. */
public final class AnalysisId extends ValueObject {

  private final String value;

  public AnalysisId(String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("AnalysisId must not be null or empty");
    }
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AnalysisId that = (AnalysisId) o;
    return Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }

  @Override
  public String toString() {
    return "AnalysisId{" + value + "}";
  }
}
