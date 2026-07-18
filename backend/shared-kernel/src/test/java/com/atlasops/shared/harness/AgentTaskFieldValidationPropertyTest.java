package com.atlasops.shared.harness;

import static org.assertj.core.api.Assertions.assertThat;

import com.atlasops.shared.harness.AgentTaskValidator.ValidationResult;
import java.util.*;
import net.jqwik.api.*;

/**
 * Property-based tests for agent task field validation.
 *
 * <p><b>Validates: Requirements 8.5, 8.7</b>
 *
 * <p>Property 12: For any task payload submitted to an agent, if any required field (objective,
 * context, outOfScope, acceptanceCriteria, affectedInterfaces, risks, requiredTests,
 * validationCommands) is absent or empty, the system SHALL reject the task before execution and
 * return the names of all missing/empty fields.
 */
@Tag("Feature: monorepo-sdd-harness, Property 12: Agent Task Field Validation")
class AgentTaskFieldValidationPropertyTest {

  private static final List<String> ALL_REQUIRED_FIELDS =
      List.of(
          "objective",
          "context",
          "outOfScope",
          "acceptanceCriteria",
          "affectedInterfaces",
          "risks",
          "requiredTests",
          "validationCommands");

  // ─── Property: A fully valid task should always pass validation ───────────────

  @Property(tries = 100)
  void fullyValidTask_shouldAlwaysPassValidation(
      @ForAll("validObjectives") String objective,
      @ForAll("nonEmptyStringLists") List<String> context,
      @ForAll("nonEmptyStringLists") List<String> outOfScope,
      @ForAll("nonEmptyStringLists") List<String> acceptanceCriteria,
      @ForAll("nonEmptyStringLists") List<String> affectedInterfaces,
      @ForAll("nonEmptyStringLists") List<String> risks,
      @ForAll("nonEmptyStringLists") List<String> requiredTests,
      @ForAll("nonEmptyStringLists") List<String> validationCommands,
      @ForAll("agentRoles") AgentRole role,
      @ForAll("agentStatuses") AgentTaskStatus status) {

    AgentTask task =
        new AgentTask(
            "TASK-001",
            objective,
            context,
            outOfScope,
            acceptanceCriteria,
            affectedInterfaces,
            risks,
            requiredTests,
            validationCommands,
            role,
            status);

    ValidationResult result = AgentTaskValidator.validate(task);

    assertThat(result.isValid()).as("Task with all valid fields should pass validation").isTrue();
    assertThat(result.missingFields())
        .as("No missing fields should be reported for a valid task")
        .isEmpty();
  }

  // ─── Property: Any non-empty subset of nullified fields should be rejected ───

  @Property(tries = 100)
  void anySubsetOfNullFields_shouldRejectAndReportExactly(
      @ForAll("nonEmptyFieldSubsets") Set<String> fieldsToNullify) {

    AgentTask task = buildTaskWithNullFields(fieldsToNullify);

    ValidationResult result = AgentTaskValidator.validate(task);

    assertThat(result.isValid())
        .as("Task with null fields %s should be rejected", fieldsToNullify)
        .isFalse();
    assertThat(new HashSet<>(result.missingFields()))
        .as("Reported missing fields should exactly match nullified fields %s", fieldsToNullify)
        .isEqualTo(fieldsToNullify);
  }

  // ─── Property: Any non-empty subset of empty-list fields should be rejected ──

  @Property(tries = 100)
  void anySubsetOfEmptyListFields_shouldRejectAndReportExactly(
      @ForAll("nonEmptyListFieldSubsets") Set<String> fieldsToEmpty) {

    AgentTask task = buildTaskWithEmptyListFields(fieldsToEmpty);

    ValidationResult result = AgentTaskValidator.validate(task);

    assertThat(result.isValid())
        .as("Task with empty list fields %s should be rejected", fieldsToEmpty)
        .isFalse();
    assertThat(new HashSet<>(result.missingFields()))
        .as("Reported missing fields should exactly match emptied fields %s", fieldsToEmpty)
        .isEqualTo(fieldsToEmpty);
  }

