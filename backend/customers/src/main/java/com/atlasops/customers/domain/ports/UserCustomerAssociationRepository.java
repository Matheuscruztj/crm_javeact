package com.atlasops.customers.domain.ports;

/**
 * Port defining persistence operations for user-customer associations. Used to link CLIENT users to
 * specific customers for data access restriction.
 */
public interface UserCustomerAssociationRepository {

  /**
   * Checks whether an association between a user and a customer already exists.
   *
   * @param userId the user identifier
   * @param customerId the customer identifier
   * @return true if the association exists
   */
  boolean exists(String userId, String customerId);

  /**
   * Creates an association between a user and a customer.
   *
   * @param userId the user identifier
   * @param customerId the customer identifier
   * @param tenantId the tenant identifier
   */
  void save(String userId, String customerId, String tenantId);
}
