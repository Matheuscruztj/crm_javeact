package com.atlasops.users.infrastructure;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Spring Data JPA repository for UserJpaEntity with case-insensitive email queries. */
public interface UserSpringDataRepository extends JpaRepository<UserJpaEntity, String> {

  @Query(
      "SELECT u FROM UserJpaEntity u WHERE LOWER(u.email) = LOWER(:email) AND u.tenantId = :tenantId")
  Optional<UserJpaEntity> findByEmailIgnoreCaseAndTenantId(
      @Param("email") String email, @Param("tenantId") String tenantId);

  @Query(
      "SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM UserJpaEntity u WHERE LOWER(u.email) = LOWER(:email) AND u.tenantId = :tenantId")
  boolean existsByEmailIgnoreCaseAndTenantId(
      @Param("email") String email, @Param("tenantId") String tenantId);
}
