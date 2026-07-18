package com.atlasops.ai.domain;

/**
 * Enum representing the types of mutable actions that require human approval. Any AI analysis
 * proposing one of these actions must go through the approval workflow.
 *
 * <p>Validates: Requirements 4.10
 */
public enum ActionType {
  CREATE,
  UPDATE,
  DELETE;

  /**
   * Checks whether this action type is mutable (all values are mutable by definition).
   *
   * @return true always, since all ActionType values represent mutable operations
   */
  public boolean isMutable() {
    return true;
  }
}
