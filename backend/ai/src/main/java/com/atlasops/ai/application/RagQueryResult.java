package com.atlasops.ai.application;

import java.util.List;
import java.util.Objects;

/**
 * Result of a RAG query pipeline execution. Contains the generated response and the IDs of chunks
 * used for context.
 *
 * <p>Validates: Requirements 4.4
 */
public record RagQueryResult(
    String response, List<String> chunkIds, boolean fallback, long durationMs) {

  public RagQueryResult {
    Objects.requireNonNull(response, "response must not be null");
    Objects.requireNonNull(chunkIds, "chunkIds must not be null");

    if (durationMs < 0) {
      throw new IllegalArgumentException("durationMs must not be negative");
    }

    chunkIds = List.copyOf(chunkIds);
  }
}
