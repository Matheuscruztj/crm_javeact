package com.atlasops.customers.infrastructure;

import com.atlasops.customers.domain.ports.UserCustomerAssociationRepository;
import org.springframework.stereotype.Component;

/**
 * JPA-based implementation of {@link UserCustomerAssociationRepository}. Manages user-customer
 * association persistence.
 */
@Component
public class JpaUserCustomerAssociationAdapter implements UserCustomerAssociationRepository {

  private final SpringDataUserCustomerAssociationRepository springDataRepository;

  public JpaUserCustomerAssociationAdapter(
      SpringDataUserCustomerAssociationRepository springDataRepository) {
    this.springDataRepository = springDataRepository;
  }

  @Override
  public boolean exists(String userId, String customerId) {
    return springDataRepository.existsByUserIdAndCustomerId(userId, customerId);
  }

  @Override
  public void save(String userId, String customerId, String tenantId) {
    UserCustomerAssociationJpaEntity entity =
        new UserCustomerAssociationJpaEntity(userId, customerId, tenantId);
    springDataRepository.save(entity);
  }

  @Override
  public void delete(String userId, String customerId) {
    springDataRepository.deleteByUserIdAndCustomerId(userId, customerId);
  }

  @Override
  public java.util.List<String> findCustomerIdsByUserId(String userId, String tenantId) {
    return springDataRepository.findCustomerIdsByUserId(userId, tenantId);
  }
}
