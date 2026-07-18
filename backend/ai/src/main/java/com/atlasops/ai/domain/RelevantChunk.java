package com.atlasops.ai.domain;

import java.util.Objects;

/**
 * Domain record representing a relevant document chunk retrieved from the vector store. Contains
 * the chunk ID, its content, the similarity score, and the source document ID.
 */
public record RelevantChunk(String chunkId, String content, double score, String documentId) {

  public RelevantChunk {
    Objects.requireNonNull(chunkId, "chunkId must not be null");
    Objects.requireNonNull(content, "content must not be null");
    Objects.requireNonNull(documentId, "documentId must not be null");

    if (chunkId.isBlank()) {
      throw new IllegalArgumentException("chunkId must not be blank");
    }
    if (documentId.isBlank()) {
      throw new IllegalArgumentException("documentId must not be blank");
    }
    if (score < 0.0 || score > 1.0) {
      throw new IllegalArgumentException("score must be between 0.0 and 1.0, got: " + score);
    }
  }
}
