package com.atlasops.documents.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlasops.documents.domain.AllowedContentType;
import com.atlasops.documents.domain.Document;
import com.atlasops.documents.domain.DocumentStatus;
import com.atlasops.shared.domain.types.TenantId;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * Unit tests for JpaDocumentRepositoryAdapter. Validates: Requirements 10.1, 10.3, 10.5
 *
 * <p>Verifies tenant-scoped queries and correct domain-entity mapping.
 */
@ExtendWith(MockitoExtension.class)
class JpaDocumentRepositoryAdapterTest {

  private static final String TENANT_ID = "tenant-alpha";
  private static final String OTHER_TENANT_ID = "tenant-beta";
  private static final String DOCUMENT_ID = "doc-001";
  private static final String REQUEST_ID = "req-001";
  private static final String FILENAME = "contract.pdf";
  private static final String CHECKSUM = "a".repeat(64);
  private static final String STORAGE_PATH = "tenant-alpha/2025/01/doc-001/contract.pdf";
  private static final Instant NOW = Instant.parse("2025-01-15T10:00:00Z");

  @Mock private SpringDataDocumentRepository springDataRepository;

  private JpaDocumentRepositoryAdapter adapter;

  @BeforeEach
  void setUp() {
    adapter = new JpaDocumentRepositoryAdapter(springDataRepository);
  }

  // --- save ---

  @Test
  void should_persistAndReturnDocument_when_saveCalled() {
    // Arrange
    Document document = createTestDocument();
    DocumentJpaEntity savedEntity = createTestEntity();

    when(springDataRepository.save(any(DocumentJpaEntity.class))).thenReturn(savedEntity);

    // Act
    Document result = adapter.save(document);

    // Assert
    assertThat(result.getId()).isEqualTo(DOCUMENT_ID);
    assertThat(result.getTenantId().getValue()).isEqualTo(TENANT_ID);
    assertThat(result.getFilename()).isEqualTo(FILENAME);
    assertThat(result.getContentType()).isEqualTo(AllowedContentType.PDF);
    assertThat(result.getStatus()).isEqualTo(DocumentStatus.PENDING_UPLOAD);

    ArgumentCaptor<DocumentJpaEntity> captor = ArgumentCaptor.forClass(DocumentJpaEntity.class);
    verify(springDataRepository).save(captor.capture());

    DocumentJpaEntity capturedEntity = captor.getValue();
    assertThat(capturedEntity.getId()).isEqualTo(DOCUMENT_ID);
    assertThat(capturedEntity.getTenantId()).isEqualTo(TENANT_ID);
  }

  // --- findById ---

  @Test
  void should_returnDocument_when_foundByIdAndTenantId() {
    // Arrange
    DocumentJpaEntity entity = createTestEntity();
    when(springDataRepository.findByIdAndTenantId(DOCUMENT_ID, TENANT_ID))
        .thenReturn(Optional.of(entity));

    // Act
    Optional<Document> result = adapter.findById(DOCUMENT_ID, TENANT_ID);

    // Assert
    assertThat(result).isPresent();
    assertThat(result.get().getId()).isEqualTo(DOCUMENT_ID);
    assertThat(result.get().getTenantId().getValue()).isEqualTo(TENANT_ID);
  }

  @Test
  void should_returnEmpty_when_documentNotFoundForTenant() {
    // Arrange
    when(springDataRepository.findByIdAndTenantId(DOCUMENT_ID, OTHER_TENANT_ID))
        .thenReturn(Optional.empty());

    // Act
    Optional<Document> result = adapter.findById(DOCUMENT_ID, OTHER_TENANT_ID);

    // Assert
    assertThat(result).isEmpty();
  }

  @Test
  void should_includeTenantIdInQuery_when_findByIdCalled() {
    // Arrange
    when(springDataRepository.findByIdAndTenantId(DOCUMENT_ID, TENANT_ID))
        .thenReturn(Optional.empty());

    // Act
    adapter.findById(DOCUMENT_ID, TENANT_ID);

    // Assert - verify tenant isolation is enforced
    verify(springDataRepository).findByIdAndTenantId(eq(DOCUMENT_ID), eq(TENANT_ID));
  }

  // --- findByTenantIdAndStatus ---

