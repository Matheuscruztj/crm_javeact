package com.atlasops.customers.application;

import com.atlasops.customers.domain.ports.CustomerRepository;
import com.atlasops.customers.domain.ports.UserCustomerAssociationRepository;
import com.atlasops.shared.domain.exceptions.DuplicateResourceException;
import com.atlasops.shared.domain.exceptions.ResourceNotFoundException;
import java.util.Objects;

/**
 * Use case for associating a CLIENT user with a customer. This restricts the CLIENT user's data
 * access to only this customer's resources.
 */
public class AssociateClientUserUseCase {

  private final CustomerRepository customerRepository;
  private final UserCustomerAssociationRepository associationRepository;

  public AssociateClientUserUseCase(
      CustomerRepository customerRepository,
      UserCustomerAssociationRepository associationRepository) {
    this.customerRepository = customerRepository;
    this.associationRepository = associationRepository;
  }

  /**
   * Associates a CLIENT user with a customer within the specified tenant.
   *
   * @param command the association command
   * @throws ResourceNotFoundException if the customer is not found
   * @throws DuplicateResourceException if the association already exists
   */
  public void execute(AssociateClientUserCommand command) {
    Objects.requireNonNull(command, "Command must not be null");

    customerRepository
        .findById(command.customerId(), command.tenantId())
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    "Customer with id '" + command.customerId() + "' not found"));

    if (associationRepository.exists(command.userId(), command.customerId())) {
      throw new DuplicateResourceException(
          "User '"
              + command.userId()
              + "' is already associated with customer '"
              + command.customerId()
              + "'");
    }

    associationRepository.save(command.userId(), command.customerId(), command.tenantId());
  }

  /**
   * Removes a CLIENT user's association from a customer.
   *
   * @param userId the user identifier
   * @param customerId the customer identifier
   * @param tenantId the tenant identifier
   */
  public void dissociate(String userId, String customerId, String tenantId) {
    Objects.requireNonNull(userId, "User id must not be null");
    Objects.requireNonNull(customerId, "Customer id must not be null");
    Objects.requireNonNull(tenantId, "Tenant id must not be null");

    customerRepository
        .findById(customerId, tenantId)
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    "Customer with id '" + customerId + "' not found"));

    associationRepository.delete(userId, customerId);
  }

  /**
   * Command for associating a CLIENT user with a customer.
   *
   * @param userId the user identifier
   * @param customerId the customer identifier
   * @param tenantId the tenant identifier
   */
  public record AssociateClientUserCommand(String userId, String customerId, String tenantId) {

    public AssociateClientUserCommand {
      Objects.requireNonNull(userId, "User id must not be null");
      Objects.requireNonNull(customerId, "Customer id must not be null");
      Objects.requireNonNull(tenantId, "Tenant id must not be null");
    }
  }
}
