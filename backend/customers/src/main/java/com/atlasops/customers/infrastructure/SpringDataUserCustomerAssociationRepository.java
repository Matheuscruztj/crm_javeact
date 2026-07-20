package com.atlasops.customers.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

/** Spring Data JPA repository for UserCustomerAssociationJpaEntity persistence. */
@Repository
public interface SpringDataUserCustomerAssociationRepository
    extends JpaRepository<UserCustomerAssociationJpaEntity, String> {

  boolean existsByUserIdAndCustomerId(String userId, String customerId);

  void deleteByUserIdAndCustomerId(String userId, String customerId);

  @Query("SELECT a.customerId FROM UserCustomerAssociationJpaEntity a "
      + "WHERE a.userId = :userId AND a.tenantId = :tenantId")
  List<String> findCustomerIdsByUserId(
      @Param("userId") String userId,
      @Param("tenantId") String tenantId);
}
