package com.atlasops.boot.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.*;
import java.util.stream.Collectors;
import net.jqwik.api.*;

/**
 * Property-based tests for environment variable validation at application startup.
 *
 * <p><b>Validates: Requirements 3.7</b>
 *
 * <p>Property 1: For any subset of required environment variables that is incomplete (at least one
 * variable absent or empty), the application startup SHALL fail with an error message listing
 * exactly the names of the missing/empty variables.
 */
@Tag("Feature: monorepo-sdd-harness, Property 1: Environment Variable Validation")
class EnvironmentValidatorPropertyTest {

  private static final List<String> REQUIRED_VARIABLES = EnvironmentValidator.REQUIRED_VARIABLES;

  // ─── Property: Complete environment always passes validation ──────────────────

  @Property(tries = 100)
  void completeEnvironment_shouldAlwaysPassValidation(
      @ForAll("validEnvironmentValues") Map<String, String> validValues) {
    // Build a map with all required variables populated with valid (non-blank) values
    Map<String, String> vars = new HashMap<>();
    List<String> valueList = new ArrayList<>(validValues.values());
    for (int i = 0; i < REQUIRED_VARIABLES.size(); i++) {
      vars.put(REQUIRED_VARIABLES.get(i), valueList.get(i % valueList.size()));
    }

    List<String> missing = EnvironmentValidator.findMissingVariables(vars);

    assertThat(missing)
        .as("When all required variables are present with non-blank values, validation should pass")
        .isEmpty();
  }

  // ─── Property: Incomplete environment always fails with exact missing names ───

  @Property(tries = 100)
  void incompleteEnvironment_shouldFailListingExactlyMissingVariables(
      @ForAll("nonEmptySubsetIndices") Set<Integer> missingIndices) {
    // Start with all variables present
    Map<String, String> vars = allVariablesPresent();

    // Remove the subset of variables identified by missingIndices
    Set<String> expectedMissing =
        missingIndices.stream().map(REQUIRED_VARIABLES::get).collect(Collectors.toSet());

    for (String varName : expectedMissing) {
      vars.remove(varName);
    }

    List<String> actualMissing = EnvironmentValidator.findMissingVariables(vars);

    assertThat(actualMissing)
        .as("Missing variables should be exactly: %s", expectedMissing)
        .containsExactlyInAnyOrderElementsOf(expectedMissing);
  }

  // ─── Property: Incomplete environment throws with all missing names in message ─

  @Property(tries = 100)
  void incompleteEnvironment_shouldThrowWithAllMissingNamesInMessage(
      @ForAll("nonEmptySubsetIndices") Set<Integer> missingIndices) {
    Map<String, String> vars = allVariablesPresent();

    Set<String> expectedMissing =
        missingIndices.stream().map(REQUIRED_VARIABLES::get).collect(Collectors.toSet());

    for (String varName : expectedMissing) {
      vars.remove(varName);
    }

    assertThatThrownBy(() -> EnvironmentValidator.validate(vars))
        .isInstanceOf(EnvironmentValidationException.class)
        .satisfies(
            exception -> {
              String message = exception.getMessage();
              for (String varName : expectedMissing) {
                assertThat(message)
                    .as("Error message should contain missing variable name '%s'", varName)
                    .contains(varName);
              }
            });
  }

  // ─── Property: Empty strings are treated as absent variables ──────────────────

  @Property(tries = 100)
  void emptyStringValues_shouldBeTreatedAsAbsent(
      @ForAll("nonEmptySubsetIndices") Set<Integer> emptyIndices,
      @ForAll("blankStrings") String blankValue) {
    Map<String, String> vars = allVariablesPresent();

    Set<String> expectedMissing =
        emptyIndices.stream().map(REQUIRED_VARIABLES::get).collect(Collectors.toSet());

    // Set chosen variables to blank value instead of removing
    for (String varName : expectedMissing) {
      vars.put(varName, blankValue);
    }

    List<String> actualMissing = EnvironmentValidator.findMissingVariables(vars);

    assertThat(actualMissing)
        .as("Variables set to '%s' should be detected as missing: %s", blankValue, expectedMissing)
        .containsExactlyInAnyOrderElementsOf(expectedMissing);
  }

  // ─── Property: Mix of absent and empty variables all reported ─────────────────

