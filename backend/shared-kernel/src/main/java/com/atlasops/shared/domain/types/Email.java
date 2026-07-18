package com.atlasops.shared.domain.types;

import com.atlasops.shared.domain.ValueObject;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value object representing an email address. Validates basic email format: local-part@domain with
 * at least one dot in the domain.
 */
public final class Email extends ValueObject {

  private static final Pattern EMAIL_PATTERN =
      Pattern.compile("^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$");

  private final String value;

  public Email(String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("Email must not be null or empty");
    }
    if (!EMAIL_PATTERN.matcher(value).matches()) {
      throw new IllegalArgumentException("Invalid email format: " + value);
    }
    this.value = value.toLowerCase();
  }

  public String getValue() {
    return value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Email email = (Email) o;
    return Objects.equals(value, email.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }

  @Override
  public String toString() {
    return "Email{" + value + "}";
  }
}
