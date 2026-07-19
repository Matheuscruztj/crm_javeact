package com.atlasops.imports.domain.ports;

import com.atlasops.imports.domain.ImportJob;
import com.atlasops.imports.domain.ImportRequest;

/**
 * Port defining the contract for data import operations. Implementations handle the orchestration
 * of bulk data imports.
 */
public interface ImportPort {

  /**
   * Starts a new data import job based on the given request.
   *
   * @param request the import request containing source and configuration details
   * @return the import job descriptor with tracking information
   */
  ImportJob startImport(ImportRequest request);
}
