package com.atlasops.shared.harness;

import java.util.List;

/**
 * Represents a task assigned to an agent in the harness workflow.
 *
 * <p>Required fields:
 *
 * <ul>
 *   <li>{@code id} — unique task identifier
 *   <li>{@code objective} — description, max 500 characters
 *   <li>{@code context} — list of modules/files affected
 *   <li>{@code outOfScope} — explicit boundaries
 *   <li>{@code acceptanceCriteria} — at least 1 verifiable criterion
 *   <li>{@code affectedInterfaces} — list of contracts or "nenhuma"
 *   <li>{@code risks} — list of risks or "nenhum identificado"
 *   <li>{@code requiredTests} — at least 1 test command
 *   <li>{@code validationCommands} — at least 1 executable command
 *   <li>{@code assignedAgent} — the agent role assigned
 *   <li>{@code status} — current lifecycle status
 * </ul>
 *
 * @param id unique task identifier
 * @param objective description (max 500 chars)
 * @param context modules and files affected
 * @param outOfScope explicit out-of-scope items
 * @param acceptanceCriteria verifiable acceptance criteria (min 1)
 * @param affectedInterfaces affected contracts or "nenhuma"
 * @param risks identified risks or "nenhum identificado"
 * @param requiredTests required test commands (min 1)
 * @param validationCommands validation commands (min 1)
 * @param assignedAgent the assigned agent role
 * @param status current task status
 */
public record AgentTask(
    String id,
    String objective,
    List<String> context,
    List<String> outOfScope,
    List<String> acceptanceCriteria,
    List<String> affectedInterfaces,
    List<String> risks,
    List<String> requiredTests,
    List<String> validationCommands,
    AgentRole assignedAgent,
    AgentTaskStatus status) {

  /** Maximum allowed length for the objective field. */
  public static final int MAX_OBJECTIVE_LENGTH = 500;
}
