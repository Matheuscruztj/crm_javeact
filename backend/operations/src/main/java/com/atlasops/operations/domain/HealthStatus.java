package com.atlasops.operations.domain;

import java.util.Map;

/**
 * Represents the overall system health status.
 *
 * @param status the aggregate health status (e.g., UP, DOWN, DEGRADED)
 * @param components a map of component names to their individual health statuses
 * @param timestamp the ISO-8601 timestamp of when the health check was performed
 */
public record HealthStatus(String status, Map<String, String> components, String timestamp) {}
