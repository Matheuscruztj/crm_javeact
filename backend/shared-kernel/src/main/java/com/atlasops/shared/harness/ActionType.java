package com.atlasops.shared.harness;

/** Enumerates the types of actions that can be attempted by an agent. */
public enum ActionType {

  /** Shell or system command execution. */
  COMMAND,

  /** Git merge operation. */
  MERGE,

  /** Access to secrets or credentials. */
  SECRET_ACCESS,

  /** Database operation (DDL or DML). */
  DB_OPERATION
}
