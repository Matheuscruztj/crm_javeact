package com.atlasops.operations.domain.ports;

import com.atlasops.operations.domain.HealthStatus;

/**
 * Port defining the contract for administrative and monitoring operations. Implementations gather
 * health information from system components.
 */
public interface OperationsPort {

  /**
   * Retrieves the current system health status including all monitored components.
   *
   * @return the aggregate health status of the system
   */
  HealthStatus getSystemHealth();
}
