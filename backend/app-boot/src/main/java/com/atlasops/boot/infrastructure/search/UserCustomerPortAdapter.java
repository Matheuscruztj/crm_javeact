package com.atlasops.boot.infrastructure.search;

import com.atlasops.customers.infrastructure.SpringDataUserCustomerAssociationRepository;
import com.atlasops.search.domain.ports.UserCustomerPort;
import java.util.List;
import org.springframework.stereotype.Component;

/** Bridges search role-filtering to persisted user-customer associations. */
@Component
public class UserCustomerPortAdapter implements UserCustomerPort {

  private final SpringDataUserCustomerAssociationRepository associationRepository;

  public UserCustomerPortAdapter(
      SpringDataUserCustomerAssociationRepository associationRepository) {
    this.associationRepository = associationRepository;
  }

  @Override
  public List<String> findCustomerIdsByUserId(String userId, String tenantId) {
    return associationRepository.findCustomerIdsByUserId(userId, tenantId);
  }
}
