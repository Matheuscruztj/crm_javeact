package com.atlasops.ai.infrastructure;

import static org.assertj.core.api.Assertions.*;

import com.atlasops.ai.domain.IngestionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Unit tests for DocumentIngestionAdapter. Validates rejection of documents without extractable
 * text (Requirement 4.11).
 */
@DisplayName("DocumentIngestionAdapter")
class DocumentIngestionAdapterTest {

  private DocumentIngestionAdapter adapter;
  private DocumentChunker chunker;

  @BeforeEach
  void setUp() {
    chunker = new DocumentChunker();
    adapter = new DocumentIngestionAdapter(chunker);
  }

  @ParameterizedTest(name = "content=\"{0}\"")
  @NullAndEmptySource
  @ValueSource(strings = {"   ", "\t", "\n", "  \t\n  "})
  @DisplayName("should return FAILED status when document has no extractable text")
  void should_returnFailed_when_documentHasNoExtractableText(String content) {
    IngestionResult result = adapter.ingest("doc-123", content);

    assertThat(result.status()).isEqualTo(IngestionResult.IngestionStatus.FAILED);
    assertThat(result.documentId()).isEqualTo("doc-123");
    assertThat(result.failureReason()).isNotNull();
    assertThat(result.failureReason()).contains("text");
    assertThat(result.chunksCreated()).isZero();
    assertThat(result.chunkIds()).isEmpty();
  }

  @Test
  @DisplayName("should not generate embeddings when document has no text")
  void should_notGenerateEmbeddings_when_documentHasNoText() {
    IngestionResult result = adapter.ingest("doc-no-text", null);

    // FAILED status with zero chunks means no embeddings were generated
    assertThat(result.status()).isEqualTo(IngestionResult.IngestionStatus.FAILED);
    assertThat(result.chunksCreated()).isZero();
    assertThat(result.chunkIds()).isEmpty();
  }

  @Test
  @DisplayName("should return SUCCESS when document has valid text content")
  void should_returnSuccess_when_documentHasValidText() {
    String validContent = "This is a valid document with extractable text content.";

    IngestionResult result = adapter.ingest("doc-valid", validContent);

    assertThat(result.status()).isEqualTo(IngestionResult.IngestionStatus.SUCCESS);
    assertThat(result.documentId()).isEqualTo("doc-valid");
    assertThat(result.chunksCreated()).isPositive();
    assertThat(result.chunkIds()).isNotEmpty();
    assertThat(result.failureReason()).isNull();
  }

  @Test
  @DisplayName("should throw when documentId is null")
  void should_throwException_when_documentIdIsNull() {
    assertThatThrownBy(() -> adapter.ingest(null, "some content"))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("documentId");
  }

  @Test
  @DisplayName("should throw when documentId is blank")
  void should_throwException_when_documentIdIsBlank() {
    assertThatThrownBy(() -> adapter.ingest("  ", "some content"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("documentId must not be blank");
  }

  @Test
  @DisplayName("should include descriptive reason in failure for blank content")
  void should_includeDescriptiveReason_when_contentIsBlank() {
    IngestionResult result = adapter.ingest("doc-blank", "   ");

    assertThat(result.failureReason()).isNotBlank().containsIgnoringCase("text");
  }
}
