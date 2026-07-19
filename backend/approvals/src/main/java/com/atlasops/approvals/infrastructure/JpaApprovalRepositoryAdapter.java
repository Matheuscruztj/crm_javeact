package com.atlasops.approvals.infrastructure;

import com.atlasops.approvals.domain.Approval;
import com.atlasops.approvals.domain.ApprovalStatus;
import com.atlasops.approvals.domain.ports.ApprovalRepository;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/**
 * JPA adapter implementing the ApprovalRepository domain port. Provides tenant-scoped CRUD
 * operations for Approval aggregates.
 */
@Component
public class JpaApprovalRepositoryAdapter implements ApprovalRepository {

  private final SpringDataApprovalRepository springDataRepository;

  public JpaApprovalRepositoryAdapter(SpringDataApprovalRepository springDataRepository) {
    this.springDataRepository = springDataRepository;
  }

  @Override
  public Approval save(Approval approval) {
    ApprovalJpaEntity entity = toEntity(approval);
    ApprovalJpaEntity saved = springDataRepository.save(entity);
    return toDomain(saved);
  }

  @Override
  public Optional<Approval> findById(String id, String tenantId) {
    return springDataRepository.findByIdAndTenantId(id, tenantId).map(this::toDomain);
  }

  @Override
  public Optional<Approval> findByDocumentIdAndTenantId(String documentId, String tenantId) {
    return springDataRepository
        .findByDocumentIdAndTenantId(documentId, tenantId)
        .map(this::toDomain);
  }

  @Override
  public Page<Approval> findPendingByTenantId(String tenantId, Pageable pageable) {
    return springDataRepository
        .findByTenantIdAndStatus(tenantId, ApprovalStatus.PENDING.name(), pageable)
        .map(this::toDomain);
  }

  @Override
  public Page<Approval> findByTenantIdAndStatus(
      String tenantId, ApprovalStatus status, Pageable pageable) {
    return springDataRepository
        .findByTenantIdAndStatus(tenantId, status.name(), pageable)
        .map(this::toDomain);
  }

  private Approval toDomain(ApprovalJpaEntity entity) {
    return Approval.reconstitute(
        entity.getId(),
        entity.getTenantId(),
        entity.getDocumentId(),
        ApprovalStatus.valueOf(entity.getStatus()),
        entity.getDecisionBy(),
        entity.getRejectionReason(),
        entity.getCreatedAt(),
        entity.getDecisionAt());
  }

  private ApprovalJpaEntity toEntity(Approval approval) {
    return new ApprovalJpaEntity(
        approval.getId(),
        approval.getTenantId(),
        approval.getDocumentId(),
        approval.getStatus().name(),
        approval.getDecisionBy(),
        approval.getRejectionReason(),
        approval.getCreatedAt(),
        approval.getDecidedAt());
  }
}
