package com.atlasops.shared.harness;

/** Lifecycle status of an agent task. */
public enum AgentTaskStatus {
  PENDING,
  IN_PROGRESS,
  DONE,
  BLOCKED,
  REJECTED
}
