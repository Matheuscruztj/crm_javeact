package com.atlasops.ai.domain.ports;

import com.atlasops.ai.domain.PromptTemplate;
import java.util.List;
import java.util.Optional;

/**
 * Port interface for managing prompt template versions. Supports finding the active version by
 * name, saving new versions, and listing all versions for a given template name.
 *
 * <p>Validates: Requirements 4.9
 */
public interface PromptTemplateRepository {

  /**
   * Finds the active prompt template for a given name (analysis type).
   *
   * @param name the template name (e.g., "document-analysis")
   * @return the active template if found, empty otherwise
   */
  Optional<PromptTemplate> findActiveByName(String name);

  /**
   * Persists a prompt template (new version or update).
   *
   * @param template the prompt template to save
   * @return the saved template
   */
  PromptTemplate save(PromptTemplate template);

  /**
   * Lists all versions of a prompt template by name, ordered by version descending.
   *
   * @param name the template name
   * @return all versions of the template
   */
  List<PromptTemplate> findAllByName(String name);
}
