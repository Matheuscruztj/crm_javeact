package com.atlasops.shared.sdd;

import java.util.List;
import java.util.Objects;

/**
 * Represents a single task item parsed from a tasks.md file.
 *
 * @param description the task description text
 * @param status the current status of the task
 * @param children optional nested sub-tasks
 */
public record TaskItem(String description, TaskStatus status, List<TaskItem> children) {

  public TaskItem {
    Objects.requireNonNull(description, "description must not be null");
    Objects.requireNonNull(status, "status must not be null");
    if (children == null) {
      children = List.of();
    } else {
      children = List.copyOf(children);
    }
  }

  /** Creates a TaskItem with no children. */
  public TaskItem(String description, TaskStatus status) {
    this(description, status, List.of());
  }

  /**
   * Transitions this task to the given target status, returning a new TaskItem.
   *
   * @param target the desired new status
   * @return a new TaskItem with the updated status
   * @throws InvalidTaskStatusTransitionException if the transition is not valid
   */
  public TaskItem transitionTo(TaskStatus target) {
    if (!status.canTransitionTo(target)) {
      throw new InvalidTaskStatusTransitionException(status, target);
    }
    return new TaskItem(description, target, children);
  }
}
