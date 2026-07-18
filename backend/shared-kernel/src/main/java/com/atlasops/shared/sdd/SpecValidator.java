package com.atlasops.shared.sdd;

import com.atlasops.shared.validation.FeatureNameValidator;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Validates a spec directory for completeness according to SDD workflow rules.
 *
 * <p>A valid spec directory must contain exactly four files:
 *
 * <ul>
 *   <li>{@code requirements.md}
 *   <li>{@code design.md}
 *   <li>{@code tasks.md}
 *   <li>{@code .config.kiro}
 * </ul>
 *
 * <p>Additionally, the feature-name (directory name) must be valid kebab-case with at most 50
 * characters.
 */
public final class SpecValidator {

  /** The list of required files that must be present in every spec directory. */
  public static final List<String> REQUIRED_FILES =
      List.of("requirements.md", "design.md", "tasks.md", ".config.kiro");

  private SpecValidator() {
    // Utility class — no instantiation
  }

  /**
   * Validates a spec directory at the given path.
   *
   * <p>Checks:
   *
   * <ol>
   *   <li>The directory exists
   *   <li>The feature name (last path segment) is valid kebab-case
   *   <li>All required files are present
   * </ol>
   *
   * @param specDir the path to the spec directory
   * @return a {@link SpecValidationResult} indicating the outcome
   */
  public static SpecValidationResult validate(Path specDir) {
    Objects.requireNonNull(specDir, "specDir must not be null");

    List<String> errors = new ArrayList<>();
    List<String> missingFiles = new ArrayList<>();

    // Validate directory exists
    if (!Files.isDirectory(specDir)) {
      return SpecValidationResult.invalid(
          List.of("Spec directory does not exist: " + specDir), List.of());
    }

    // Validate feature name (last segment of path)
    String featureName = specDir.getFileName().toString();
    FeatureNameValidator.ValidationResult nameResult = FeatureNameValidator.validate(featureName);
    if (!nameResult.isValid()) {
      errors.add("Invalid feature-name '" + featureName + "': " + nameResult.reason());
    }

    // Check for required files
    for (String requiredFile : REQUIRED_FILES) {
      Path filePath = specDir.resolve(requiredFile);
      if (!Files.exists(filePath)) {
        missingFiles.add(requiredFile);
      }
    }

    if (!missingFiles.isEmpty()) {
      errors.add("Missing required files: " + missingFiles);
    }

    if (errors.isEmpty()) {
      return SpecValidationResult.valid(featureName);
    }
    return SpecValidationResult.invalid(errors, missingFiles);
  }

  /**
   * Validates only the completeness of a spec directory (file presence), without checking the
   * feature name. Useful when validating directories that may not follow the kebab-case naming
   * convention yet.
   *
   * @param specDir the path to the spec directory
   * @return a {@link SpecValidationResult} indicating the outcome
   */
  public static SpecValidationResult validateCompleteness(Path specDir) {
    Objects.requireNonNull(specDir, "specDir must not be null");

    if (!Files.isDirectory(specDir)) {
      return SpecValidationResult.invalid(
          List.of("Spec directory does not exist: " + specDir), List.of());
    }

    List<String> missingFiles = new ArrayList<>();
    for (String requiredFile : REQUIRED_FILES) {
      Path filePath = specDir.resolve(requiredFile);
      if (!Files.exists(filePath)) {
        missingFiles.add(requiredFile);
      }
    }

    if (missingFiles.isEmpty()) {
      String featureName = specDir.getFileName().toString();
      return SpecValidationResult.valid(featureName);
    }

    return SpecValidationResult.invalid(
        List.of("Missing required files: " + missingFiles), missingFiles);
  }

  /** Result of a spec directory validation. */
  public record SpecValidationResult(
      boolean isComplete, String featureName, List<String> errors, List<String> missingFiles) {

    public static SpecValidationResult valid(String featureName) {
      return new SpecValidationResult(true, featureName, List.of(), List.of());
    }

    public static SpecValidationResult invalid(List<String> errors, List<String> missingFiles) {
      return new SpecValidationResult(false, null, List.copyOf(errors), List.copyOf(missingFiles));
    }
  }
}
