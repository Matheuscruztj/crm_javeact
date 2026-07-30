package com.atlasops.boot.infrastructure.activities;

import com.atlasops.activities.domain.ports.UserCustomerResolverPort;
import com.atlasops.customers.infrastructure.SpringDataUserCustomerAssociationRepository;
import java.util.List;
import org.springframework.stereotype.Component;

/** Bridges the activities module to customer-user associations stored in the customers module. */
@Component
public class UserCustomerResolverAdapter implements UserCustomerResolverPort {

  private final SpringDataUserCustomerAssociationRepository associationRepository;

  public UserCustomerResolverAdapter(
      SpringDataUserCustomerAssociationRepository associationRepository) {
    this.associationRepository = associationRepository;
  }

  @Override
  public List<String> findCustomerIdsByUserId(String userId, String tenantId) {
    return associationRepository.findCustomerIdsByUserId(userId, tenantId);
  }
}
