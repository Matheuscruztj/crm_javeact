package com.atlasops.shared.domain.types;

import com.atlasops.shared.domain.ValueObject;
import java.util.Objects;

/** Value object representing a tenant identifier. Wraps a non-null, non-empty String. */
public final class TenantId extends ValueObject {

  private final String value;

  public TenantId(String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("TenantId must not be null or empty");
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
    TenantId tenantId = (TenantId) o;
    return Objects.equals(value, tenantId.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }

  @Override
  public String toString() {
    return "TenantId{" + value + "}";
  }
}
