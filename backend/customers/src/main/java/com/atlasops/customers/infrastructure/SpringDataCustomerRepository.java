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
      value =
          "SELECT * FROM customers c WHERE c.tenant_id = :tenantId"
              + " AND c.latitude IS NOT NULL AND c.longitude IS NOT NULL"
              + " AND (6371.0 * acos("
              + "   cos(radians(:lat))"
              + "   * cos(radians(c.latitude))"
              + "   * cos(radians(c.longitude) - radians(:lon))"
              + "   + sin(radians(:lat))"
              + "   * sin(radians(c.latitude))"
              + " )) <= :distanceKm"
              + " ORDER BY (6371.0 * acos("
              + "   cos(radians(:lat))"
              + "   * cos(radians(c.latitude))"
              + "   * cos(radians(c.longitude) - radians(:lon))"
              + "   + sin(radians(:lat))"
              + "   * sin(radians(c.latitude))"
              + " )) ASC",
      countQuery =
          "SELECT count(*) FROM customers c WHERE c.tenant_id = :tenantId"
              + " AND c.latitude IS NOT NULL AND c.longitude IS NOT NULL"
              + " AND (6371.0 * acos("
              + "   cos(radians(:lat))"
              + "   * cos(radians(c.latitude))"
              + "   * cos(radians(c.longitude) - radians(:lon))"
              + "   + sin(radians(:lat))"
              + "   * sin(radians(c.latitude))"
              + " )) <= :distanceKm",
      nativeQuery = true)
  Page<CustomerJpaEntity> findByRadiusWithHaversine(
      @Param("lat") double latitude,
      @Param("lon") double longitude,
      @Param("distanceKm") double distanceKm,
      @Param("tenantId") String tenantId,
      Pageable pageable);
}
