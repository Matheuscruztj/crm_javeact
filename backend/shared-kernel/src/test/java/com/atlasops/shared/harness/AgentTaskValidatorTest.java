package com.atlasops.shared.harness;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class AgentTaskValidatorTest {

  private static AgentTask validTask() {
    return new AgentTask(
        "TASK-001",
        "Implement user registration endpoint",
        List.of("backend/users", "backend/auth"),
        List.of("frontend changes", "email service"),
        List.of("POST /api/v1/users returns 201"),
        List.of("UserPort", "AuthPort"),
        List.of("nenhum identificado"),
        List.of("./gradlew :backend:users:test"),
        List.of("curl -X POST http://localhost:8080/api/v1/users"),
        AgentRole.A2,
        AgentTaskStatus.PENDING);
  }

  @Test
  void should_beValid_when_allFieldsPresent() {
    var result = AgentTaskValidator.validate(validTask());

    assertThat(result.isValid()).isTrue();
    assertThat(result.missingFields()).isEmpty();
  }

  @Test
  void should_rejectNull_when_taskIsNull() {
    assertThatThrownBy(() -> AgentTaskValidator.validate(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("task must not be null");
  }

  @Test
  void should_reportObjective_when_objectiveIsNull() {
    var task =
        new AgentTask(
            "TASK-001",
            null,
            List.of("module"),
            List.of("out"),
            List.of("criteria"),
            List.of("interface"),
            List.of("risk"),
            List.of("test"),
            List.of("validate"),
            AgentRole.A2,
            AgentTaskStatus.PENDING);

    var result = AgentTaskValidator.validate(task);

    assertThat(result.isValid()).isFalse();
    assertThat(result.missingFields()).contains("objective");
  }

  @Test
  void should_reportObjective_when_objectiveIsBlank() {
    var task =
        new AgentTask(
            "TASK-001",
            "   ",
            List.of("module"),
            List.of("out"),
            List.of("criteria"),
            List.of("interface"),
            List.of("risk"),
            List.of("test"),
            List.of("validate"),
            AgentRole.A2,
            AgentTaskStatus.PENDING);

    var result = AgentTaskValidator.validate(task);

    assertThat(result.isValid()).isFalse();
    assertThat(result.missingFields()).contains("objective");
  }

  @Test
  void should_reportObjective_when_objectiveExceeds500Chars() {
    String longObjective = "x".repeat(501);
    var task =
        new AgentTask(
            "TASK-001",
            longObjective,
            List.of("module"),
            List.of("out"),
            List.of("criteria"),
            List.of("interface"),
            List.of("risk"),
            List.of("test"),
            List.of("validate"),
            AgentRole.A2,
            AgentTaskStatus.PENDING);

    var result = AgentTaskValidator.validate(task);

    assertThat(result.isValid()).isFalse();
    assertThat(result.missingFields()).contains("objective");
  }

  @Test
  void should_beValid_when_objectiveIsExactly500Chars() {
    String objective = "x".repeat(500);
    var task =
        new AgentTask(
            "TASK-001",
            objective,
            List.of("module"),
            List.of("out"),
            List.of("criteria"),
            List.of("interface"),
            List.of("risk"),
            List.of("test"),
            List.of("validate"),
            AgentRole.A2,
            AgentTaskStatus.PENDING);

    var result = AgentTaskValidator.validate(task);

    assertThat(result.isValid()).isTrue();
  }

  @Test
  void should_reportContext_when_contextIsNull() {
    var task =
        new AgentTask(
            "TASK-001",
            "objective",
            null,
            List.of("out"),
            List.of("criteria"),
            List.of("interface"),
            List.of("risk"),
            List.of("test"),
            List.of("validate"),
            AgentRole.A2,
            AgentTaskStatus.PENDING);

    var result = AgentTaskValidator.validate(task);

    assertThat(result.isValid()).isFalse();
    assertThat(result.missingFields()).contains("context");
  }

  @Test
  void should_reportContext_when_contextIsEmptyList() {
    var task =
        new AgentTask(
            "TASK-001",
            "objective",
            List.of(),
            List.of("out"),
            List.of("criteria"),
            List.of("interface"),
            List.of("risk"),
            List.of("test"),
            List.of("validate"),
            AgentRole.A2,
            AgentTaskStatus.PENDING);

    var result = AgentTaskValidator.validate(task);

    assertThat(result.isValid()).isFalse();
    assertThat(result.missingFields()).contains("context");
  }

  @Test
  void should_reportOutOfScope_when_outOfScopeIsEmpty() {
    var task =
        new AgentTask(
            "TASK-001",
            "objective",
            List.of("module"),
            List.of(),
            List.of("criteria"),
            List.of("interface"),
            List.of("risk"),
            List.of("test"),
            List.of("validate"),
            AgentRole.A2,
            AgentTaskStatus.PENDING);

    var result = AgentTaskValidator.validate(task);

    assertThat(result.isValid()).isFalse();
    assertThat(result.missingFields()).contains("outOfScope");
  }

  @Test
  void should_reportAcceptanceCriteria_when_empty() {
    var task =
        new AgentTask(
            "TASK-001",
            "objective",
            List.of("module"),
            List.of("out"),
            List.of(),
            List.of("interface"),
            List.of("risk"),
            List.of("test"),
            List.of("validate"),
            AgentRole.A2,
            AgentTaskStatus.PENDING);

    var result = AgentTaskValidator.validate(task);

    assertThat(result.isValid()).isFalse();
    assertThat(result.missingFields()).contains("acceptanceCriteria");
  }

  @Test
  void should_reportAffectedInterfaces_when_empty() {
    var task =
        new AgentTask(
            "TASK-001",
            "objective",
            List.of("module"),
            List.of("out"),
            List.of("criteria"),
            List.of(),
            List.of("risk"),
            List.of("test"),
            List.of("validate"),
            AgentRole.A2,
            AgentTaskStatus.PENDING);

    var result = AgentTaskValidator.validate(task);

    assertThat(result.isValid()).isFalse();
    assertThat(result.missingFields()).contains("affectedInterfaces");
  }

  @Test
  void should_reportRisks_when_empty() {
    var task =
        new AgentTask(
            "TASK-001",
            "objective",
            List.of("module"),
            List.of("out"),
            List.of("criteria"),
            List.of("interface"),
            List.of(),
            List.of("test"),
            List.of("validate"),
            AgentRole.A2,
            AgentTaskStatus.PENDING);

    var result = AgentTaskValidator.validate(task);

    assertThat(result.isValid()).isFalse();
    assertThat(result.missingFields()).contains("risks");
  }

  @Test
  void should_reportRequiredTests_when_empty() {
    var task =
        new AgentTask(
            "TASK-001",
            "objective",
            List.of("module"),
            List.of("out"),
            List.of("criteria"),
            List.of("interface"),
            List.of("risk"),
            List.of(),
            List.of("validate"),
            AgentRole.A2,
            AgentTaskStatus.PENDING);

    var result = AgentTaskValidator.validate(task);

    assertThat(result.isValid()).isFalse();
    assertThat(result.missingFields()).contains("requiredTests");
  }

  @Test
  void should_reportValidationCommands_when_empty() {
    var task =
        new AgentTask(
            "TASK-001",
            "objective",
            List.of("module"),
            List.of("out"),
            List.of("criteria"),
            List.of("interface"),
            List.of("risk"),
            List.of("test"),
            List.of(),
            AgentRole.A2,
            AgentTaskStatus.PENDING);

    var result = AgentTaskValidator.validate(task);

    assertThat(result.isValid()).isFalse();
    assertThat(result.missingFields()).contains("validationCommands");
  }

  @Test
  void should_reportMultipleFields_when_multipleFieldsInvalid() {
    var task =
        new AgentTask(
            "TASK-001",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            AgentRole.A2,
            AgentTaskStatus.PENDING);

    var result = AgentTaskValidator.validate(task);

    assertThat(result.isValid()).isFalse();
    assertThat(result.missingFields())
        .containsExactlyInAnyOrder(
            "objective",
            "context",
            "outOfScope",
            "acceptanceCriteria",
            "affectedInterfaces",
            "risks",
            "requiredTests",
            "validationCommands");
  }

  @Test
  void should_reportField_when_listContainsOnlyBlankStrings() {
    var task =
        new AgentTask(
            "TASK-001",
            "objective",
            List.of("  ", ""),
            List.of("out"),
            List.of("criteria"),
            List.of("interface"),
            List.of("risk"),
            List.of("test"),
            List.of("validate"),
            AgentRole.A2,
            AgentTaskStatus.PENDING);

    var result = AgentTaskValidator.validate(task);

    assertThat(result.isValid()).isFalse();
    assertThat(result.missingFields()).contains("context");
  }

  static Stream<Arguments> singleMissingFieldProvider() {
    return Stream.of(
        Arguments.of(
            "objective",
            new AgentTask(
                "id",
                null,
                List.of("m"),
                List.of("o"),
                List.of("c"),
                List.of("i"),
                List.of("r"),
                List.of("t"),
                List.of("v"),
                AgentRole.A1,
                AgentTaskStatus.PENDING)),
        Arguments.of(
            "context",
            new AgentTask(
                "id",
                "obj",
                null,
                List.of("o"),
                List.of("c"),
                List.of("i"),
                List.of("r"),
                List.of("t"),
                List.of("v"),
                AgentRole.A1,
                AgentTaskStatus.PENDING)),
        Arguments.of(
            "outOfScope",
            new AgentTask(
                "id",
                "obj",
                List.of("m"),
                null,
                List.of("c"),
                List.of("i"),
                List.of("r"),
                List.of("t"),
                List.of("v"),
                AgentRole.A1,
                AgentTaskStatus.PENDING)),
        Arguments.of(
            "acceptanceCriteria",
            new AgentTask(
                "id",
                "obj",
                List.of("m"),
                List.of("o"),
                null,
                List.of("i"),
                List.of("r"),
                List.of("t"),
                List.of("v"),
                AgentRole.A1,
                AgentTaskStatus.PENDING)),
        Arguments.of(
            "affectedInterfaces",
            new AgentTask(
                "id",
                "obj",
                List.of("m"),
                List.of("o"),
                List.of("c"),
                null,
                List.of("r"),
                List.of("t"),
                List.of("v"),
                AgentRole.A1,
                AgentTaskStatus.PENDING)),
        Arguments.of(
            "risks",
            new AgentTask(
                "id",
                "obj",
                List.of("m"),
                List.of("o"),
                List.of("c"),
                List.of("i"),
                null,
                List.of("t"),
                List.of("v"),
                AgentRole.A1,
                AgentTaskStatus.PENDING)),
        Arguments.of(
            "requiredTests",
            new AgentTask(
                "id",
                "obj",
                List.of("m"),
                List.of("o"),
                List.of("c"),
                List.of("i"),
                List.of("r"),
                null,
                List.of("v"),
                AgentRole.A1,
                AgentTaskStatus.PENDING)),
        Arguments.of(
            "validationCommands",
            new AgentTask(
                "id",
                "obj",
                List.of("m"),
                List.of("o"),
                List.of("c"),
                List.of("i"),
                List.of("r"),
                List.of("t"),
                null,
                AgentRole.A1,
                AgentTaskStatus.PENDING)));
  }

  @ParameterizedTest
  @MethodSource("singleMissingFieldProvider")
  void should_reportExactlyOneField_when_onlyOneFieldMissing(String expectedField, AgentTask task) {
    var result = AgentTaskValidator.validate(task);

    assertThat(result.isValid()).isFalse();
    assertThat(result.missingFields()).containsExactly(expectedField);
  }
}
