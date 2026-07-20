package com.atlasops.operations.domain.ports;

import com.atlasops.operations.domain.ProjectionStatus;
import java.util.List;
import java.util.Optional;

/**
 * Repository port for projection status records.
 *
 * <p>Validates: P2.7 — Projection health registry
 */
public interface ProjectionRepository {

    /**
     * Persists or updates a projection status record.
     *
     * @param projection the projection status to save
     * @return the persisted projection status
     */
    ProjectionStatus save(ProjectionStatus projection);

    /**
     * Finds the status of a specific projection by its name.
     *
     * @param name the projection name (e.g., "search-index", "vector-index")
     * @return the projection status, or empty if not found
     */
    Optional<ProjectionStatus> findByName(String name);

    /**
     * Returns all known projection statuses.
     *
     * @return list of all projection statuses
     */
    List<ProjectionStatus> findAll();
}
