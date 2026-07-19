package com.atlasops.tenants.domain;

import com.atlasops.shared.domain.ValueObject;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value object representing a tenant name. Validates that the name is between 3 and 100 characters
 * and contains only alphanumeric characters, hyphens, and spaces.
 */
public final class TenantName extends ValueObject {

  private static final int MIN_LENGTH = 3;
  private static final int MAX_LENGTH = 100;
  private static final Pattern VALID_PATTERN = Pattern.compile("^[a-zA-Z0-9\\- ]+$");

  private final String value;

  public TenantName(String value) {
    if (value == null) {
      throw new IllegalArgumentException("Tenant name must not be null");
    }
    String trimmed = value.trim();
    if (trimmed.length() < MIN_LENGTH) {
      throw new IllegalArgumentException(
          "Tenant name must be at least " + MIN_LENGTH + " characters, got " + trimmed.length());
    }
    if (trimmed.length() > MAX_LENGTH) {
      throw new IllegalArgumentException(
          "Tenant name must be at most " + MAX_LENGTH + " characters, got " + trimmed.length());
    }
    if (!VALID_PATTERN.matcher(trimmed).matches()) {
      throw new IllegalArgumentException(
          "Tenant name must contain only alphanumeric characters, hyphens, and spaces");
    }
    this.value = trimmed;
  }

  public String getValue() {
    return value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TenantName that = (TenantName) o;
    return value.equalsIgnoreCase(that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value.toLowerCase());
  }

  @Override
  public String toString() {
    return "TenantName{" + value + "}";
  }
}
