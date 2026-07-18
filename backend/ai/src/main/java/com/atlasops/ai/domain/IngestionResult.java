package com.atlasops.ai.domain;

import java.util.List;
import java.util.Objects;

/**
 * Domain record representing the result of a document ingestion operation. Contains the document
 * ID, the number of chunks created, their IDs, and status.
 */
public record IngestionResult(
    String documentId,
    IngestionStatus status,
    int chunksCreated,
    List<String> chunkIds,
    String failureReason) {

  /** Status of the ingestion operation. */
  public enum IngestionStatus {
    SUCCESS,
    FAILED
  }

  public IngestionResult {
    Objects.requireNonNull(documentId, "documentId must not be null");
    Objects.requireNonNull(status, "status must not be null");
    Objects.requireNonNull(chunkIds, "chunkIds must not be null");

    if (documentId.isBlank()) {
      throw new IllegalArgumentException("documentId must not be blank");
    }
    if (chunksCreated < 0) {
      throw new IllegalArgumentException(
          "chunksCreated must not be negative, got: " + chunksCreated);
    }

    chunkIds = List.copyOf(chunkIds);
  }

  /** Factory method for a successful ingestion. */
  public static IngestionResult success(
      String documentId, int chunksCreated, List<String> chunkIds) {
    return new IngestionResult(documentId, IngestionStatus.SUCCESS, chunksCreated, chunkIds, null);
  }

  /** Factory method for a failed ingestion. */
  public static IngestionResult failed(String documentId, String reason) {
    return new IngestionResult(documentId, IngestionStatus.FAILED, 0, List.of(), reason);
  }
}
