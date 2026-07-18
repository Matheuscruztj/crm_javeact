package com.atlasops.ai.domain.ports;

import com.atlasops.ai.domain.AIAnalysisRecord;
import java.util.List;
import java.util.Optional;

/**
 * Port interface for persisting and retrieving AI analysis records. Implementations may use JPA,
 * in-memory storage, or any other persistence mechanism.
 *
 * <p>Validates: Requirements 4.8
 */
public interface AIAnalysisRecordRepository {

  /**
   * Persists an AI analysis record.
   *
   * @param record the analysis record to save
   * @return the saved record
   */
  AIAnalysisRecord save(AIAnalysisRecord record);

  /**
   * Finds an analysis record by its unique identifier.
   *
   * @param id the record identifier
   * @return the record if found, empty otherwise
   */
  Optional<AIAnalysisRecord> findById(String id);

  /**
   * Finds all analysis records for a given tenant.
   *
   * @param tenantId the tenant identifier
   * @return list of analysis records for the tenant
   */
  List<AIAnalysisRecord> findByTenantId(String tenantId);
}
