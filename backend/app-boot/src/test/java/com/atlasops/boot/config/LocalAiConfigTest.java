package com.atlasops.boot.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.atlasops.ai.domain.DocumentAnalysisRequest;
import com.atlasops.ai.domain.IngestionResult;
import com.atlasops.ai.domain.RelevantChunk;
import org.junit.jupiter.api.Test;

class LocalAiConfigTest {

  private final LocalAiConfig config = new LocalAiConfig();

  @Test
  void should_returnDeterministicFallbacks_when_localAiBeansAreUsed() {
    var analysisPort = config.documentAnalysisPort();
    var ingestionPort = config.documentIngestionPort();
    var vectorSearchPort = config.vectorSearchPort();

    DocumentAnalysisRequest request =
        new DocumentAnalysisRequest(
            "tenant-1",
            "doc-1",
            "Conteudo extraido",
            "analysis:v1",
            "{\"type\":\"object\"}");

    var result = analysisPort.analyze(request);
    assertThat(result.summary()).contains("disabled in local profile");
    assertThat(result.category()).isEqualTo("UNAVAILABLE");
    assertThat(result.fallback()).isTrue();
    assertThat(result.risks()).contains("Ollama disabled in local profile");
    assertThat(result.missingInformation()).contains("AI enrichment unavailable");

    assertThat(analysisPort.searchRelevant("invoice", 5, 0.8)).isEmpty();

    IngestionResult ingestionResult = ingestionPort.ingest("doc-1", "conteudo");
    assertThat(ingestionResult.status()).isEqualTo(IngestionResult.IngestionStatus.SUCCESS);
    assertThat(ingestionResult.documentId()).isEqualTo("doc-1");
    assertThat(ingestionResult.chunkIds()).isEmpty();

    assertThat(vectorSearchPort.searchSimilar("invoice", 5, 0.5)).isEmpty();
  }
}
