package com.atlasops.shared.sdd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("TaskItem")
class TaskItemTest {

  @Nested
  @DisplayName("Construction")
  class Construction {

    @Test
    @DisplayName("should create task with description and status")
    void should_createTask_when_validParameters() {
      TaskItem task = new TaskItem("Do something", TaskStatus.TODO);

      assertThat(task.description()).isEqualTo("Do something");
      assertThat(task.status()).isEqualTo(TaskStatus.TODO);
      assertThat(task.children()).isEmpty();
    }

    @Test
    @DisplayName("should reject null description")
    void should_throw_when_descriptionIsNull() {
      assertThatThrownBy(() -> new TaskItem(null, TaskStatus.TODO))
          .isInstanceOf(NullPointerException.class)
          .hasMessageContaining("description");
    }

    @Test
    @DisplayName("should reject null status")
    void should_throw_when_statusIsNull() {
      assertThatThrownBy(() -> new TaskItem("Task", null))
          .isInstanceOf(NullPointerException.class)
          .hasMessageContaining("status");
    }
  }

  @Nested
  @DisplayName("Valid transitions")
  class ValidTransitions {

    @Test
    @DisplayName("should transition TODO → IN_PROGRESS")
    void should_transitionToInProgress_when_currentStatusIsTodo() {
      TaskItem task = new TaskItem("Task", TaskStatus.TODO);

      TaskItem updated = task.transitionTo(TaskStatus.IN_PROGRESS);

      assertThat(updated.status()).isEqualTo(TaskStatus.IN_PROGRESS);
      assertThat(updated.description()).isEqualTo("Task");
    }

    @Test
    @DisplayName("should transition TODO → DONE")
    void should_transitionToDone_when_currentStatusIsTodo() {
      TaskItem task = new TaskItem("Task", TaskStatus.TODO);

      TaskItem updated = task.transitionTo(TaskStatus.DONE);

      assertThat(updated.status()).isEqualTo(TaskStatus.DONE);
    }

    @Test
    @DisplayName("should transition IN_PROGRESS → DONE")
    void should_transitionToDone_when_currentStatusIsInProgress() {
      TaskItem task = new TaskItem("Task", TaskStatus.IN_PROGRESS);

      TaskItem updated = task.transitionTo(TaskStatus.DONE);

      assertThat(updated.status()).isEqualTo(TaskStatus.DONE);
    }

    @Test
    @DisplayName("should transition IN_PROGRESS → BLOCKED")
    void should_transitionToBlocked_when_currentStatusIsInProgress() {
      TaskItem task = new TaskItem("Task", TaskStatus.IN_PROGRESS);

      TaskItem updated = task.transitionTo(TaskStatus.BLOCKED);

      assertThat(updated.status()).isEqualTo(TaskStatus.BLOCKED);
    }

    @Test
    @DisplayName("should not mutate original task on transition")
    void should_notMutateOriginal_when_transitioned() {
      TaskItem original = new TaskItem("Task", TaskStatus.TODO);

      TaskItem updated = original.transitionTo(TaskStatus.DONE);

      assertThat(original.status()).isEqualTo(TaskStatus.TODO);
      assertThat(updated.status()).isEqualTo(TaskStatus.DONE);
    }
  }

  @Nested
  @DisplayName("Invalid transitions")
  class InvalidTransitions {

    @Test
    @DisplayName("should reject DONE → TODO")
    void should_throw_when_doneToTodo() {
      TaskItem task = new TaskItem("Task", TaskStatus.DONE);

      assertThatThrownBy(() -> task.transitionTo(TaskStatus.TODO))
          .isInstanceOf(InvalidTaskStatusTransitionException.class)
          .hasMessageContaining("DONE")
          .hasMessageContaining("TODO");
    }

    @Test
    @DisplayName("should reject BLOCKED → DONE")
    void should_throw_when_blockedToDone() {
      TaskItem task = new TaskItem("Task", TaskStatus.BLOCKED);

      assertThatThrownBy(() -> task.transitionTo(TaskStatus.DONE))
          .isInstanceOf(InvalidTaskStatusTransitionException.class)
          .hasMessageContaining("BLOCKED")
          .hasMessageContaining("DONE");
    }

    @Test
    @DisplayName("should reject TODO → BLOCKED")
    void should_throw_when_todoToBlocked() {
      TaskItem task = new TaskItem("Task", TaskStatus.TODO);

      assertThatThrownBy(() -> task.transitionTo(TaskStatus.BLOCKED))
          .isInstanceOf(InvalidTaskStatusTransitionException.class)
          .hasMessageContaining("TODO")
          .hasMessageContaining("BLOCKED");
    }

    @Test
    @DisplayName("should reject IN_PROGRESS → TODO")
    void should_throw_when_inProgressToTodo() {
      TaskItem task = new TaskItem("Task", TaskStatus.IN_PROGRESS);

      assertThatThrownBy(() -> task.transitionTo(TaskStatus.TODO))
          .isInstanceOf(InvalidTaskStatusTransitionException.class)
          .hasMessageContaining("IN_PROGRESS")
          .hasMessageContaining("TODO");
    }

    @Test
    @DisplayName("should provide from and to statuses in exception")
    void should_provideStatusesInException_when_invalidTransition() {
      TaskItem task = new TaskItem("Task", TaskStatus.DONE);

      try {
        task.transitionTo(TaskStatus.IN_PROGRESS);
      } catch (InvalidTaskStatusTransitionException e) {
        assertThat(e.getFrom()).isEqualTo(TaskStatus.DONE);
        assertThat(e.getTo()).isEqualTo(TaskStatus.IN_PROGRESS);
      }
    }
  }

  @Nested
  @DisplayName("Round-trip (parse → render)")
  class RoundTrip {

    @Test
    @DisplayName("should roundtrip TODO task")
    void should_roundtrip_when_todoTask() {
      String original = "- [ ] Create user module";
      TaskItem parsed = TaskMarkdownParser.parseLine(original);
      String rendered = TaskMarkdownRenderer.render(parsed);

      assertThat(rendered).isEqualTo(original);
    }

    @Test
    @DisplayName("should roundtrip DONE task")
    void should_roundtrip_when_doneTask() {
      String original = "- [x] Create user module";
      TaskItem parsed = TaskMarkdownParser.parseLine(original);
      String rendered = TaskMarkdownRenderer.render(parsed);

      assertThat(rendered).isEqualTo(original);
    }

    @Test
    @DisplayName("should roundtrip IN_PROGRESS task")
    void should_roundtrip_when_inProgressTask() {
      String original = "- [ ] [in_progress] Create user module";
      TaskItem parsed = TaskMarkdownParser.parseLine(original);
      String rendered = TaskMarkdownRenderer.render(parsed);

      assertThat(rendered).isEqualTo(original);
    }

    @Test
    @DisplayName("should roundtrip BLOCKED task")
    void should_roundtrip_when_blockedTask() {
      String original = "- [ ] [blocked] Create user module";
      TaskItem parsed = TaskMarkdownParser.parseLine(original);
      String rendered = TaskMarkdownRenderer.render(parsed);

      assertThat(rendered).isEqualTo(original);
    }
  }
}
