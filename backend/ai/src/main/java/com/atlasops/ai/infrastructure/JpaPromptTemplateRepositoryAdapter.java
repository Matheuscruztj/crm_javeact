package com.atlasops.ai.infrastructure;

import com.atlasops.ai.domain.PromptTemplate;
import com.atlasops.ai.domain.ports.PromptTemplateRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA-based implementation of {@link PromptTemplateRepository}.
 * When saving an active template, deactivates all previous active versions for the same name.
 *
 * <p>Validates: P0.F.2 — Prompt Version Registry
 */
@Component
public class JpaPromptTemplateRepositoryAdapter implements PromptTemplateRepository {

    private final SpringDataPromptVersionRepository springDataRepository;

    public JpaPromptTemplateRepositoryAdapter(
            SpringDataPromptVersionRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public Optional<PromptTemplate> findActiveByName(String name) {
        return springDataRepository.findByNameAndActiveTrue(name).map(this::toDomain);
    }

    @Override
    @Transactional
    public PromptTemplate save(PromptTemplate template) {
        if (template.isActive()) {
            springDataRepository.deactivateAllByName(template.getName());
        }
        PromptVersionJpaEntity entity = toEntity(template);
        springDataRepository.save(entity);
        return template;
    }

    @Override
    public List<PromptTemplate> findAllByName(String name) {
        return springDataRepository.findByNameOrderByVersionDesc(name)
                .stream().map(this::toDomain).toList();
    }

    private PromptTemplate toDomain(PromptVersionJpaEntity e) {
        return new PromptTemplate(
                e.getId(),
                e.getName(),
                parseVersion(e.getVersion()),
                e.getTemplate(),
                e.isActive(),
                e.getCreatedAt());
    }

    private PromptVersionJpaEntity toEntity(PromptTemplate t) {
        return new PromptVersionJpaEntity(
                t.getId(),
                "global",         // tenant_id — global templates are not tenant-scoped by default
                t.getName(),
                "v" + t.getVersion(),
                null,             // tag
                t.getContent(),
                t.isActive(),
                100,              // ab_weight default
                "system",         // created_by
                t.getCreatedAt());
    }

    private int parseVersion(String versionStr) {
        try {
            return Integer.parseInt(versionStr.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return 1;
        }
    }
}
