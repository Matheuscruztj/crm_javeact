package com.atlasops.shared.harness;

/**
 * Context information needed to provision a sandbox for a task.
 *
 * @param issue the issue identifier (e.g., "ATLAS-42")
 * @param role the agent role executing the task
 */
public record TaskContext(String issue, AgentRole role) {}