  @Property(tries = 100)
  void mixOfAbsentAndEmpty_shouldReportAll(
      @ForAll("disjointSubsetPair") List<Set<Integer>> subsetPair) {
    Set<Integer> absentIndices = subsetPair.get(0);
    Set<Integer> emptyIndices = subsetPair.get(1);

    Map<String, String> vars = allVariablesPresent();

    Set<String> allExpectedMissing = new HashSet<>();

    for (int idx : absentIndices) {
      String varName = REQUIRED_VARIABLES.get(idx);
      vars.remove(varName);
      allExpectedMissing.add(varName);
    }

    for (int idx : emptyIndices) {
      String varName = REQUIRED_VARIABLES.get(idx);
      vars.put(varName, "");
      allExpectedMissing.add(varName);
    }

    List<String> actualMissing = EnvironmentValidator.findMissingVariables(vars);

    assertThat(actualMissing)
        .as("Both absent and empty variables should be reported as missing")
        .containsExactlyInAnyOrderElementsOf(allExpectedMissing);
  }

  // ─── Helpers ─────────────────────────────────────────────────────────────────

  private Map<String, String> allVariablesPresent() {
    Map<String, String> vars = new HashMap<>();
    vars.put("APP_ENV", "local");
    vars.put("APP_PORT", "8080");
    vars.put("DATABASE_URL", "jdbc:postgresql://localhost:5432/atlasops");
    vars.put("REDIS_URL", "redis://localhost:6379");
    vars.put("OBJECT_STORAGE_ENDPOINT", "http://localhost:9000");
    vars.put("OBJECT_STORAGE_BUCKET", "atlasops-local");
    vars.put("JWT_ISSUER", "atlasops-local");
    vars.put("JWT_AUDIENCE", "atlasops-api");
    vars.put("LOG_LEVEL", "INFO");
    return vars;
  }

  // ─── Generators ──────────────────────────────────────────────────────────────

  @Provide
  Arbitrary<Map<String, String>> validEnvironmentValues() {
    // Generate non-blank strings for all required variables
    Arbitrary<String> nonBlankValue =
        Arbitraries.strings()
            .withCharRange('a', 'z')
            .withCharRange('0', '9')
            .withChars('-', '_', ':', '/', '.')
            .ofMinLength(1)
            .ofMaxLength(50)
            .filter(s -> !s.isBlank());

    return nonBlankValue
        .list()
        .ofSize(REQUIRED_VARIABLES.size())
        .map(
            values -> {
              Map<String, String> map = new HashMap<>();
              for (int i = 0; i < REQUIRED_VARIABLES.size(); i++) {
                map.put(REQUIRED_VARIABLES.get(i), values.get(i));
              }
              return map;
            });
  }

  @Provide
  Arbitrary<Set<Integer>> nonEmptySubsetIndices() {
    // Generate a non-empty subset of indices into REQUIRED_VARIABLES
    int maxIndex = REQUIRED_VARIABLES.size() - 1;
    return Arbitraries.integers()
        .between(0, maxIndex)
        .set()
        .ofMinSize(1)
        .ofMaxSize(REQUIRED_VARIABLES.size());
  }

  @Provide
  Arbitrary<String> blankStrings() {
    // Generate empty or whitespace-only strings
    return Arbitraries.oneOf(
        Arbitraries.just(""),
        Arbitraries.just(" "),
        Arbitraries.just("  "),
        Arbitraries.just("\t"),
        Arbitraries.just("   \t  "));
  }

  @Provide
  Arbitrary<List<Set<Integer>>> disjointSubsetPair() {
    // Generate two disjoint non-empty subsets of indices covering at least one var
    int maxIndex = REQUIRED_VARIABLES.size() - 1;
    return Arbitraries.integers()
        .between(0, maxIndex)
        .set()
        .ofMinSize(2)
        .ofMaxSize(REQUIRED_VARIABLES.size())
        .map(
            combined -> {
              List<Integer> shuffled = new ArrayList<>(combined);
              Collections.shuffle(shuffled);
              int splitPoint = Math.max(1, shuffled.size() / 2);
              Set<Integer> first = new HashSet<>(shuffled.subList(0, splitPoint));
              Set<Integer> second = new HashSet<>(shuffled.subList(splitPoint, shuffled.size()));
              return List.of(first, second);
            });
  }
}
