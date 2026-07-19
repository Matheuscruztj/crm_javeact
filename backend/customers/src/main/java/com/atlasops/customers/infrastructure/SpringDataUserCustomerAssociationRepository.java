package com.atlasops.customers.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Spring Data JPA repository for UserCustomerAssociationJpaEntity persistence. */
@Repository
public interface SpringDataUserCustomerAssociationRepository
    extends JpaRepository<UserCustomerAssociationJpaEntity, String> {

  boolean existsByUserIdAndCustomerId(String userId, String customerId);
}
