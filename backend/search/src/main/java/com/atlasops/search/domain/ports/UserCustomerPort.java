package com.atlasops.search.domain.ports;

import java.util.List;

/**
 * Port for retrieving the customer identifiers associated with a user. Used to enforce CLIENT role
 * search restrictions: CLIENT users can only see entities belonging to their associated customers.
 */
public interface UserCustomerPort {

  /**
   * Returns the customer identifiers associated with the given user within the specified tenant.
   *
   * @param userId the user identifier
   * @param tenantId the tenant identifier
   * @return list of customer identifiers associated with the user (may be empty)
   */
  List<String> findCustomerIdsByUserId(String userId, String tenantId);
}
