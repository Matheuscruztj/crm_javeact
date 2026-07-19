package com.atlasops.ai.domain.ports;

import com.atlasops.ai.domain.DocumentAnalysisRequest;
import com.atlasops.ai.domain.DocumentAnalysisResult;
import com.atlasops.ai.domain.RelevantChunk;
import java.util.List;

/**
 * Port defining the contract for document analysis operations. Implementations may use Ollama,
 * OpenAI, or any other LLM provider.
 *
 * <p>Validates: Requirements 6.3, 6.4, 6.5
 */
public interface DocumentAnalysisPort {

  /**
   * Performs a document analysis based on the given request. The implementation should handle
   * fallback when the AI provider is unavailable.
   *
   * @param request the document analysis request containing tenant, document, text, and schema info
   * @return the analysis result with summary, category, fields, risks, confidence, and metadata
   */
  DocumentAnalysisResult analyze(DocumentAnalysisRequest request);

  /**
   * Searches for relevant document chunks based on similarity to the query.
   *
   * @param query the search query to find relevant chunks
   * @param maxResults the maximum number of results to return
   * @param minScore the minimum similarity score threshold (0.0-1.0)
   * @return a list of relevant chunks ordered by descending similarity score
   */
  List<RelevantChunk> searchRelevant(String query, int maxResults, double minScore);
}
