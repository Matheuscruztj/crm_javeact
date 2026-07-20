package com.atlasops.ai.application;

import com.atlasops.ai.domain.PromptTemplate;
import com.atlasops.ai.domain.ports.PromptTemplateRepository;
import com.atlasops.shared.domain.exceptions.ResourceNotFoundException;
import java.util.Objects;

/**
 * Use case for retrieving the active prompt template by name.
 *
 * <p>Validates: P0.F.2 — Prompt Version Registry
 */
public class GetActivePromptUseCase {

    private final PromptTemplateRepository repository;

    public GetActivePromptUseCase(PromptTemplateRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    /**
     * Retrieves the currently active prompt template for the given name.
     *
     * @param name the template name
     * @return the active template
     * @throws ResourceNotFoundException if no active template exists for the given name
     */
    public PromptTemplate execute(String name) {
        Objects.requireNonNull(name, "Name must not be null");
        return repository.findActiveByName(name)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No active prompt template found for name: " + name));
    }
}
