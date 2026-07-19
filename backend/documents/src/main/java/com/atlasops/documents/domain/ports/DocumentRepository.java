package com.atlasops.documents.domain.ports;

import com.atlasops.documents.domain.Document;
import com.atlasops.documents.domain.DocumentStatus;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Port defining persistence operations for Document aggregates. All query methods require tenant
 * context for data isolation.
 */
public interface DocumentRepository {

  /**
   * Persists a document (create or update).
   *
   * @param document the document to persist
   * @return the persisted document
   */
  Document save(Document document);

  /**
   * Finds a document by its unique identifier within a tenant.
   *
   * @param id the document identifier
   * @param tenantId the tenant identifier
   * @return the document if found
   */
  Optional<Document> findById(String id, String tenantId);

  /**
   * Finds all documents belonging to a tenant with a specific status, with pagination.
   *
   * @param tenantId the tenant identifier
   * @param status the document status to filter by
   * @param pageable pagination parameters
   * @return a page of documents matching the criteria
   */
  Page<Document> findByTenantIdAndStatus(String tenantId, DocumentStatus status, Pageable pageable);

  /**
   * Finds all documents belonging to a tenant, with pagination.
   *
   * @param tenantId the tenant identifier
   * @param pageable pagination parameters
   * @return a page of documents
   */
  Page<Document> findByTenantId(String tenantId, Pageable pageable);

  /**
   * Finds all documents associated with a specific request within a tenant.
   *
   * @param requestId the request identifier
   * @param tenantId the tenant identifier
   * @param pageable pagination parameters
   * @return a page of documents for the request
   */
  Page<Document> findByRequestIdAndTenantId(String requestId, String tenantId, Pageable pageable);
}
