package com.atlasops.customers.infrastructure;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for CustomerJpaEntity persistence. All queries enforce tenant
 * isolation.
 */
@Repository
public interface SpringDataCustomerRepository extends JpaRepository<CustomerJpaEntity, String> {

  Optional<CustomerJpaEntity> findByIdAndTenantId(String id, String tenantId);

  boolean existsByEmailIgnoreCaseAndTenantId(String email, String tenantId);

  Page<CustomerJpaEntity> findByTenantIdOrderByCreatedAtDesc(String tenantId, Pageable pageable);

  @Query(
      "SELECT c FROM CustomerJpaEntity c WHERE c.tenantId = :tenantId"
          + " AND (LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%'))"
          + " OR LOWER(c.email) LIKE LOWER(CONCAT('%', :query, '%')))"
          + " ORDER BY c.name ASC")
  Page<CustomerJpaEntity> searchByNameOrEmail(
      @Param("query") String query, @Param("tenantId") String tenantId, Pageable pageable);

  /**
   * Finds customers within a specified radius using the Haversine formula approximation. This uses
   * a simplified distance calculation suitable for PostgreSQL. For production with PostGIS, this
   * would use ST_DWithin with geography type.
   */
  @Query(
      "SELECT c FROM CustomerJpaEntity c WHERE c.tenantId = :tenantId"
          + " AND c.latitude IS NOT NULL AND c.longitude IS NOT NULL"
          + " AND (6371.0 * FUNCTION('acos', "
          + "   FUNCTION('cos', FUNCTION('radians', :lat))"
          + "   * FUNCTION('cos', FUNCTION('radians', c.latitude))"
          + "   * FUNCTION('cos', FUNCTION('radians', c.longitude) - FUNCTION('radians', :lon))"
          + "   + FUNCTION('sin', FUNCTION('radians', :lat))"
          + "   * FUNCTION('sin', FUNCTION('radians', c.latitude))"
          + " )) <= :distanceKm"
          + " ORDER BY (6371.0 * FUNCTION('acos', "
          + "   FUNCTION('cos', FUNCTION('radians', :lat))"
          + "   * FUNCTION('cos', FUNCTION('radians', c.latitude))"
          + "   * FUNCTION('cos', FUNCTION('radians', c.longitude) - FUNCTION('radians', :lon))"
          + "   + FUNCTION('sin', FUNCTION('radians', :lat))"
          + "   * FUNCTION('sin', FUNCTION('radians', c.latitude))"
          + " )) ASC")
  Page<CustomerJpaEntity> findByRadiusWithHaversine(
      @Param("lat") double latitude,
      @Param("lon") double longitude,
      @Param("distanceKm") double distanceKm,
      @Param("tenantId") String tenantId,
      Pageable pageable);
}
