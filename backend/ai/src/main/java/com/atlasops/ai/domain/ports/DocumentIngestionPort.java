package com.atlasops.ai.domain.ports;

import com.atlasops.ai.domain.IngestionResult;

/**
 * Port defining the contract for document ingestion into the RAG pipeline. Implementations handle
 * text extraction, chunking, embedding generation, and vector storage.
 *
 * <p>Validates: Requirements 4.5
 */
public interface DocumentIngestionPort {

  /**
   * Ingests a document into the vector store. The implementation should: 1. Extract text from the
   * document 2. Split text into chunks (max 1000 tokens, 200 token overlap) 3. Generate embeddings
   * for each chunk 4. Store embeddings in the vector store
   *
   * @param documentId the unique identifier of the document to ingest
   * @param content the text content of the document
   * @return the ingestion result with status and chunk information
   */
  IngestionResult ingest(String documentId, String content);
}
