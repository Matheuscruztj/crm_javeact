package com.atlasops.operations.application;

import com.atlasops.operations.domain.ProjectionStatus;
import com.atlasops.operations.domain.ports.ProjectionRepository;
import java.util.List;
import java.util.Objects;

/**
 * Use case that returns the current status of all known projections.
 *
 * <p>Validates: P2.7 — Projection health registry
 */
public class GetProjectionsUseCase {

    private final ProjectionRepository projectionRepository;

    public GetProjectionsUseCase(ProjectionRepository projectionRepository) {
        this.projectionRepository = Objects.requireNonNull(
                projectionRepository, "ProjectionRepository must not be null");
    }

    /**
     * Returns a list of all projection statuses ordered alphabetically by name.
     *
     * @return list of all known projection statuses
     */
    public List<ProjectionStatus> execute() {
        return projectionRepository.findAll().stream()
                .sorted((a, b) -> a.getName().compareTo(b.getName()))
                .toList();
    }
}
