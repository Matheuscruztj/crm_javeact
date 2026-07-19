package com.atlasops.search.infrastructure;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for SearchIndexJpaEntity with native PostgreSQL full-text search
 * queries using tsvector/tsquery.
 */
@Repository
public interface SpringDataSearchIndexRepository
    extends JpaRepository<SearchIndexJpaEntity, String> {

  /** Full-text search across all entity types for a tenant using ts_rank for relevance scoring. */
  @Query(
      value =
          "SELECT si.*, ts_rank(si.content_vector, plainto_tsquery('english', :query)) AS"
              + " rank FROM search_index si WHERE si.tenant_id = :tenantId AND"
              + " si.content_vector @@ plainto_tsquery('english', :query) ORDER BY rank"
              + " DESC",
      countQuery =
          "SELECT count(*) FROM search_index si WHERE si.tenant_id = :tenantId AND"
              + " si.content_vector @@ plainto_tsquery('english', :query)",
      nativeQuery = true)
  Page<Object[]> searchByQuery(
      @Param("query") String query, @Param("tenantId") String tenantId, Pageable pageable);

  /** Full-text search filtered by entity type for a tenant using ts_rank for relevance scoring. */
  @Query(
      value =
          "SELECT si.*, ts_rank(si.content_vector, plainto_tsquery('english', :query)) AS"
              + " rank FROM search_index si WHERE si.tenant_id = :tenantId AND"
              + " si.entity_type = :entityType AND si.content_vector @@ "
              + " plainto_tsquery('english', :query) ORDER BY rank DESC",
      countQuery =
          "SELECT count(*) FROM search_index si WHERE si.tenant_id = :tenantId AND"
              + " si.entity_type = :entityType AND si.content_vector @@ "
              + " plainto_tsquery('english', :query)",
      nativeQuery = true)
  Page<Object[]> searchByQueryAndEntityType(
      @Param("query") String query,
      @Param("tenantId") String tenantId,
      @Param("entityType") String entityType,
      Pageable pageable);

  /**
   * Finds an existing search index entry by entity type, entity ID, and tenant for upsert
   * operations.
   */
  Optional<SearchIndexJpaEntity> findByEntityTypeAndEntityIdAndTenantId(
      String entityType, String entityId, String tenantId);

  /** Deletes search index entries for a specific entity within a tenant. */
  @Modifying
  @Query(
      "DELETE FROM SearchIndexJpaEntity s WHERE s.entityType = :entityType"
          + " AND s.entityId = :entityId AND s.tenantId = :tenantId")
  void deleteByEntityTypeAndEntityIdAndTenantId(
      @Param("entityType") String entityType,
      @Param("entityId") String entityId,
      @Param("tenantId") String tenantId);

  /** Finds all entries for a given tenant (used for testing). */
  List<SearchIndexJpaEntity> findByTenantId(String tenantId);
}
