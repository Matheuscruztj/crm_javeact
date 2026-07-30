package com.atlasops.ai.infrastructure;

import com.atlasops.ai.domain.RelevantChunk;
import com.atlasops.ai.domain.VectorStoreUnavailableException;
import com.atlasops.ai.domain.ports.VectorSearchPort;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Infrastructure adapter that implements vector similarity search using pgvector via Spring AI's
 * VectorStore abstraction.
 *
 * <p>Constraints: - Maximum 5 results per query - Minimum similarity score of 0.7 - Uses cosine
 * distance for similarity - Gracefully handles pgvector unavailability (throws
 * VectorStoreUnavailableException)
 *
 * <p>Validates: Requirements 4.2, 4.4, 4.7
 */
@Component
@Profile("!local")
public class PgVectorSearchAdapter implements VectorSearchPort {

  private static final Logger log = LoggerFactory.getLogger(PgVectorSearchAdapter.class);

  static final int MAX_RESULTS_CAP = 5;
  static final double MIN_SCORE_THRESHOLD = 0.7;

  private final VectorStore vectorStore;

  public PgVectorSearchAdapter(VectorStore vectorStore) {
    this.vectorStore = Objects.requireNonNull(vectorStore, "vectorStore must not be null");
  }

  @Override
  public List<RelevantChunk> searchSimilar(String query, int maxResults, double minScore) {
    Objects.requireNonNull(query, "query must not be null");
    if (query.isBlank()) {
      throw new IllegalArgumentException("query must not be blank");
    }

    int effectiveMaxResults = Math.min(maxResults, MAX_RESULTS_CAP);
    double effectiveMinScore = Math.max(minScore, MIN_SCORE_THRESHOLD);

    try {
      SearchRequest searchRequest =
          SearchRequest.query(query)
              .withTopK(effectiveMaxResults)
              .withSimilarityThreshold(effectiveMinScore);

      var documents = vectorStore.similaritySearch(searchRequest);

      if (documents == null) {
        return List.of();
      }

      return documents.stream()
          .map(doc -> toRelevantChunk(doc, effectiveMinScore))
          .filter(Objects::nonNull)
          .limit(effectiveMaxResults)
          .toList();

    } catch (VectorStoreUnavailableException e) {
      throw e;
    } catch (Exception e) {
      log.error("Vector store unavailable during similarity search: {}", e.getMessage(), e);
      throw new VectorStoreUnavailableException(
          "Vector store is unavailable: " + e.getMessage(), e);
    }
  }

  private RelevantChunk toRelevantChunk(
      org.springframework.ai.document.Document doc, double minScore) {
    if (doc == null) {
      return null;
    }

    String chunkId = doc.getId();
    String content = doc.getContent();
    Map<String, Object> metadata = doc.getMetadata();

    String documentId =
        metadata != null && metadata.containsKey("documentId")
            ? String.valueOf(metadata.get("documentId"))
            : "unknown";

    // Spring AI provides score in metadata or as part of the document
    double score = extractScore(metadata);

    if (score < minScore) {
      return null;
    }

    return new RelevantChunk(chunkId, content, score, documentId);
  }

  private double extractScore(Map<String, Object> metadata) {
    if (metadata == null || !metadata.containsKey("score")) {
      return MIN_SCORE_THRESHOLD;
    }
    Object scoreObj = metadata.get("score");
    if (scoreObj instanceof Number number) {
      return number.doubleValue();
    }
    try {
      return Double.parseDouble(String.valueOf(scoreObj));
    } catch (NumberFormatException e) {
      return MIN_SCORE_THRESHOLD;
    }
  }
}
