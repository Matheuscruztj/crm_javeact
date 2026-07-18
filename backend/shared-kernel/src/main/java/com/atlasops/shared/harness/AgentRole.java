package com.atlasops.shared.harness;

/**
 * Defines the agent roles in the harness engineering workflow. Each role has a unique code (A1-A11)
 * and a human-readable description.
 */
public enum AgentRole {
  A1("Planner"),
  A2("Implementer"),
  A3("Test_Engineer"),
  A4("Reviewer"),
  A5("Security_Agent"),
  A6("Architecture_Agent"),
  A7("Migration_Agent"),
  A8("Documentation_Agent"),
  A9("SRE_Agent"),
  A10("Quality_Janitor"),
  A11("AI_Evaluation_Agent");

  private final String description;

  AgentRole(String description) {
    this.description = description;
  }

  /** Returns the human-readable description of this agent role. */
  public String description() {
    return description;
  }

  /** Returns the role code (e.g., "A1", "A2", ..., "A11"). */
  public String code() {
    return name();
  }
}
