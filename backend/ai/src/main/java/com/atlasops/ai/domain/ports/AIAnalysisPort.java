package com.atlasops.ai.domain.ports;

import com.atlasops.ai.domain.AnalysisRequest;
import com.atlasops.ai.domain.AnalysisResult;
import com.atlasops.ai.domain.RelevantChunk;
import java.util.List;

/**
 * Port defining the contract for AI analysis operations. Implementations may use Ollama, OpenAI, or
 * any other LLM provider.
 *
 * <p>Validates: Requirements 4.5
 */
public interface AIAnalysisPort {

  /**
   * Performs an AI analysis based on the given request. The implementation should handle fallback
   * when the AI provider is unavailable.
   *
   * @param request the analysis request containing input text, model, and prompt version
   * @return the analysis result with response text, confidence, and metadata
   */
  AnalysisResult analyze(AnalysisRequest request);

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
