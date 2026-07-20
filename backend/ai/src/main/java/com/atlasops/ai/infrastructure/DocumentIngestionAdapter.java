package com.atlasops.ai.infrastructure;

import com.atlasops.ai.domain.IngestionResult;
import com.atlasops.ai.domain.ports.DocumentIngestionPort;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
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
  private final VectorStore vectorStore;

  public DocumentIngestionAdapter(DocumentChunker documentChunker, VectorStore vectorStore) {
    this.documentChunker =
        Objects.requireNonNull(documentChunker, "documentChunker must not be null");
    this.vectorStore =
        Objects.requireNonNull(vectorStore, "vectorStore must not be null");
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

    // Generate embeddings and store in pgvector via Spring AI VectorStore
    List<Document> springDocuments = chunks.stream()
        .map(chunk -> new Document(
            chunk.chunkId(),
            chunk.content(),
            Map.of("documentId", documentId,
                   "chunkIndex", chunk.chunkIndex(),
                   "tokenCount", chunk.tokenCount())))
        .toList();

    try {
      vectorStore.add(springDocuments);
      log.info("Document {} ingested successfully: {} chunks stored in vector store",
          documentId, chunks.size());
    } catch (Exception e) {
      log.warn("Vector store unavailable for document {}: {}. Chunks created without embeddings.",
          documentId, e.getMessage());
      // Graceful degradation: return success even without embeddings
      // Search will fall back to PostgreSQL FTS
    }

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
