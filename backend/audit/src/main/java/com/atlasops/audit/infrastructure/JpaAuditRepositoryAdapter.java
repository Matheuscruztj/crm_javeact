package com.atlasops.audit.infrastructure;

import com.atlasops.audit.domain.AuditEntry;
import com.atlasops.audit.domain.AuditQueryFilters;
import com.atlasops.audit.domain.ports.AuditRepository;
import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

/**
 * JPA adapter implementing the AuditRepository domain port. INSERT-only — no update or delete
 * operations exist. All queries are scoped by tenantId for multi-tenant isolation.
 */
@Component
public class JpaAuditRepositoryAdapter implements AuditRepository {

  private final SpringDataAuditEntryRepository springDataRepository;

  public JpaAuditRepositoryAdapter(SpringDataAuditEntryRepository springDataRepository) {
    this.springDataRepository = springDataRepository;
  }

  @Override
  public AuditEntry append(AuditEntry entry) {
    AuditEntryJpaEntity entity = toEntity(entry);
    AuditEntryJpaEntity saved = springDataRepository.save(entity);
    return toDomain(saved);
  }

  @Override
  public Page<AuditEntry> query(AuditQueryFilters filters, Pageable pageable) {
    Specification<AuditEntryJpaEntity> spec = AuditEntrySpecifications.fromFilters(filters);

    // Ensure default sort by timestamp descending if no sort specified
    Pageable sortedPageable = ensureTimestampDescSort(pageable);

    return springDataRepository.findAll(spec, sortedPageable).map(this::toDomain);
  }

  private Pageable ensureTimestampDescSort(Pageable pageable) {
    if (pageable.getSort().isUnsorted()) {
      return org.springframework.data.domain.PageRequest.of(
          pageable.getPageNumber(),
          pageable.getPageSize(),
          Sort.by(Sort.Direction.DESC, "timestamp"));
    }
    return pageable;
  }

  private AuditEntry toDomain(AuditEntryJpaEntity entity) {
    return AuditEntry.reconstitute(
        entity.getId(),
        entity.getActionType(),
        entity.getActorId(),
        entity.getTenantId(),
        entity.getEntityType(),
        entity.getEntityId(),
        entity.getCorrelationId(),
        entity.getDetails(),
        entity.getTimestamp());
  }

  private AuditEntryJpaEntity toEntity(AuditEntry entry) {
    return new AuditEntryJpaEntity(
        entry.getId(),
        entry.getActionType(),
        entry.getActorId(),
        entry.getTenantId(),
        entry.getEntityType(),
        entry.getEntityId(),
        entry.getCorrelationId(),
        entry.getDetails(),
        entry.getTimestamp(),
        Instant.now());
  }
}
