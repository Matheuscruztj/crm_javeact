package com.atlasops.shared.harness;

/** Result of an action enforcement check. */
public enum EnforcementResult {

  /** The action is allowed to proceed. */
  ALLOWED,

  /** The action is blocked by security policy. */
  BLOCKED
}
