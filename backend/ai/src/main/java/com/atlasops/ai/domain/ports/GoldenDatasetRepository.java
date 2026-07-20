package com.atlasops.ai.domain.ports;

import com.atlasops.ai.domain.GoldenExample;
import java.util.List;
import java.util.Optional;

/**
 * Port for the golden dataset used in AI quality evaluation.
 *
 * <p>Validates: P0.G.2 — Golden Dataset + AI Evaluation Framework
 */
public interface GoldenDatasetRepository {

  /** Saves a golden example (create or update). */
  GoldenExample save(GoldenExample example);

  /** Finds a golden example by ID within a tenant. */
  Optional<GoldenExample> findById(String id, String tenantId);

  /** Lists all golden examples for a tenant. */
  List<GoldenExample> findByTenantId(String tenantId);

  /** Deletes a golden example by ID within a tenant. */
  void deleteById(String id, String tenantId);
}
