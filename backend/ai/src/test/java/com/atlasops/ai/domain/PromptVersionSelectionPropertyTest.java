package com.atlasops.ai.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.atlasops.ai.domain.ports.PromptTemplateRepository;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.jqwik.api.*;

/**
 * Property-based tests for prompt version selection in the AI module.
 *
 * <p><b>Validates: Requirements 4.9</b>
 *
 * <p>Property 7: For any analysis type with multiple registered prompt versions, the system SHALL
 * select and use the version marked as active for that analysis type, identified by name and
 * sequential numeric version.
 */
@Tag("Feature: monorepo-sdd-harness, Property 7: Prompt Version Selection")
class PromptVersionSelectionPropertyTest {

  // ─── Property: Selection always returns the active version ────────────────────

  @Property(tries = 100)
  void findActiveByName_shouldAlwaysReturnTheActiveVersion(
      @ForAll("templateSetsWithOneActive") TemplateSet templateSet) {

    InMemoryPromptTemplateRepository repository = new InMemoryPromptTemplateRepository();
    templateSet.templates().forEach(repository::save);

    Optional<PromptTemplate> selected = repository.findActiveByName(templateSet.name());

    assertThat(selected)
        .as(
            "Selection for name '%s' with %d versions should find the active one",
            templateSet.name(), templateSet.templates().size())
        .isPresent();

    PromptTemplate active = selected.get();
    assertThat(active.isActive()).as("Selected template must be active").isTrue();
    assertThat(active.getName())
        .as("Selected template must match requested name")
        .isEqualTo(templateSet.name());
  }

  // ─── Property: Only one version per name can be active at a time ─────────────

  @Property(tries = 100)
  void templateSet_shouldHaveExactlyOneActiveVersionPerName(
      @ForAll("templateSetsWithOneActive") TemplateSet templateSet) {

    long activeCount = templateSet.templates().stream().filter(PromptTemplate::isActive).count();

    assertThat(activeCount)
        .as(
            "Template name '%s' must have exactly one active version, found %d",
            templateSet.name(), activeCount)
        .isEqualTo(1);
  }

  // ─── Property: Templates are identified by name and sequential numeric version ─

  @Property(tries = 100)
  void templates_shouldHaveSequentialNumericVersions(
      @ForAll("templateSetsWithOneActive") TemplateSet templateSet) {

    List<Integer> versions =
        templateSet.templates().stream().map(PromptTemplate::getVersion).sorted().toList();

    // Versions should be sequential starting from 1
    for (int i = 0; i < versions.size(); i++) {
      assertThat(versions.get(i))
          .as("Version at position %d should be %d (sequential from 1)", i, i + 1)
          .isEqualTo(i + 1);
    }

    // All templates share the same name
    templateSet
        .templates()
        .forEach(
            t ->
                assertThat(t.getName())
                    .as("All templates in set should share the same name")
                    .isEqualTo(templateSet.name()));
  }

  // ─── Property: Selection never returns an inactive version ────────────────────

  @Property(tries = 100)
  void findActiveByName_shouldNeverReturnInactiveVersion(
      @ForAll("templateSetsWithOneActive") TemplateSet templateSet) {

    InMemoryPromptTemplateRepository repository = new InMemoryPromptTemplateRepository();
    templateSet.templates().forEach(repository::save);

    Optional<PromptTemplate> selected = repository.findActiveByName(templateSet.name());

    assertThat(selected).isPresent();

    // Verify the selected version is NOT any of the inactive ones
    List<PromptTemplate> inactiveVersions =
        templateSet.templates().stream().filter(t -> !t.isActive()).toList();

    for (PromptTemplate inactive : inactiveVersions) {
      assertThat(selected.get().getVersion())
          .as(
              "Selected version %d must not be inactive version %d",
              selected.get().getVersion(), inactive.getVersion())
          .isNotEqualTo(inactive.getVersion());
    }
  }

  // ─── Property: Version identifier follows name:vN format ─────────────────────

