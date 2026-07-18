package com.atlasops.shared.validation;

import java.util.regex.Pattern;

/**
 * Validates feature names for use in the SDD workflow.
 *
 * <p>A valid feature-name must:
 *
 * <ul>
 *   <li>Be kebab-case (lowercase letters, digits, and hyphens only)
 *   <li>Not start or end with a hyphen
 *   <li>Not contain consecutive hyphens
 *   <li>Have at most 50 characters
 *   <li>Not be empty or blank
 * </ul>
 */
public final class FeatureNameValidator {

  private static final int MAX_LENGTH = 50;

  /**
   * Regex for kebab-case: starts with lowercase letter or digit, allows single hyphens between
   * segments of lowercase letters/digits, ends with a lowercase letter or digit.
   */
  private static final Pattern KEBAB_CASE_PATTERN = Pattern.compile("^[a-z0-9]+(-[a-z0-9]+)*$");

  private FeatureNameValidator() {
    // Utility class — no instantiation
  }

  /**
   * Validates a feature name and returns a result with the outcome.
   *
   * @param featureName the feature name to validate
   * @return a {@link ValidationResult} indicating valid or invalid with reason
   */
  public static ValidationResult validate(String featureName) {
    if (featureName == null || featureName.isBlank()) {
      return ValidationResult.invalid("Feature name must not be null or empty");
    }

    if (featureName.length() > MAX_LENGTH) {
      return ValidationResult.invalid(
          "Feature name must have at most "
              + MAX_LENGTH
              + " characters, got "
              + featureName.length());
    }

    if (!KEBAB_CASE_PATTERN.matcher(featureName).matches()) {
      if (featureName.startsWith("-")) {
        return ValidationResult.invalid("Feature name must not start with a hyphen");
      }
      if (featureName.endsWith("-")) {
        return ValidationResult.invalid("Feature name must not end with a hyphen");
      }
      if (featureName.contains("--")) {
        return ValidationResult.invalid("Feature name must not contain consecutive hyphens");
      }
      return ValidationResult.invalid(
          "Feature name must be kebab-case (lowercase letters, digits, and hyphens only)");
    }

    return ValidationResult.valid();
  }

  /** Represents the result of a feature-name validation. */
  public record ValidationResult(boolean isValid, String reason) {

    public static ValidationResult valid() {
      return new ValidationResult(true, null);
    }

    public static ValidationResult invalid(String reason) {
      return new ValidationResult(false, reason);
    }
  }
}
