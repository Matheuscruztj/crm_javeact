package com.atlasops.boot.config;

import com.atlasops.ai.domain.DocumentAnalysisRequest;
import com.atlasops.ai.domain.DocumentAnalysisResult;
import com.atlasops.ai.domain.IngestionResult;
import com.atlasops.ai.domain.RelevantChunk;
import com.atlasops.ai.domain.ports.DocumentAnalysisPort;
import com.atlasops.ai.domain.ports.DocumentIngestionPort;
import com.atlasops.ai.domain.ports.VectorSearchPort;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/** Local AI fallbacks that keep the API bootable without Ollama or pgvector. */
@Configuration(proxyBeanMethods = false)
@Profile("local")
public class LocalAiConfig {

  @Bean
  DocumentAnalysisPort documentAnalysisPort() {
    return new DocumentAnalysisPort() {
      @Override
      public DocumentAnalysisResult analyze(DocumentAnalysisRequest request) {
        return new DocumentAnalysisResult(
            "AI analysis disabled in local profile",
            "UNAVAILABLE",
            List.of(),
            List.of("Ollama disabled in local profile"),
            List.of("AI enrichment unavailable"),
            0.0,
            "local-fallback:analysis",
            true);
      }

      @Override
      public List<RelevantChunk> searchRelevant(String query, int maxResults, double minScore) {
        return List.of();
      }
    };
  }

  @Bean
  DocumentIngestionPort documentIngestionPort() {
    return (documentId, content) -> IngestionResult.success(documentId, 0, List.of());
  }

  @Bean
  VectorSearchPort vectorSearchPort() {
    return (query, maxResults, minScore) -> List.of();
  }
}
