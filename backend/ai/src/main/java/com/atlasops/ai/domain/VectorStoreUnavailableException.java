package com.atlasops.ai.domain;

/**
 * Domain exception indicating that the vector store (pgvector) is unavailable. When this occurs
 * during a RAG query, the system should return an error indicating vector store unavailability
 * without interrupting other system functionalities.
 *
 * <p>Validates: Requirements 4.7
 */
public class VectorStoreUnavailableException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public VectorStoreUnavailableException(String message) {
    super(message);
  }

  public VectorStoreUnavailableException(String message, Throwable cause) {
    super(message, cause);
  }
}
