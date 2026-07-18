package com.atlasops.shared.sdd;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses task lines from a tasks.md markdown file into {@link TaskItem} instances.
 *
 * <p>Supported formats:
 *
 * <ul>
 *   <li>{@code - [ ] Description} → TODO
 *   <li>{@code - [x] Description} → DONE
 *   <li>{@code - [ ] [in_progress] Description} → IN_PROGRESS
 *   <li>{@code - [ ] [blocked] Description} → BLOCKED
 * </ul>
 */
public final class TaskMarkdownParser {

  /**
   * Pattern to match a task line. Group 1: checkbox content ('x' or ' ') Group 2: optional status
   * marker (e.g., "[in_progress]" or "[blocked]") Group 3: task description
   */
  private static final Pattern TASK_LINE_PATTERN =
      Pattern.compile("^\\s*- \\[([ x])\\]\\s*(?:\\[(in_progress|blocked)\\]\\s*)?(.+)$");

  private TaskMarkdownParser() {
    // Utility class
  }

  /**
   * Parses a single task line into a {@link TaskItem}.
   *
   * @param line the markdown line to parse
   * @return the parsed TaskItem
   * @throws IllegalArgumentException if the line does not match expected task format
   */
  public static TaskItem parseLine(String line) {
    if (line == null || line.isBlank()) {
      throw new IllegalArgumentException("Task line must not be null or blank");
    }

    Matcher matcher = TASK_LINE_PATTERN.matcher(line);
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Line does not match task format: " + line);
    }

    String checkbox = matcher.group(1);
    String statusMarker = matcher.group(2);
    String description = matcher.group(3).trim();

    TaskStatus status = resolveStatus(checkbox, statusMarker);
    return new TaskItem(description, status);
  }

  /**
   * Parses multiple lines of markdown into a list of {@link TaskItem} instances. Lines that don't
   * match the task format are skipped.
   *
   * @param markdownContent the full markdown content
   * @return list of parsed task items
   */
  public static List<TaskItem> parseAll(String markdownContent) {
    if (markdownContent == null || markdownContent.isBlank()) {
      return List.of();
    }

    List<TaskItem> tasks = new ArrayList<>();
    String[] lines = markdownContent.split("\\R");

    for (String line : lines) {
      Matcher matcher = TASK_LINE_PATTERN.matcher(line);
      if (matcher.matches()) {
        String checkbox = matcher.group(1);
        String statusMarker = matcher.group(2);
        String description = matcher.group(3).trim();

        TaskStatus status = resolveStatus(checkbox, statusMarker);
        tasks.add(new TaskItem(description, status));
      }
    }

    return tasks;
  }

  private static TaskStatus resolveStatus(String checkbox, String statusMarker) {
    if ("x".equals(checkbox)) {
      return TaskStatus.DONE;
    }

    if (statusMarker != null) {
      return switch (statusMarker) {
        case "in_progress" -> TaskStatus.IN_PROGRESS;
        case "blocked" -> TaskStatus.BLOCKED;
        default -> TaskStatus.TODO;
      };
    }

    return TaskStatus.TODO;
  }
}
