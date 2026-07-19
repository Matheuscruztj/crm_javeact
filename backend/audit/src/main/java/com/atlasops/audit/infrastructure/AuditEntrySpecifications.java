package com.atlasops.audit.infrastructure;

import com.atlasops.audit.domain.AuditQueryFilters;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

/**
 * JPA Specifications for building dynamic queries on AuditEntryJpaEntity. Builds predicates from
 * AuditQueryFilters, always including tenant isolation.
 */
final class AuditEntrySpecifications {

  private AuditEntrySpecifications() {
    // utility class
  }

  /**
   * Creates a specification that matches the given filters. TenantId is always enforced; other
   * fields are optional.
   *
   * @param filters the query filters
   * @return a JPA Specification for audit entries
   */
  static Specification<AuditEntryJpaEntity> fromFilters(AuditQueryFilters filters) {
    return (root, query, criteriaBuilder) -> {
      List<Predicate> predicates = new ArrayList<>();

      // Always filter by tenantId (mandatory for multi-tenant isolation)
      predicates.add(criteriaBuilder.equal(root.get("tenantId"), filters.tenantId()));

      if (filters.actorId() != null && !filters.actorId().isBlank()) {
        predicates.add(criteriaBuilder.equal(root.get("actorId"), filters.actorId()));
      }

      if (filters.entityType() != null && !filters.entityType().isBlank()) {
        predicates.add(criteriaBuilder.equal(root.get("entityType"), filters.entityType()));
      }

      if (filters.entityId() != null && !filters.entityId().isBlank()) {
        predicates.add(criteriaBuilder.equal(root.get("entityId"), filters.entityId()));
      }

      if (filters.actionType() != null && !filters.actionType().isBlank()) {
        predicates.add(criteriaBuilder.equal(root.get("actionType"), filters.actionType()));
      }

      if (filters.fromTimestamp() != null) {
        predicates.add(
            criteriaBuilder.greaterThanOrEqualTo(root.get("timestamp"), filters.fromTimestamp()));
      }

      if (filters.toTimestamp() != null) {
        predicates.add(
            criteriaBuilder.lessThanOrEqualTo(root.get("timestamp"), filters.toTimestamp()));
      }

      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };
  }
}