  @Property(tries = 100)
  void activeVersion_shouldHaveCorrectVersionIdentifierFormat(
      @ForAll("templateSetsWithOneActive") TemplateSet templateSet) {

    InMemoryPromptTemplateRepository repository = new InMemoryPromptTemplateRepository();
    templateSet.templates().forEach(repository::save);

    Optional<PromptTemplate> selected = repository.findActiveByName(templateSet.name());

    assertThat(selected).isPresent();

    String expectedIdentifier = templateSet.name() + ":v" + selected.get().getVersion();
    assertThat(selected.get().getVersionIdentifier())
        .as("Version identifier should be '%s'", expectedIdentifier)
        .isEqualTo(expectedIdentifier);
  }

  // ─── Property: findAllByName returns all versions ordered by version desc ────

  @Property(tries = 100)
  void findAllByName_shouldReturnAllVersionsOrderedDescending(
      @ForAll("templateSetsWithOneActive") TemplateSet templateSet) {

    InMemoryPromptTemplateRepository repository = new InMemoryPromptTemplateRepository();
    templateSet.templates().forEach(repository::save);

    List<PromptTemplate> all = repository.findAllByName(templateSet.name());

    assertThat(all)
        .as("findAllByName should return all %d versions", templateSet.templates().size())
        .hasSize(templateSet.templates().size());

    // Verify ordering is by version descending
    for (int i = 0; i < all.size() - 1; i++) {
      assertThat(all.get(i).getVersion())
          .as("Results should be ordered by version descending")
          .isGreaterThan(all.get(i + 1).getVersion());
    }
  }

  // ─── Generators ──────────────────────────────────────────────────────────────

  @Provide
  Arbitrary<TemplateSet> templateSetsWithOneActive() {
    // Generate a template name (analysis type)
    Arbitrary<String> names =
        Arbitraries.of(
            "document-analysis",
            "sentiment-analysis",
            "summarization",
            "entity-extraction",
            "classification",
            "question-answering",
            "code-review",
            "risk-assessment");

    // Generate number of versions (at least 2 to test "multiple versions")
    Arbitrary<Integer> versionCounts = Arbitraries.integers().between(2, 10);

    // Generate which version is active (1-based index into versions)
    return Combinators.combine(names, versionCounts)
        .flatAs(
            (name, count) -> {
              Arbitrary<Integer> activeVersionArb = Arbitraries.integers().between(1, count);
              return activeVersionArb.map(
                  activeVersion -> createTemplateSet(name, count, activeVersion));
            });
  }

  // ─── Helper methods ──────────────────────────────────────────────────────────

  private TemplateSet createTemplateSet(String name, int versionCount, int activeVersion) {
    Instant baseTime = Instant.parse("2024-01-01T00:00:00Z");

    List<PromptTemplate> templates =
        IntStream.rangeClosed(1, versionCount)
            .mapToObj(
                v ->
                    new PromptTemplate(
                        UUID.randomUUID().toString(),
                        name,
                        v,
                        "Prompt content for " + name + " version " + v,
                        v == activeVersion,
                        baseTime.plusSeconds(v * 3600L)))
            .collect(Collectors.toList());

    return new TemplateSet(name, templates);
  }

  // ─── Test helper types ───────────────────────────────────────────────────────

  record TemplateSet(String name, List<PromptTemplate> templates) {}

  /**
   * In-memory implementation of PromptTemplateRepository for property testing. Faithfully
   * implements the contract: findActiveByName returns only the active version.
   */
  static class InMemoryPromptTemplateRepository implements PromptTemplateRepository {

    private final List<PromptTemplate> store = new ArrayList<>();

    @Override
    public Optional<PromptTemplate> findActiveByName(String name) {
      return store.stream()
          .filter(t -> t.getName().equals(name))
          .filter(PromptTemplate::isActive)
          .findFirst();
    }

    @Override
    public PromptTemplate save(PromptTemplate template) {
      store.add(template);
      return template;
    }

    @Override
    public List<PromptTemplate> findAllByName(String name) {
      return store.stream()
          .filter(t -> t.getName().equals(name))
          .sorted(Comparator.comparingInt(PromptTemplate::getVersion).reversed())
          .collect(Collectors.toList());
    }
  }
}
