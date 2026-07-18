package com.atlasops.shared.sdd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("TaskMarkdownParser")
class TaskMarkdownParserTest {

  @Nested
  @DisplayName("parseLine")
  class ParseLine {

    @Test
    @DisplayName("should parse TODO task")
    void should_parseTodo_when_uncheckedCheckbox() {
      TaskItem task = TaskMarkdownParser.parseLine("- [ ] Implement feature");

      assertThat(task.status()).isEqualTo(TaskStatus.TODO);
      assertThat(task.description()).isEqualTo("Implement feature");
    }

    @Test
    @DisplayName("should parse DONE task")
    void should_parseDone_when_checkedCheckbox() {
      TaskItem task = TaskMarkdownParser.parseLine("- [x] Implement feature");

      assertThat(task.status()).isEqualTo(TaskStatus.DONE);
      assertThat(task.description()).isEqualTo("Implement feature");
    }

    @Test
    @DisplayName("should parse IN_PROGRESS task")
    void should_parseInProgress_when_inProgressMarker() {
      TaskItem task = TaskMarkdownParser.parseLine("- [ ] [in_progress] Implement feature");

      assertThat(task.status()).isEqualTo(TaskStatus.IN_PROGRESS);
      assertThat(task.description()).isEqualTo("Implement feature");
    }

    @Test
    @DisplayName("should parse BLOCKED task")
    void should_parseBlocked_when_blockedMarker() {
      TaskItem task = TaskMarkdownParser.parseLine("- [ ] [blocked] Implement feature");

      assertThat(task.status()).isEqualTo(TaskStatus.BLOCKED);
      assertThat(task.description()).isEqualTo("Implement feature");
    }

    @Test
    @DisplayName("should handle leading whitespace (indented tasks)")
    void should_parse_when_lineHasLeadingWhitespace() {
      TaskItem task = TaskMarkdownParser.parseLine("  - [ ] Sub-task");

      assertThat(task.status()).isEqualTo(TaskStatus.TODO);
      assertThat(task.description()).isEqualTo("Sub-task");
    }

    @Test
    @DisplayName("should throw on null input")
    void should_throw_when_lineIsNull() {
      assertThatThrownBy(() -> TaskMarkdownParser.parseLine(null))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("must not be null or blank");
    }

    @Test
    @DisplayName("should throw on blank input")
    void should_throw_when_lineIsBlank() {
      assertThatThrownBy(() -> TaskMarkdownParser.parseLine("   "))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("must not be null or blank");
    }

    @Test
    @DisplayName("should throw on non-task line")
    void should_throw_when_lineIsNotTaskFormat() {
      assertThatThrownBy(() -> TaskMarkdownParser.parseLine("# Header"))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("does not match task format");
    }
  }

  @Nested
  @DisplayName("parseAll")
  class ParseAll {

    @Test
    @DisplayName("should parse multiple tasks from markdown content")
    void should_parseMultipleTasks_when_validMarkdown() {
      String content =
          """
                    # Tasks

                    - [ ] First task
                    - [x] Second task
                    - [ ] [in_progress] Third task
                    - [ ] [blocked] Fourth task
                    """;

      List<TaskItem> tasks = TaskMarkdownParser.parseAll(content);

      assertThat(tasks).hasSize(4);
      assertThat(tasks.get(0).status()).isEqualTo(TaskStatus.TODO);
      assertThat(tasks.get(0).description()).isEqualTo("First task");
      assertThat(tasks.get(1).status()).isEqualTo(TaskStatus.DONE);
      assertThat(tasks.get(2).status()).isEqualTo(TaskStatus.IN_PROGRESS);
      assertThat(tasks.get(3).status()).isEqualTo(TaskStatus.BLOCKED);
    }

    @Test
    @DisplayName("should skip non-task lines")
    void should_skipNonTaskLines_when_mixedContent() {
      String content =
          """
                    # Implementation Plan

                    Some description text.

                    - [ ] A task
                    - This is not a task
                    - [x] Another task
                    """;

      List<TaskItem> tasks = TaskMarkdownParser.parseAll(content);

      assertThat(tasks).hasSize(2);
      assertThat(tasks.get(0).description()).isEqualTo("A task");
      assertThat(tasks.get(1).description()).isEqualTo("Another task");
    }

    @Test
    @DisplayName("should return empty list for null input")
    void should_returnEmpty_when_inputIsNull() {
      assertThat(TaskMarkdownParser.parseAll(null)).isEmpty();
    }

    @Test
    @DisplayName("should return empty list for blank input")
    void should_returnEmpty_when_inputIsBlank() {
      assertThat(TaskMarkdownParser.parseAll("  ")).isEmpty();
    }
  }
}