  // ─── Property: Blank objective should be treated as missing ───────────────────

  @Property(tries = 100)
  void blankObjective_shouldAlwaysBeReportedAsMissing(
      @ForAll("blankStrings") String blankObjective) {

    AgentTask task =
        new AgentTask(
            "TASK-001",
            blankObjective,
            List.of("module"),
            List.of("out"),
            List.of("criteria"),
            List.of("interface"),
            List.of("risk"),
            List.of("test"),
            List.of("validate"),
            AgentRole.A2,
            AgentTaskStatus.PENDING);

    ValidationResult result = AgentTaskValidator.validate(task);

    assertThat(result.isValid())
        .as("Task with blank objective '%s' should be rejected", blankObjective)
        .isFalse();
    assertThat(result.missingFields())
        .as("'objective' should be reported as missing for blank value")
        .contains("objective");
  }

  // ─── Property: Lists with only blank strings should be treated as empty ──────

  @Property(tries = 100)
  void listWithOnlyBlankStrings_shouldBeReportedAsMissing(
      @ForAll("listFieldNames") String fieldName,
      @ForAll("blankOnlyLists") List<String> blankList) {

    AgentTask task = buildTaskWithSingleFieldValue(fieldName, blankList);

    ValidationResult result = AgentTaskValidator.validate(task);

    assertThat(result.isValid())
        .as("Task with field '%s' containing only blank strings should be rejected", fieldName)
        .isFalse();
    assertThat(result.missingFields())
        .as(
            "Field '%s' should be reported as missing when it contains only blank strings",
            fieldName)
        .contains(fieldName);
  }

  // ─── Property: Missing fields list should never contain duplicates ────────────

  @Property(tries = 100)
  void missingFieldsList_shouldNeverContainDuplicates(
      @ForAll("nonEmptyFieldSubsets") Set<String> fieldsToNullify) {

    AgentTask task = buildTaskWithNullFields(fieldsToNullify);

    ValidationResult result = AgentTaskValidator.validate(task);

    assertThat(result.missingFields())
        .as("Missing fields list should not contain duplicate entries")
        .doesNotHaveDuplicates();
  }

  // ─── Generators ──────────────────────────────────────────────────────────────

  @Provide
  Arbitrary<String> validObjectives() {
    return Arbitraries.strings()
        .withCharRange('a', 'z')
        .withCharRange('A', 'Z')
        .withCharRange('0', '9')
        .withChars(' ', '-', '_')
        .ofMinLength(1)
        .ofMaxLength(AgentTask.MAX_OBJECTIVE_LENGTH)
        .filter(s -> !s.isBlank());
  }

  @Provide
  Arbitrary<List<String>> nonEmptyStringLists() {
    Arbitrary<String> nonBlankString =
        Arbitraries.strings()
            .withCharRange('a', 'z')
            .withCharRange('A', 'Z')
            .withCharRange('0', '9')
            .withChars(' ', '-', '_', '/', '.')
            .ofMinLength(1)
            .ofMaxLength(50)
            .filter(s -> !s.isBlank());

    return nonBlankString.list().ofMinSize(1).ofMaxSize(5);
  }

  @Provide
  Arbitrary<AgentRole> agentRoles() {
    return Arbitraries.of(AgentRole.values());
  }

  @Provide
  Arbitrary<AgentTaskStatus> agentStatuses() {
    return Arbitraries.of(AgentTaskStatus.values());
  }

  @Provide
  Arbitrary<Set<String>> nonEmptyFieldSubsets() {
    return Arbitraries.integers()
        .between(1, (1 << ALL_REQUIRED_FIELDS.size()) - 1)
        .map(
            bitmask -> {
              Set<String> subset = new HashSet<>();
              for (int i = 0; i < ALL_REQUIRED_FIELDS.size(); i++) {
                if ((bitmask & (1 << i)) != 0) {
                  subset.add(ALL_REQUIRED_FIELDS.get(i));
                }
              }
              return subset;
            })
        .filter(s -> !s.isEmpty());
  }

