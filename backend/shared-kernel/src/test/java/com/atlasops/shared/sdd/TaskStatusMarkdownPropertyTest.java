package com.atlasops.shared.sdd;

import static org.assertj.core.api.Assertions.assertThat;

import net.jqwik.api.*;
import net.jqwik.api.Tuple.Tuple2;

/**
 * Property-based tests for task status markdown representation in the SDD workflow.
 *
 * <p><b>Validates: Requirements 6.3, 6.4</b>
 *
 * <p>Property 10: For any task with a valid status transition (todo→in_progress, todo→done,
 * in_progress→done, in_progress→blocked), the markdown representation SHALL be updated correctly:
 * {@code - [x]} for done, {@code - [ ] [in_progress]} for in_progress, {@code - [ ] [blocked]} for
 * blocked, {@code - [ ]} for todo.
 */
@Tag("Feature: monorepo-sdd-harness, Property 10: Task Status Markdown Representation")
class TaskStatusMarkdownPropertyTest {

  // ─── Property: Valid transitions render correct markdown for new status ───────

  @Property(tries = 100)
  void validTransition_shouldRenderCorrectMarkdownForNewStatus(
      @ForAll("validTransitions") Tuple2<TaskStatus, TaskStatus> transition,
      @ForAll("taskDescriptions") String description) {

    TaskStatus from = transition.get1();
    TaskStatus to = transition.get2();

    TaskItem originalTask = new TaskItem(description, from);
    TaskItem transitionedTask = originalTask.transitionTo(to);

    String rendered = TaskMarkdownRenderer.render(transitionedTask);

    switch (to) {
      case DONE ->
          assertThat(rendered)
              .as("DONE status should render as '- [x] %s'", description)
              .isEqualTo("- [x] " + description);
      case IN_PROGRESS ->
          assertThat(rendered)
              .as("IN_PROGRESS status should render as '- [ ] [in_progress] %s'", description)
              .isEqualTo("- [ ] [in_progress] " + description);
      case BLOCKED ->
          assertThat(rendered)
              .as("BLOCKED status should render as '- [ ] [blocked] %s'", description)
              .isEqualTo("- [ ] [blocked] " + description);
      case TODO ->
          assertThat(rendered)
              .as("TODO status should render as '- [ ] %s'", description)
              .isEqualTo("- [ ] " + description);
    }
  }

  // ─── Property: Round-trip — render then parse preserves status ────────────────

  @Property(tries = 100)
  void renderThenParse_shouldPreserveStatus(
      @ForAll("allStatuses") TaskStatus status, @ForAll("taskDescriptions") String description) {

    TaskItem original = new TaskItem(description, status);

    String rendered = TaskMarkdownRenderer.render(original);
    TaskItem parsed = TaskMarkdownParser.parseLine(rendered);

    assertThat(parsed.status())
        .as("Parsing rendered markdown for status %s should produce the same status", status)
        .isEqualTo(status);
    assertThat(parsed.description())
        .as("Parsing rendered markdown should preserve the description")
        .isEqualTo(description);
  }

  // ─── Property: '- [x]' always parses as DONE ────────────────────────────────

  @Property(tries = 100)
  void doneCheckbox_shouldAlwaysParseAsDone(@ForAll("taskDescriptions") String description) {

    String markdown = "- [x] " + description;

    TaskItem parsed = TaskMarkdownParser.parseLine(markdown);

    assertThat(parsed.status())
        .as("'- [x] %s' should always parse as DONE", description)
        .isEqualTo(TaskStatus.DONE);
  }

  // ─── Property: '- [ ] [in_progress]' always parses as IN_PROGRESS ────────────

  @Property(tries = 100)
  void inProgressMarker_shouldAlwaysParseAsInProgress(
      @ForAll("taskDescriptions") String description) {

    String markdown = "- [ ] [in_progress] " + description;

    TaskItem parsed = TaskMarkdownParser.parseLine(markdown);

    assertThat(parsed.status())
        .as("'- [ ] [in_progress] %s' should always parse as IN_PROGRESS", description)
        .isEqualTo(TaskStatus.IN_PROGRESS);
  }

  // ─── Property: '- [ ] [blocked]' always parses as BLOCKED ────────────────────

  @Property(tries = 100)
  void blockedMarker_shouldAlwaysParseAsBlocked(@ForAll("taskDescriptions") String description) {

    String markdown = "- [ ] [blocked] " + description;

    TaskItem parsed = TaskMarkdownParser.parseLine(markdown);

    assertThat(parsed.status())
        .as("'- [ ] [blocked] %s' should always parse as BLOCKED", description)
        .isEqualTo(TaskStatus.BLOCKED);
  }

  // ─── Property: '- [ ]' always parses as TODO ─────────────────────────────────

  @Property(tries = 100)
  void todoCheckbox_shouldAlwaysParseAsTodo(@ForAll("taskDescriptions") String description) {

    String markdown = "- [ ] " + description;

    TaskItem parsed = TaskMarkdownParser.parseLine(markdown);

    assertThat(parsed.status())
        .as("'- [ ] %s' should always parse as TODO", description)
        .isEqualTo(TaskStatus.TODO);
  }

  // ─── Generators ──────────────────────────────────────────────────────────────

  @Provide
  Arbitrary<Tuple2<TaskStatus, TaskStatus>> validTransitions() {
    return Arbitraries.of(
        Tuple.of(TaskStatus.TODO, TaskStatus.IN_PROGRESS),
        Tuple.of(TaskStatus.TODO, TaskStatus.DONE),
        Tuple.of(TaskStatus.IN_PROGRESS, TaskStatus.DONE),
        Tuple.of(TaskStatus.IN_PROGRESS, TaskStatus.BLOCKED));
  }

  @Provide
  Arbitrary<TaskStatus> allStatuses() {
    return Arbitraries.of(TaskStatus.values());
  }

  @Provide
  Arbitrary<String> taskDescriptions() {
    // Generate realistic task descriptions:
    // alphanumeric words separated by spaces, avoiding markdown-sensitive chars
    Arbitrary<String> word =
        Arbitraries.strings()
            .withCharRange('a', 'z')
            .withCharRange('A', 'Z')
            .withCharRange('0', '9')
            .ofMinLength(1)
            .ofMaxLength(12);

    return word.list()
        .ofMinSize(1)
        .ofMaxSize(8)
        .map(words -> String.join(" ", words))
        .filter(desc -> !desc.isBlank())
        .filter(desc -> !desc.startsWith("[")) // Avoid conflicting with status markers
        .filter(desc -> !desc.contains("[in_progress]"))
        .filter(desc -> !desc.contains("[blocked]"));
  }
}
