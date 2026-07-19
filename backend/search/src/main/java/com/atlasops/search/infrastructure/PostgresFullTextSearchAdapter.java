package com.atlasops.search.infrastructure;

import com.atlasops.search.domain.SearchQuery;
import com.atlasops.search.domain.SearchResult;
import com.atlasops.search.domain.ports.SearchIndexPort;
import com.atlasops.search.domain.ports.SearchIndexUpdatePort;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * PostgreSQL full-text search adapter implementing both SearchIndexPort and SearchIndexUpdatePort.
 * Uses tsvector/tsquery for keyword search with ts_rank for relevance scoring.
 *
 * <p>Validates: Requirements 18.1, 18.2, 18.3, 18.6
 */
@Component
public class PostgresFullTextSearchAdapter implements SearchIndexPort, SearchIndexUpdatePort {

  private static final Logger log = LoggerFactory.getLogger(PostgresFullTextSearchAdapter.class);
  private static final int SNIPPET_MAX_LENGTH = 200;

  private final SpringDataSearchIndexRepository repository;
  private final JdbcTemplate jdbcTemplate;

  public PostgresFullTextSearchAdapter(
      SpringDataSearchIndexRepository repository, JdbcTemplate jdbcTemplate) {
    this.repository = repository;
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public Page<SearchResult> search(SearchQuery query, Pageable pageable) {
    Page<Object[]> results;

    if (query.entityTypeFilter() != null && !query.entityTypeFilter().isBlank()) {
      results =
          repository.searchByQueryAndEntityType(
              query.query(), query.tenantId(), query.entityTypeFilter(), pageable);
    } else {
      results = repository.searchByQuery(query.query(), query.tenantId(), pageable);
    }

    return results.map(this::toSearchResult);
  }

  @Override
  @Transactional
  public void indexEntity(String entityType, String entityId, String content, String tenantId) {
    var existing =
        repository.findByEntityTypeAndEntityIdAndTenantId(entityType, entityId, tenantId);

    String title = extractTitle(content);
    String snippet = extractSnippet(content);

    if (existing.isPresent()) {
      updateExistingEntry(existing.get(), title, snippet, content);
    } else {
      insertNewEntry(entityType, entityId, tenantId, title, snippet, content);
    }
  }

  @Override
  @Transactional
  public void removeEntity(String entityType, String entityId, String tenantId) {
    repository.deleteByEntityTypeAndEntityIdAndTenantId(entityType, entityId, tenantId);
    log.debug(
        "Removed search index entry: type={}, id={}, tenant={}", entityType, entityId, tenantId);
  }

  private void updateExistingEntry(
      SearchIndexJpaEntity entity, String title, String snippet, String content) {
    jdbcTemplate.update(
        "UPDATE search_index SET title = ?, content_snippet = ?,"
            + " content_vector = to_tsvector('english', ?), updated_at = ? WHERE id = ?",
        title,
        snippet,
        content,
        Instant.now(),
        entity.getId());

    log.debug("Updated search index entry: id={}", entity.getId());
  }

  private void insertNewEntry(
      String entityType,
      String entityId,
      String tenantId,
      String title,
      String snippet,
      String content) {
    String id = UUID.randomUUID().toString();

    jdbcTemplate.update(
        "INSERT INTO search_index (id, tenant_id, entity_type, entity_id, title,"
            + " content_snippet, content_vector, updated_at) VALUES (?, ?, ?, ?, ?, ?,"
            + " to_tsvector('english', ?), ?)",
        id,
        tenantId,
        entityType,
        entityId,
        title,
        snippet,
        content,
        Instant.now());

    log.debug("Inserted search index entry: id={}, type={}, entityId={}", id, entityType, entityId);
  }

  private SearchResult toSearchResult(Object[] row) {
    // Native query returns columns in order:
    // id, tenant_id, entity_type, entity_id, title, content_snippet, updated_at,
    // content_vector, rank
    String entityType = (String) row[2];
    String entityId = (String) row[3];
    String title = (String) row[4];
    String snippet = row[5] != null ? (String) row[5] : "";
    double rank = row[row.length - 1] != null ? ((Number) row[row.length - 1]).doubleValue() : 0.0;

    // Normalize rank to 0.0-1.0 range (ts_rank typically returns values 0-1 for
    // simple queries, but can exceed 1 for some configurations)
    double normalizedScore = Math.min(1.0, Math.max(0.0, rank));

    // Truncate snippet if needed
    String truncatedSnippet =
        snippet.length() > SNIPPET_MAX_LENGTH ? snippet.substring(0, SNIPPET_MAX_LENGTH) : snippet;

    return new SearchResult(entityType, entityId, title, truncatedSnippet, normalizedScore);
  }

  private String extractTitle(String content) {
    if (content == null || content.isBlank()) {
      return "Untitled";
    }
    // Use the first line or first 150 chars as title
    int newlineIndex = content.indexOf('\n');
    String firstLine = newlineIndex > 0 ? content.substring(0, newlineIndex) : content;
    return firstLine.length() > 150 ? firstLine.substring(0, 150) : firstLine;
  }

  private String extractSnippet(String content) {
    if (content == null || content.isBlank()) {
      return "";
    }
    return content.length() > SNIPPET_MAX_LENGTH
        ? content.substring(0, SNIPPET_MAX_LENGTH)
        : content;
  }
}
