package com.atlasops.ai.domain.ports;

import com.atlasops.ai.domain.RelevantChunk;
import java.util.List;

/**
 * Port defining the contract for vector similarity search operations. Implementations use pgvector
 * for cosine distance similarity searches.
 *
 * <p>Validates: Requirements 4.2, 4.4
 */
public interface VectorSearchPort {

  /**
   * Searches for relevant document chunks based on cosine similarity to the query embedding.
   *
   * @param query the search query text
   * @param maxResults the maximum number of results to return (capped at 5)
   * @param minScore the minimum similarity score threshold (0.0-1.0)
   * @return a list of relevant chunks ordered by descending similarity score
   * @throws com.atlasops.ai.domain.VectorStoreUnavailableException if pgvector is unavailable
   */
  List<RelevantChunk> searchSimilar(String query, int maxResults, double minScore);
}
