package com.atlasops.shared.harness;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Validates an {@link AgentTask} for completeness before execution.
 *
 * <p>All required fields must be present and non-empty. The validator returns a list of
 * missing/empty field names if the task is invalid.
 */
public final class AgentTaskValidator {

  private AgentTaskValidator() {
    // Utility class — no instantiation
  }

  /**
   * Validates the given agent task.
   *
   * <p>Checks that all required fields are present and non-empty: objective, context, outOfScope,
   * acceptanceCriteria, affectedInterfaces, risks, requiredTests, validationCommands.
   *
   * <p>Additionally validates:
   *
   * <ul>
   *   <li>objective must not exceed 500 characters
   *   <li>acceptanceCriteria must have at least 1 item
   *   <li>requiredTests must have at least 1 item
   *   <li>validationCommands must have at least 1 item
   * </ul>
   *
   * @param task the agent task to validate
   * @return a {@link ValidationResult} indicating outcome
   */
  public static ValidationResult validate(AgentTask task) {
    Objects.requireNonNull(task, "task must not be null");

    List<String> missingFields = new ArrayList<>();

    // Validate objective
    if (isBlankOrNull(task.objective())) {
      missingFields.add("objective");
    } else if (task.objective().length() > AgentTask.MAX_OBJECTIVE_LENGTH) {
      missingFields.add("objective");
    }

    // Validate list fields (must be non-null and non-empty)
    if (isEmptyList(task.context())) {
      missingFields.add("context");
    }

    if (isEmptyList(task.outOfScope())) {
      missingFields.add("outOfScope");
    }

    if (isEmptyList(task.acceptanceCriteria())) {
      missingFields.add("acceptanceCriteria");
    }

    if (isEmptyList(task.affectedInterfaces())) {
      missingFields.add("affectedInterfaces");
    }

    if (isEmptyList(task.risks())) {
      missingFields.add("risks");
    }

    if (isEmptyList(task.requiredTests())) {
      missingFields.add("requiredTests");
    }

    if (isEmptyList(task.validationCommands())) {
      missingFields.add("validationCommands");
    }

    if (missingFields.isEmpty()) {
      return ValidationResult.valid();
    }
    return ValidationResult.invalid(missingFields);
  }

  private static boolean isBlankOrNull(String value) {
    return value == null || value.isBlank();
  }

  private static boolean isEmptyList(List<String> list) {
    if (list == null || list.isEmpty()) {
      return true;
    }
    // All entries must be non-blank
    return list.stream().allMatch(s -> s == null || s.isBlank());
  }

  /**
   * Result of agent task validation.
   *
   * @param isValid true if the task is valid
   * @param missingFields list of field names that are missing or empty
   */
  public record ValidationResult(boolean isValid, List<String> missingFields) {

    public static ValidationResult valid() {
      return new ValidationResult(true, List.of());
    }

    public static ValidationResult invalid(List<String> missingFields) {
      Objects.requireNonNull(missingFields);
      return new ValidationResult(false, List.copyOf(missingFields));
    }
  }
}
