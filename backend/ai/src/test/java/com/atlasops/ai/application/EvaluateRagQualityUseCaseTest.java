package com.atlasops.ai.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.atlasops.ai.domain.DocumentAnalysisRequest;
import com.atlasops.ai.domain.DocumentAnalysisResult;
import com.atlasops.ai.domain.EvaluationResult;
import com.atlasops.ai.domain.GoldenExample;
import com.atlasops.ai.domain.ports.DocumentAnalysisPort;
import com.atlasops.ai.domain.ports.GoldenDatasetRepository;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link EvaluateRagQualityUseCase}.
 * Validates: P0.G.2 — Golden Dataset + AI Evaluation Framework
 */
class EvaluateRagQualityUseCaseTest {

  private static final Instant NOW = Instant.parse("2025-01-15T10:00:00Z");
  private static final String TENANT_ID = "tenant-alpha";
  private static final String PROMPT_VERSION = "v1.0";

  private GoldenDatasetRepository goldenDatasetRepository;
  private DocumentAnalysisPort documentAnalysisPort;
  private EvaluateRagQualityUseCase useCase;

  @BeforeEach
  void setUp() {
    goldenDatasetRepository = mock(GoldenDatasetRepository.class);
    documentAnalysisPort = mock(DocumentAnalysisPort.class);
    useCase = new EvaluateRagQualityUseCase(goldenDatasetRepository, documentAnalysisPort);
  }

  @Test
  void should_evaluateAllExamples_when_goldenDatasetHasEntries() {
    GoldenExample example = GoldenExample.create(
        "ge-001", TENANT_ID, "What is the contract value?",
        "The contract value is 100000.", "contract", "admin-001", NOW);

    when(goldenDatasetRepository.findByTenantId(TENANT_ID)).thenReturn(List.of(example));
    when(documentAnalysisPort.analyze(any(DocumentAnalysisRequest.class)))
        .thenReturn(new DocumentAnalysisResult(
            "The contract value is 100000.", "general", List.of(), List.of(), List.of(), 0.9, PROMPT_VERSION, false));

    List<EvaluationResult> results = useCase.evaluate(TENANT_ID, PROMPT_VERSION);

    assertThat(results).hasSize(1);
    assertThat(results.get(0).cosineSimilarity()).isGreaterThan(0.8);
    assertThat(results.get(0).passed()).isTrue();
    assertThat(results.get(0).goldenExampleId()).isEqualTo("ge-001");
  }

  @Test
  void should_returnEmptyResults_when_noGoldenExamples() {
    when(goldenDatasetRepository.findByTenantId(TENANT_ID)).thenReturn(List.of());

    List<EvaluationResult> results = useCase.evaluate(TENANT_ID, PROMPT_VERSION);

    assertThat(results).isEmpty();
  }

  @Test
  void should_markAsFailed_when_aiThrowsException() {
    GoldenExample example = GoldenExample.create(
        "ge-002", TENANT_ID, "Question?", "Expected answer.", "general", "admin-001", NOW);

    when(goldenDatasetRepository.findByTenantId(TENANT_ID)).thenReturn(List.of(example));
    when(documentAnalysisPort.analyze(any())).thenThrow(new RuntimeException("AI unavailable"));

    List<EvaluationResult> results = useCase.evaluate(TENANT_ID, PROMPT_VERSION);

    assertThat(results).hasSize(1);
    assertThat(results.get(0).cosineSimilarity()).isZero();
    assertThat(results.get(0).passed()).isFalse();
    assertThat(results.get(0).actualAnswer()).isEqualTo("EVALUATION_FAILED");
  }

  @Test
  void should_computeHighSimilarity_when_textsAreIdentical() {
    double similarity = EvaluateRagQualityUseCase.cosineSimilarity(
        "The contract value is 100000", "The contract value is 100000");
    assertThat(similarity).isEqualTo(1.0, org.assertj.core.api.Assertions.offset(0.001));
  }

  @Test
  void should_computeZeroSimilarity_when_textsAreEmpty() {
    assertThat(EvaluateRagQualityUseCase.cosineSimilarity("", "some text")).isZero();
    assertThat(EvaluateRagQualityUseCase.cosineSimilarity(null, "some text")).isZero();
  }
}
