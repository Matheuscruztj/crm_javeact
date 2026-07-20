package com.atlasops.documents.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
 * Unit tests for ReleaseLegalHoldUseCase.
 * Validates: P2.9 — Legal hold releasing
 */
@ExtendWith(MockitoExtension.class)
class ReleaseLegalHoldUseCaseTest {

    private static final Instant NOW = Instant.parse("2025-01-15T10:00:00Z");
    private static final String TENANT = "tenant-alpha";
    private static final String DOC_ID = "doc-001";

    @Mock private DocumentRepository documentRepository;
    @Mock private Clock clock;

    private ReleaseLegalHoldUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ReleaseLegalHoldUseCase(documentRepository, clock);
        when(clock.now()).thenReturn(NOW);
    }

    private Document aDocumentOnHold() {
        Document doc = Document.create(DOC_ID, new TenantId(TENANT), null, "report.pdf",
                AllowedContentType.PDF, 1024L, "a".repeat(64), NOW);
        doc.activateLegalHold(NOW);
        return doc;
    }

    @Test
    void should_releaseLegalHold_when_holdIsActive() {
        Document doc = aDocumentOnHold();
        assertThat(doc.isOnLegalHold()).isTrue();

        when(documentRepository.findById(DOC_ID, TENANT)).thenReturn(Optional.of(doc));
        when(documentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Document result = useCase.execute(DOC_ID, TENANT, "actor-001");

        assertThat(result.isOnLegalHold()).isFalse();
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
    void should_throwNullPointer_when_tenantIdIsNull() {
        assertThatThrownBy(() -> useCase.execute(DOC_ID, null, "actor-001"))
                .isInstanceOf(NullPointerException.class);
    }
}
