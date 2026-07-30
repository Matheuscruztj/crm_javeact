package com.atlasops.documents.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlasops.documents.domain.AllowedContentType;
import com.atlasops.documents.domain.Document;
import com.atlasops.documents.domain.DocumentStatus;
import com.atlasops.documents.domain.ports.DocumentRepository;
import com.atlasops.shared.domain.exceptions.BusinessRuleViolationException;
import com.atlasops.shared.domain.exceptions.ResourceNotFoundException;
import com.atlasops.shared.domain.ports.Clock;
import com.atlasops.shared.domain.ports.EventPublisher;
import com.atlasops.shared.domain.types.TenantId;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for ReprocessDocumentUseCase. Validates: P0.N.5 — Document reprocessing */
@ExtendWith(MockitoExtension.class)
class ReprocessDocumentUseCaseTest {

  private static final Instant NOW = Instant.parse("2025-01-15T10:00:00Z");
  private static final String TENANT = "tenant-alpha";
  private static final String DOC_ID = "doc-001";

  @Mock private DocumentRepository documentRepository;
  @Mock private EventPublisher eventPublisher;
  @Mock private Clock clock;

  private ReprocessDocumentUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new ReprocessDocumentUseCase(documentRepository, eventPublisher, clock);
    lenient().when(clock.now()).thenReturn(NOW);
  }

  private Document anAnalyzedDocument() {
    Document doc =
        Document.create(
            DOC_ID,
            new TenantId(TENANT),
            null,
            "report.pdf",
            AllowedContentType.PDF,
            1024L,
            "a".repeat(64),
            NOW);
    // Transition through the lifecycle to ANALYZED
    doc.confirmUpload("storage/path.pdf", "corr-001", NOW);
    doc.clearDomainEvents();
    doc.markTextExtracted("corr-001", NOW);
    doc.markAnalyzed("Summary", false, "corr-002", NOW);
    doc.clearDomainEvents();
    return doc;
  }

  @Test
  void should_reprocessDocument_when_statusIsAnalyzed() {
    Document doc = anAnalyzedDocument();
    assertThat(doc.getStatus()).isEqualTo(DocumentStatus.ANALYZED);

    when(documentRepository.findById(DOC_ID, TENANT)).thenReturn(Optional.of(doc));
    when(documentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    Document result = useCase.execute(DOC_ID, TENANT, "corr-002");

    assertThat(result.getStatus()).isEqualTo(DocumentStatus.UPLOADED);
    verify(documentRepository).save(any());
  }

  @Test
  void should_throwNotFound_when_documentDoesNotExist() {
    when(documentRepository.findById(DOC_ID, TENANT)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> useCase.execute(DOC_ID, TENANT, null))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining(DOC_ID);
  }

  @Test
  void should_throwBusinessRule_when_documentIsInUploadedStatus() {
    Document doc =
        Document.create(
            DOC_ID,
            new TenantId(TENANT),
            null,
            "report.pdf",
            AllowedContentType.PDF,
            1024L,
            "a".repeat(64),
            NOW);
    // Document just created — still PENDING_UPLOAD, not eligible for reprocess

    when(documentRepository.findById(DOC_ID, TENANT)).thenReturn(Optional.of(doc));

    assertThatThrownBy(() -> useCase.execute(DOC_ID, TENANT, null))
        .isInstanceOf(BusinessRuleViolationException.class)
        .hasMessageContaining("ANALYZED");
  }

  @Test
  void should_throwNullPointer_when_documentIdIsNull() {
    assertThatThrownBy(() -> useCase.execute(null, TENANT, null))
        .isInstanceOf(NullPointerException.class);
  }
}
