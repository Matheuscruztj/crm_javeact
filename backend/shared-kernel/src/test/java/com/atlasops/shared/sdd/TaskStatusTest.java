package com.atlasops.shared.sdd;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("TaskStatus")
class TaskStatusTest {

  @Nested
  @DisplayName("Valid transitions")
  class ValidTransitions {

    @Test
    @DisplayName("should allow TODO → IN_PROGRESS")
    void should_allowTransition_when_todoToInProgress() {
      assertThat(TaskStatus.TODO.canTransitionTo(TaskStatus.IN_PROGRESS)).isTrue();
    }

    @Test
    @DisplayName("should allow TODO → DONE")
    void should_allowTransition_when_todoToDone() {
      assertThat(TaskStatus.TODO.canTransitionTo(TaskStatus.DONE)).isTrue();
    }

    @Test
    @DisplayName("should allow IN_PROGRESS → DONE")
    void should_allowTransition_when_inProgressToDone() {
      assertThat(TaskStatus.IN_PROGRESS.canTransitionTo(TaskStatus.DONE)).isTrue();
    }

    @Test
    @DisplayName("should allow IN_PROGRESS → BLOCKED")
    void should_allowTransition_when_inProgressToBlocked() {
      assertThat(TaskStatus.IN_PROGRESS.canTransitionTo(TaskStatus.BLOCKED)).isTrue();
    }
  }

  @Nested
  @DisplayName("Invalid transitions")
  class InvalidTransitions {

    @Test
    @DisplayName("should reject DONE → TODO")
    void should_rejectTransition_when_doneToTodo() {
      assertThat(TaskStatus.DONE.canTransitionTo(TaskStatus.TODO)).isFalse();
    }

    @Test
    @DisplayName("should reject DONE → IN_PROGRESS")
    void should_rejectTransition_when_doneToInProgress() {
      assertThat(TaskStatus.DONE.canTransitionTo(TaskStatus.IN_PROGRESS)).isFalse();
    }

    @Test
    @DisplayName("should reject DONE → BLOCKED")
    void should_rejectTransition_when_doneToBlocked() {
      assertThat(TaskStatus.DONE.canTransitionTo(TaskStatus.BLOCKED)).isFalse();
    }

    @Test
    @DisplayName("should reject BLOCKED → TODO")
    void should_rejectTransition_when_blockedToTodo() {
      assertThat(TaskStatus.BLOCKED.canTransitionTo(TaskStatus.TODO)).isFalse();
    }

    @Test
    @DisplayName("should reject BLOCKED → DONE")
    void should_rejectTransition_when_blockedToDone() {
      assertThat(TaskStatus.BLOCKED.canTransitionTo(TaskStatus.DONE)).isFalse();
    }

    @Test
    @DisplayName("should reject BLOCKED → IN_PROGRESS")
    void should_rejectTransition_when_blockedToInProgress() {
      assertThat(TaskStatus.BLOCKED.canTransitionTo(TaskStatus.IN_PROGRESS)).isFalse();
    }

    @Test
    @DisplayName("should reject TODO → BLOCKED")
    void should_rejectTransition_when_todoToBlocked() {
      assertThat(TaskStatus.TODO.canTransitionTo(TaskStatus.BLOCKED)).isFalse();
    }

    @Test
    @DisplayName("should reject IN_PROGRESS → TODO")
    void should_rejectTransition_when_inProgressToTodo() {
      assertThat(TaskStatus.IN_PROGRESS.canTransitionTo(TaskStatus.TODO)).isFalse();
    }
  }

  @Test
  @DisplayName("should have 4 status values")
  void should_haveFourValues() {
    assertThat(TaskStatus.values()).hasSize(4);
  }
}