  @Test
  void should_returnPagedDocuments_when_queryByTenantAndStatus() {
    // Arrange
    Pageable pageable = PageRequest.of(0, 20);
    DocumentJpaEntity entity = createTestEntity();
    Page<DocumentJpaEntity> page = new PageImpl<>(List.of(entity), pageable, 1);

    when(springDataRepository.findByTenantIdAndStatus(TENANT_ID, "PENDING_UPLOAD", pageable))
        .thenReturn(page);

    // Act
    Page<Document> result =
        adapter.findByTenantIdAndStatus(TENANT_ID, DocumentStatus.PENDING_UPLOAD, pageable);

    // Assert
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getStatus()).isEqualTo(DocumentStatus.PENDING_UPLOAD);
    assertThat(result.getTotalElements()).isEqualTo(1);
  }

  @Test
  void should_returnEmptyPage_when_noDocumentsMatchStatusForTenant() {
    // Arrange
    Pageable pageable = PageRequest.of(0, 20);
    Page<DocumentJpaEntity> emptyPage = Page.empty(pageable);

    when(springDataRepository.findByTenantIdAndStatus(TENANT_ID, "ANALYZED", pageable))
        .thenReturn(emptyPage);

    // Act
    Page<Document> result =
        adapter.findByTenantIdAndStatus(TENANT_ID, DocumentStatus.ANALYZED, pageable);

    // Assert
    assertThat(result.getContent()).isEmpty();
    assertThat(result.getTotalElements()).isZero();
  }

  // --- findByTenantId ---

  @Test
  void should_returnAllDocumentsForTenant_when_findByTenantId() {
    // Arrange
    Pageable pageable = PageRequest.of(0, 20);
    DocumentJpaEntity entity = createTestEntity();
    Page<DocumentJpaEntity> page = new PageImpl<>(List.of(entity), pageable, 1);

    when(springDataRepository.findByTenantId(TENANT_ID, pageable)).thenReturn(page);

    // Act
    Page<Document> result = adapter.findByTenantId(TENANT_ID, pageable);

    // Assert
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getTenantId().getValue()).isEqualTo(TENANT_ID);
  }

  // --- findByRequestIdAndTenantId ---

  @Test
  void should_returnDocumentsForRequest_when_queriedWithTenantId() {
    // Arrange
    Pageable pageable = PageRequest.of(0, 20);
    DocumentJpaEntity entity = createTestEntity();
    Page<DocumentJpaEntity> page = new PageImpl<>(List.of(entity), pageable, 1);

    when(springDataRepository.findByRequestIdAndTenantId(REQUEST_ID, TENANT_ID, pageable))
        .thenReturn(page);

    // Act
    Page<Document> result = adapter.findByRequestIdAndTenantId(REQUEST_ID, TENANT_ID, pageable);

    // Assert
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getRequestId()).isEqualTo(REQUEST_ID);
    verify(springDataRepository)
        .findByRequestIdAndTenantId(eq(REQUEST_ID), eq(TENANT_ID), eq(pageable));
  }

  @Test
  void should_returnEmptyPage_when_noDocumentsForRequestInTenant() {
    // Arrange
    Pageable pageable = PageRequest.of(0, 20);
    Page<DocumentJpaEntity> emptyPage = Page.empty(pageable);

    when(springDataRepository.findByRequestIdAndTenantId(REQUEST_ID, OTHER_TENANT_ID, pageable))
        .thenReturn(emptyPage);

    // Act
    Page<Document> result =
        adapter.findByRequestIdAndTenantId(REQUEST_ID, OTHER_TENANT_ID, pageable);

    // Assert
    assertThat(result.getContent()).isEmpty();
  }

  // --- Domain-Entity Mapping ---

  @Test
  void should_correctlyMapAllFields_when_convertingEntityToDomain() {
    // Arrange
    DocumentJpaEntity entity =
        new DocumentJpaEntity(
            DOCUMENT_ID,
            TENANT_ID,
            REQUEST_ID,
            FILENAME,
            "PDF",
            1024L,
            CHECKSUM,
            STORAGE_PATH,
            "UPLOADED",
            "{\"summary\":\"test\"}",
            NOW,
            NOW.plusSeconds(60));

    when(springDataRepository.findByIdAndTenantId(DOCUMENT_ID, TENANT_ID))
        .thenReturn(Optional.of(entity));

    // Act
    Optional<Document> result = adapter.findById(DOCUMENT_ID, TENANT_ID);

    // Assert
    assertThat(result).isPresent();
    Document doc = result.get();
    assertThat(doc.getId()).isEqualTo(DOCUMENT_ID);
    assertThat(doc.getTenantId().getValue()).isEqualTo(TENANT_ID);
    assertThat(doc.getRequestId()).isEqualTo(REQUEST_ID);
    assertThat(doc.getFilename()).isEqualTo(FILENAME);
    assertThat(doc.getContentType()).isEqualTo(AllowedContentType.PDF);
    assertThat(doc.getFileSize()).isEqualTo(1024L);
    assertThat(doc.getChecksum()).isEqualTo(CHECKSUM);
    assertThat(doc.getStoragePath()).isEqualTo(STORAGE_PATH);
    assertThat(doc.getStatus()).isEqualTo(DocumentStatus.UPLOADED);
    assertThat(doc.getAnalysisResult()).isEqualTo("{\"summary\":\"test\"}");
    assertThat(doc.getCreatedAt()).isEqualTo(NOW);
    assertThat(doc.getUpdatedAt()).isEqualTo(NOW.plusSeconds(60));
  }

  // --- Helpers ---

  private Document createTestDocument() {
    return Document.reconstitute(
        DOCUMENT_ID,
        new TenantId(TENANT_ID),
        REQUEST_ID,
        FILENAME,
        AllowedContentType.PDF,
        1024L,
        CHECKSUM,
        null,
        DocumentStatus.PENDING_UPLOAD,
        null,
        NOW,
        NOW);
  }

  private DocumentJpaEntity createTestEntity() {
    return new DocumentJpaEntity(
        DOCUMENT_ID,
        TENANT_ID,
        REQUEST_ID,
        FILENAME,
        "PDF",
        1024L,
        CHECKSUM,
        null,
        "PENDING_UPLOAD",
        null,
        NOW,
        NOW);
  }
}
