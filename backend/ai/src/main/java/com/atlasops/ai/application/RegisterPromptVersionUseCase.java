package com.atlasops.ai.application;

import com.atlasops.ai.domain.PromptTemplate;
import com.atlasops.ai.domain.ports.PromptTemplateRepository;
import com.atlasops.shared.domain.ports.Clock;
import com.atlasops.shared.domain.ports.IdGenerator;
import java.util.List;
import java.util.Objects;

/**
 * Use case for registering a new prompt version.
 *
 * <p>Automatically increments the version number based on existing versions for the given name.
 * If the new version is marked active, any existing active version for the same name is
 * deactivated.
 *
 * <p>Validates: P0.F.2 — Prompt Version Registry
 */
public class RegisterPromptVersionUseCase {

    private final PromptTemplateRepository repository;
    private final IdGenerator idGenerator;
    private final Clock clock;

    public RegisterPromptVersionUseCase(
            PromptTemplateRepository repository,
            IdGenerator idGenerator,
            Clock clock) {
        this.repository = Objects.requireNonNull(repository);
        this.idGenerator = Objects.requireNonNull(idGenerator);
        this.clock = Objects.requireNonNull(clock);
    }

    /**
     * Registers a new prompt version.
     *
     * @param name    the template name (e.g., "document-analysis")
     * @param content the prompt template content
     * @param active  whether this version should become the active one
     * @return the registered prompt template
     */
    public PromptTemplate execute(String name, String content, boolean active) {
        Objects.requireNonNull(name, "Name must not be null");
        Objects.requireNonNull(content, "Content must not be null");

        List<PromptTemplate> existing = repository.findAllByName(name);
        int nextVersion = existing.stream()
                .mapToInt(PromptTemplate::getVersion)
                .max()
                .orElse(0) + 1;

        PromptTemplate template = new PromptTemplate(
                idGenerator.generate(),
                name,
                nextVersion,
                content,
                active,
                clock.now());

        return repository.save(template);
    }
}
