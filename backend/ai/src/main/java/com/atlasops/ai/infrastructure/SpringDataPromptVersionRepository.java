package com.atlasops.ai.infrastructure;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Data JPA repository for prompt version entities.
 */
public interface SpringDataPromptVersionRepository
        extends JpaRepository<PromptVersionJpaEntity, String> {

    Optional<PromptVersionJpaEntity> findByNameAndActiveTrue(String name);

    List<PromptVersionJpaEntity> findByNameOrderByVersionDesc(String name);

    @Modifying
    @Transactional
    @Query("UPDATE PromptVersionJpaEntity p SET p.active = false "
            + "WHERE p.name = :name AND p.active = true")
    void deactivateAllByName(@Param("name") String name);
}
