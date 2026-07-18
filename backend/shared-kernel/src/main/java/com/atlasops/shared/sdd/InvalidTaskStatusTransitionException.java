package com.atlasops.shared.sdd;

/** Thrown when an invalid task status transition is attempted. */
public class InvalidTaskStatusTransitionException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  private final TaskStatus from;
  private final TaskStatus to;

  public InvalidTaskStatusTransitionException(TaskStatus from, TaskStatus to) {
    super("Invalid task status transition: " + from + " → " + to);
    this.from = from;
    this.to = to;
  }

  public TaskStatus getFrom() {
    return from;
  }

  public TaskStatus getTo() {
    return to;
  }
}
