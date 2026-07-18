package com.atlasops.shared.validation;

import static org.assertj.core.api.Assertions.assertThat;

import com.atlasops.shared.validation.FeatureNameValidator.ValidationResult;
import java.util.regex.Pattern;
import net.jqwik.api.*;

/**
 * Property-based tests for feature-name validation in the SDD workflow.
 *
 * <p><b>Validates: Requirements 6.1</b>
 *
 * <p>Property 9: For any string used as feature-name in the SDD workflow, the system SHALL accept
 * it only if it matches kebab-case format with at most 50 characters, and upon acceptance SHALL
 * create the directory {@code .kiro/specs/{feature-name}/} with exactly three files: {@code
 * requirements.md}, {@code design.md}, {@code tasks.md}.
 */
@Tag("Feature: monorepo-sdd-harness, Property 9: Spec Feature-Name Validation")
class FeatureNameValidatorPropertyTest {

  private static final Pattern KEBAB_CASE_PATTERN = Pattern.compile("^[a-z0-9]+(-[a-z0-9]+)*$");
  private static final int MAX_LENGTH = 50;

  // ─── Property: Valid kebab-case names are always accepted ─────────────────────

  @Property(tries = 100)
  void validKebabCaseNames_shouldAlwaysBeAccepted(@ForAll("validFeatureNames") String featureName) {
    ValidationResult result = FeatureNameValidator.validate(featureName);

    assertThat(result.isValid())
        .as("Valid kebab-case name '%s' should be accepted", featureName)
        .isTrue();
    assertThat(result.reason()).isNull();
  }

  // ─── Property: Invalid names with uppercase are always rejected ───────────────

  @Property(tries = 100)
  void namesWithUppercase_shouldAlwaysBeRejected(@ForAll("namesWithUppercase") String featureName) {
    ValidationResult result = FeatureNameValidator.validate(featureName);

    assertThat(result.isValid())
        .as("Name with uppercase '%s' should be rejected", featureName)
        .isFalse();
    assertThat(result.reason()).isNotNull();
  }

  // ─── Property: Names exceeding 50 chars are always rejected ──────────────────

  @Property(tries = 100)
  void namesTooLong_shouldAlwaysBeRejected(@ForAll("tooLongNames") String featureName) {
    ValidationResult result = FeatureNameValidator.validate(featureName);

    assertThat(result.isValid())
        .as(
            "Name exceeding 50 chars '%s' (length=%d) should be rejected",
            featureName, featureName.length())
        .isFalse();
    assertThat(result.reason()).contains("at most 50 characters");
  }

  // ─── Property: Names with special chars are always rejected ───────────────────

  @Property(tries = 100)
  void namesWithSpecialChars_shouldAlwaysBeRejected(
      @ForAll("namesWithSpecialChars") String featureName) {
    ValidationResult result = FeatureNameValidator.validate(featureName);

    assertThat(result.isValid())
        .as("Name with special chars '%s' should be rejected", featureName)
        .isFalse();
    assertThat(result.reason()).isNotNull();
  }

  // ─── Property: Names with spaces are always rejected ─────────────────────────

  @Property(tries = 100)
  void namesWithSpaces_shouldAlwaysBeRejected(@ForAll("namesWithSpaces") String featureName) {
    ValidationResult result = FeatureNameValidator.validate(featureName);

    assertThat(result.isValid())
        .as("Name with spaces '%s' should be rejected", featureName)
        .isFalse();
    assertThat(result.reason()).isNotNull();
  }

  // ─── Property: Accepted names always match kebab-case pattern ─────────────────

  @Property(tries = 100)
  void acceptedNames_shouldAlwaysMatchKebabCasePattern(
      @ForAll("validFeatureNames") String featureName) {
    ValidationResult result = FeatureNameValidator.validate(featureName);

    if (result.isValid()) {
      assertThat(KEBAB_CASE_PATTERN.matcher(featureName).matches())
          .as("Accepted name '%s' must match kebab-case pattern", featureName)
          .isTrue();
      assertThat(featureName.length())
          .as("Accepted name '%s' must have at most 50 characters", featureName)
          .isLessThanOrEqualTo(MAX_LENGTH);
    }
  }

  // ─── Generators ──────────────────────────────────────────────────────────────

  @Provide
  Arbitrary<String> validFeatureNames() {
    // Generate valid kebab-case segments: [a-z0-9]+
    Arbitrary<String> segment =
        Arbitraries.strings()
            .withCharRange('a', 'z')
            .ofMinLength(1)
            .ofMaxLength(8)
            .filter(s -> !s.isEmpty());

    Arbitrary<String> segmentWithDigits =
        Arbitraries.oneOf(
            segment,
            Arbitraries.strings()
                .withCharRange('a', 'z')
                .withCharRange('0', '9')
                .ofMinLength(1)
                .ofMaxLength(8)
                .filter(s -> !s.isEmpty()));

    // Join 1-4 segments with hyphens and ensure max 50 chars
    return segmentWithDigits
        .list()
        .ofMinSize(1)
        .ofMaxSize(4)
        .map(segments -> String.join("-", segments))
        .filter(name -> name.length() <= MAX_LENGTH)
        .filter(name -> KEBAB_CASE_PATTERN.matcher(name).matches());
  }

  @Provide
  Arbitrary<String> namesWithUppercase() {
    // Generate strings that contain at least one uppercase character
    Arbitrary<String> base =
        Arbitraries.strings()
            .withCharRange('a', 'z')
            .withCharRange('A', 'Z')
            .withCharRange('0', '9')
            .withChars('-')
            .ofMinLength(2)
            .ofMaxLength(30)
            .filter(s -> s.chars().anyMatch(Character::isUpperCase))
            .filter(s -> !s.isBlank());

    return base;
  }

  @Provide
  Arbitrary<String> tooLongNames() {
    // Generate valid kebab-case segments that when joined exceed 50 chars
    Arbitrary<String> segment =
        Arbitraries.strings().withCharRange('a', 'z').ofMinLength(5).ofMaxLength(15);

    return segment
        .list()
        .ofMinSize(4)
        .ofMaxSize(8)
        .map(segments -> String.join("-", segments))
        .filter(name -> name.length() > MAX_LENGTH)
        .filter(name -> !name.isBlank());
  }

  @Provide
  Arbitrary<String> namesWithSpecialChars() {
    // Generate strings containing special characters (not a-z, 0-9, -)
    char[] specialChars = {'_', '.', '@', '#', '$', '%', '&', '!', '?', '/', '\\', '+', '='};

    return Arbitraries.strings()
        .withCharRange('a', 'z')
        .withChars(specialChars)
        .ofMinLength(2)
        .ofMaxLength(30)
        .filter(
            s ->
                s.chars()
                    .anyMatch(c -> c != '-' && !(c >= 'a' && c <= 'z') && !(c >= '0' && c <= '9')))
        .filter(s -> !s.isBlank());
  }

  @Provide
  Arbitrary<String> namesWithSpaces() {
    // Generate strings that contain at least one space
    return Arbitraries.strings()
        .withCharRange('a', 'z')
        .withChars(' ')
        .ofMinLength(3)
        .ofMaxLength(30)
        .filter(s -> s.contains(" "))
        .filter(s -> !s.isBlank());
  }
}
