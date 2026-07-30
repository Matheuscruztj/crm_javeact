package com.atlasops.documents.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlasops.documents.domain.AllowedContentType;
import com.atlasops.documents.domain.Document;
import com.atlasops.documents.domain.ports.DocumentRepository;
import com.atlasops.shared.domain.exceptions.ResourceNotFoundException;
import com.atlasops.shared.domain.ports.Clock;
import com.atlasops.shared.domain.types.TenantId;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for ActivateLegalHoldUseCase. Validates: P2.9 — Legal hold preventing archive/delete
 */
@ExtendWith(MockitoExtension.class)
class ActivateLegalHoldUseCaseTest {

  private static final Instant NOW = Instant.parse("2025-01-15T10:00:00Z");
  private static final String TENANT = "tenant-alpha";
  private static final String DOC_ID = "doc-001";

  @Mock private DocumentRepository documentRepository;
  @Mock private Clock clock;

  private ActivateLegalHoldUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new ActivateLegalHoldUseCase(documentRepository, clock);
    lenient().when(clock.now()).thenReturn(NOW);
  }

  private Document aDocument() {
    return Document.create(
        DOC_ID,
        new TenantId(TENANT),
        null,
        "report.pdf",
        AllowedContentType.PDF,
        1024L,
        "a".repeat(64),
        NOW);
  }

  @Test
  void should_activateLegalHold_when_documentExists() {
    Document doc = aDocument();
    when(documentRepository.findById(DOC_ID, TENANT)).thenReturn(Optional.of(doc));
    when(documentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    Document result = useCase.execute(DOC_ID, TENANT, "actor-001");

    assertThat(result.isLegalHold()).isTrue();
    verify(documentRepository).save(doc);
  }

  @Test
  void should_throwNotFound_when_documentDoesNotExist() {
    when(documentRepository.findById(DOC_ID, TENANT)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> useCase.execute(DOC_ID, TENANT, "actor-001"))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining(DOC_ID);
  }

  @Test
  void should_throwNullPointer_when_documentIdIsNull() {
    assertThatThrownBy(() -> useCase.execute(null, TENANT, "actor-001"))
        .isInstanceOf(NullPointerException.class);
  }
}
