package com.atlasops.shared.sdd;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Renders {@link TaskItem} instances back to markdown format.
 *
 * <p>Output format:
 *
 * <ul>
 *   <li>TODO: {@code - [ ] Description}
 *   <li>DONE: {@code - [x] Description}
 *   <li>IN_PROGRESS: {@code - [ ] [in_progress] Description}
 *   <li>BLOCKED: {@code - [ ] [blocked] Description}
 * </ul>
 */
public final class TaskMarkdownRenderer {

  private TaskMarkdownRenderer() {
    // Utility class
  }

  /**
   * Renders a single TaskItem to its markdown representation.
   *
   * @param task the task item to render
   * @return the markdown string
   */
  public static String render(TaskItem task) {
    if (task == null) {
      throw new IllegalArgumentException("TaskItem must not be null");
    }

    return switch (task.status()) {
      case TODO -> "- [ ] " + task.description();
      case DONE -> "- [x] " + task.description();
      case IN_PROGRESS -> "- [ ] [in_progress] " + task.description();
      case BLOCKED -> "- [ ] [blocked] " + task.description();
    };
  }

  /**
   * Renders a list of TaskItems to a markdown string, one line per task.
   *
   * @param tasks the list of task items
   * @return the markdown content
   */
  public static String renderAll(List<TaskItem> tasks) {
    if (tasks == null || tasks.isEmpty()) {
      return "";
    }

    return tasks.stream().map(TaskMarkdownRenderer::render).collect(Collectors.joining("\n"));
  }
}
