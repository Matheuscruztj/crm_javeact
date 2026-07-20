package com.atlasops.documents.infrastructure;

import com.atlasops.documents.domain.AllowedContentType;
import com.atlasops.documents.domain.Document;
import com.atlasops.documents.domain.DocumentStatus;
import com.atlasops.documents.domain.ports.DocumentRepository;
import com.atlasops.shared.domain.types.TenantId;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/**
 * JPA adapter implementing the DocumentRepository domain port. Provides tenant-scoped CRUD
 * operations for Document aggregates.
 */
@Component
public class JpaDocumentRepositoryAdapter implements DocumentRepository {

  private final SpringDataDocumentRepository springDataRepository;

  public JpaDocumentRepositoryAdapter(SpringDataDocumentRepository springDataRepository) {
    this.springDataRepository = springDataRepository;
  }

  @Override
  public Document save(Document document) {
    DocumentJpaEntity entity = toEntity(document);
    DocumentJpaEntity saved = springDataRepository.save(entity);
    return toDomain(saved);
  }

  @Override
  public Optional<Document> findById(String id, String tenantId) {
    return springDataRepository.findByIdAndTenantId(id, tenantId).map(this::toDomain);
  }

  @Override
  public Page<Document> findByTenantIdAndStatus(
      String tenantId, DocumentStatus status, Pageable pageable) {
    return springDataRepository
        .findByTenantIdAndStatus(tenantId, status.name(), pageable)
        .map(this::toDomain);
  }

  @Override
  public Page<Document> findByTenantId(String tenantId, Pageable pageable) {
    return springDataRepository.findByTenantId(tenantId, pageable).map(this::toDomain);
  }

  @Override
  public Page<Document> findByRequestIdAndTenantId(
      String requestId, String tenantId, Pageable pageable) {
    return springDataRepository
        .findByRequestIdAndTenantId(requestId, tenantId, pageable)
        .map(this::toDomain);
  }

  private Document toDomain(DocumentJpaEntity entity) {
    return Document.reconstitute(
        entity.getId(),
        new TenantId(entity.getTenantId()),
        entity.getRequestId(),
        entity.getFilename(),
        AllowedContentType.valueOf(entity.getContentType()),
        entity.getFileSize(),
        entity.getChecksum(),
        entity.getStoragePath(),
        DocumentStatus.valueOf(entity.getStatus()),
        entity.getAnalysisResult(),
        entity.getCreatedAt(),
        entity.getUpdatedAt(),
        entity.isLegalHold(),
        entity.getLegalHoldActivatedAt());
  }

  private DocumentJpaEntity toEntity(Document document) {
    var entity = new DocumentJpaEntity(
        document.getId(),
        document.getTenantId().getValue(),
        document.getRequestId(),
        document.getFilename(),
        document.getContentType().name(),
        document.getFileSize(),
        document.getChecksum(),
        document.getStoragePath(),
        document.getStatus().name(),
        document.getAnalysisResult(),
        document.getCreatedAt(),
        document.getUpdatedAt());
    entity.setLegalHold(document.isLegalHold());
    entity.setLegalHoldActivatedAt(document.getLegalHoldActivatedAt());
    return entity;
  }
}