  @Provide
  Arbitrary<Set<String>> nonEmptyListFieldSubsets() {
    // Only list fields (excluding "objective" which is a String)
    List<String> listFields =
        List.of(
            "context",
            "outOfScope",
            "acceptanceCriteria",
            "affectedInterfaces",
            "risks",
            "requiredTests",
            "validationCommands");

    return Arbitraries.integers()
        .between(1, (1 << listFields.size()) - 1)
        .map(
            bitmask -> {
              Set<String> subset = new HashSet<>();
              for (int i = 0; i < listFields.size(); i++) {
                if ((bitmask & (1 << i)) != 0) {
                  subset.add(listFields.get(i));
                }
              }
              return subset;
            })
        .filter(s -> !s.isEmpty());
  }

  @Provide
  Arbitrary<String> blankStrings() {
    return Arbitraries.of("", "   ", "\t", "\n", "  \t  ", "\t\n  ");
  }

  @Provide
  Arbitrary<String> listFieldNames() {
    return Arbitraries.of(
        "context",
        "outOfScope",
        "acceptanceCriteria",
        "affectedInterfaces",
        "risks",
        "requiredTests",
        "validationCommands");
  }

  @Provide
  Arbitrary<List<String>> blankOnlyLists() {
    Arbitrary<String> blankString = Arbitraries.of("", "   ", "\t", "\n", "  \t  ");
    return blankString.list().ofMinSize(1).ofMaxSize(4);
  }

  // ─── Helpers ─────────────────────────────────────────────────────────────────

  private AgentTask buildTaskWithNullFields(Set<String> fieldsToNullify) {
    return new AgentTask(
        "TASK-001",
        fieldsToNullify.contains("objective") ? null : "Valid objective",
        fieldsToNullify.contains("context") ? null : List.of("module"),
        fieldsToNullify.contains("outOfScope") ? null : List.of("out"),
        fieldsToNullify.contains("acceptanceCriteria") ? null : List.of("criteria"),
        fieldsToNullify.contains("affectedInterfaces") ? null : List.of("interface"),
        fieldsToNullify.contains("risks") ? null : List.of("risk"),
        fieldsToNullify.contains("requiredTests") ? null : List.of("test"),
        fieldsToNullify.contains("validationCommands") ? null : List.of("validate"),
        AgentRole.A2,
        AgentTaskStatus.PENDING);
  }

  private AgentTask buildTaskWithEmptyListFields(Set<String> fieldsToEmpty) {
    return new AgentTask(
        "TASK-001",
        "Valid objective",
        fieldsToEmpty.contains("context") ? List.of() : List.of("module"),
        fieldsToEmpty.contains("outOfScope") ? List.of() : List.of("out"),
        fieldsToEmpty.contains("acceptanceCriteria") ? List.of() : List.of("criteria"),
        fieldsToEmpty.contains("affectedInterfaces") ? List.of() : List.of("interface"),
        fieldsToEmpty.contains("risks") ? List.of() : List.of("risk"),
        fieldsToEmpty.contains("requiredTests") ? List.of() : List.of("test"),
        fieldsToEmpty.contains("validationCommands") ? List.of() : List.of("validate"),
        AgentRole.A2,
        AgentTaskStatus.PENDING);
  }

  private AgentTask buildTaskWithSingleFieldValue(String fieldName, List<String> value) {
    return new AgentTask(
        "TASK-001",
        "Valid objective",
        fieldName.equals("context") ? value : List.of("module"),
        fieldName.equals("outOfScope") ? value : List.of("out"),
        fieldName.equals("acceptanceCriteria") ? value : List.of("criteria"),
        fieldName.equals("affectedInterfaces") ? value : List.of("interface"),
        fieldName.equals("risks") ? value : List.of("risk"),
        fieldName.equals("requiredTests") ? value : List.of("test"),
        fieldName.equals("validationCommands") ? value : List.of("validate"),
        AgentRole.A2,
        AgentTaskStatus.PENDING);
  }
}
