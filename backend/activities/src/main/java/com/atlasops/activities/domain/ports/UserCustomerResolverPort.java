package com.atlasops.activities.domain.ports;

import java.util.List;

/**
 * Port for resolving the customer identifiers associated with a CLIENT user. Used by the activity
 * feed to restrict CLIENT users to activities belonging to their associated customers' entities.
 */
public interface UserCustomerResolverPort {

  /**
   * Returns the list of customer identifiers associated with the given user.
   *
   * @param userId the user identifier
   * @param tenantId the tenant identifier
   * @return list of customer identifiers (empty if no associations exist)
   */
  List<String> findCustomerIdsByUserId(String userId, String tenantId);
}
