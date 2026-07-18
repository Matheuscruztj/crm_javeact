package com.atlasops.ai.infrastructure;

import com.atlasops.ai.domain.IngestionResult;
import com.atlasops.ai.domain.ports.DocumentIngestionPort;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Adapter implementing DocumentIngestionPort for the RAG pipeline. Handles text validation,
 * chunking, and embedding generation.
 *
 * <p>If the document content is null, empty, or blank (no extractable text), the ingestion is
 * rejected with status FAILED and a descriptive reason, without generating any embeddings.
 *
 * <p>Validates: Requirements 4.5, 4.11
 */
@Component
public class DocumentIngestionAdapter implements DocumentIngestionPort {

  private static final Logger log = LoggerFactory.getLogger(DocumentIngestionAdapter.class);
  private static final String NO_TEXT_REASON = "Document does not contain extractable text";

  private final DocumentChunker documentChunker;

  public DocumentIngestionAdapter(DocumentChunker documentChunker) {
    this.documentChunker =
        Objects.requireNonNull(documentChunker, "documentChunker must not be null");
  }

  @Override
  public IngestionResult ingest(String documentId, String content) {
    Objects.requireNonNull(documentId, "documentId must not be null");
    if (documentId.isBlank()) {
      throw new IllegalArgumentException("documentId must not be blank");
    }

    // Requirement 4.11: reject documents without extractable text
    if (!hasExtractableText(content)) {
      log.info("Document {} rejected: no extractable text content", documentId);
      return IngestionResult.failed(documentId, NO_TEXT_REASON);
    }

    // Proceed with chunking and embedding generation
    List<DocumentChunker.Chunk> chunks = documentChunker.chunk(documentId, content);

    List<String> chunkIds = chunks.stream().map(DocumentChunker.Chunk::chunkId).toList();

    // TODO: generate embeddings and store in vector store (to be implemented in pgvector adapter
    // task)

    log.info("Document {} ingested successfully: {} chunks created", documentId, chunks.size());
    return IngestionResult.success(documentId, chunks.size(), chunkIds);
  }

  /**
   * Checks whether the content has extractable text. Returns false if content is null, empty, or
   * contains only whitespace.
   */
  boolean hasExtractableText(String content) {
    return content != null && !content.isBlank();
  }
}
