package com.atlasops.shared.sdd;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents the content of a {@code .config.kiro} file in a spec directory.
 *
 * <p>Required fields:
 *
 * <ul>
 *   <li>{@code specId} — unique identifier for the spec (UUID string)
 *   <li>{@code workflowType} — must be "sdd"
 *   <li>{@code specType} — must be "feature" or "fix"
 * </ul>
 *
 * @param specId unique identifier for the spec (UUID string)
 * @param workflowType workflow type, must be "sdd"
 * @param specType spec type, must be "feature" or "fix"
 */
public record SpecConfig(String specId, String workflowType, String specType) {

  private static final String VALID_WORKFLOW_TYPE = "sdd";
  private static final List<String> VALID_SPEC_TYPES = List.of("feature", "fix");

  /**
   * Validates this SpecConfig and returns a result indicating any issues found.
   *
   * @return a {@link ValidationResult} with validation outcome
   */
  public ValidationResult validate() {
    List<String> errors = new ArrayList<>();

    if (specId == null || specId.isBlank()) {
      errors.add("specId is required and must not be empty");
    }

    if (workflowType == null || workflowType.isBlank()) {
      errors.add("workflowType is required and must not be empty");
    } else if (!VALID_WORKFLOW_TYPE.equals(workflowType)) {
      errors.add("workflowType must be 'sdd', got '" + workflowType + "'");
    }

    if (specType == null || specType.isBlank()) {
      errors.add("specType is required and must not be empty");
    } else if (!VALID_SPEC_TYPES.contains(specType)) {
      errors.add("specType must be 'feature' or 'fix', got '" + specType + "'");
    }

    if (errors.isEmpty()) {
      return ValidationResult.valid();
    }
    return ValidationResult.invalid(errors);
  }

  /** Result of a SpecConfig validation. */
  public record ValidationResult(boolean isValid, List<String> errors) {

    public static ValidationResult valid() {
      return new ValidationResult(true, List.of());
    }

    public static ValidationResult invalid(List<String> errors) {
      Objects.requireNonNull(errors);
      return new ValidationResult(false, List.copyOf(errors));
    }
  }
}
