package com.atlasops.shared.sdd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("TaskMarkdownRenderer")
class TaskMarkdownRendererTest {

  @Nested
  @DisplayName("render")
  class Render {

    @Test
    @DisplayName("should render TODO task")
    void should_renderTodoFormat_when_statusIsTodo() {
      TaskItem task = new TaskItem("Implement feature", TaskStatus.TODO);

      String result = TaskMarkdownRenderer.render(task);

      assertThat(result).isEqualTo("- [ ] Implement feature");
    }

    @Test
    @DisplayName("should render DONE task")
    void should_renderDoneFormat_when_statusIsDone() {
      TaskItem task = new TaskItem("Implement feature", TaskStatus.DONE);

      String result = TaskMarkdownRenderer.render(task);

      assertThat(result).isEqualTo("- [x] Implement feature");
    }

    @Test
    @DisplayName("should render IN_PROGRESS task")
    void should_renderInProgressFormat_when_statusIsInProgress() {
      TaskItem task = new TaskItem("Implement feature", TaskStatus.IN_PROGRESS);

      String result = TaskMarkdownRenderer.render(task);

      assertThat(result).isEqualTo("- [ ] [in_progress] Implement feature");
    }

    @Test
    @DisplayName("should render BLOCKED task")
    void should_renderBlockedFormat_when_statusIsBlocked() {
      TaskItem task = new TaskItem("Implement feature", TaskStatus.BLOCKED);

      String result = TaskMarkdownRenderer.render(task);

      assertThat(result).isEqualTo("- [ ] [blocked] Implement feature");
    }

    @Test
    @DisplayName("should throw on null task")
    void should_throw_when_taskIsNull() {
      assertThatThrownBy(() -> TaskMarkdownRenderer.render(null))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("must not be null");
    }
  }

  @Nested
  @DisplayName("renderAll")
  class RenderAll {

    @Test
    @DisplayName("should render multiple tasks")
    void should_renderMultipleTasks_when_listProvided() {
      List<TaskItem> tasks =
          List.of(
              new TaskItem("First", TaskStatus.TODO),
              new TaskItem("Second", TaskStatus.DONE),
              new TaskItem("Third", TaskStatus.IN_PROGRESS),
              new TaskItem("Fourth", TaskStatus.BLOCKED));

      String result = TaskMarkdownRenderer.renderAll(tasks);

      assertThat(result)
          .isEqualTo(
              "- [ ] First\n"
                  + "- [x] Second\n"
                  + "- [ ] [in_progress] Third\n"
                  + "- [ ] [blocked] Fourth");
    }

    @Test
    @DisplayName("should return empty string for null list")
    void should_returnEmpty_when_listIsNull() {
      assertThat(TaskMarkdownRenderer.renderAll(null)).isEmpty();
    }

    @Test
    @DisplayName("should return empty string for empty list")
    void should_returnEmpty_when_listIsEmpty() {
      assertThat(TaskMarkdownRenderer.renderAll(List.of())).isEmpty();
    }
  }
}
