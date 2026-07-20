package com.atlasops.ai.application;

import com.atlasops.ai.domain.DocumentAnalysisRequest;
import com.atlasops.ai.domain.DocumentAnalysisResult;
import com.atlasops.ai.domain.EvaluationResult;
import com.atlasops.ai.domain.GoldenExample;
import com.atlasops.ai.domain.ports.DocumentAnalysisPort;
import com.atlasops.ai.domain.ports.GoldenDatasetRepository;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Use case for evaluating RAG quality using the golden dataset.
 *
 * <p>For each golden example in the tenant:
 * <ol>
 *   <li>Submits the query to the AI analysis port
 *   <li>Computes cosine similarity between expected and actual answers
 *   <li>Marks as passed if similarity >= threshold (default 0.7)
 * </ol>
 *
 * <p>Validates: P0.G.2 — Golden Dataset + AI Evaluation Framework
 */
public class EvaluateRagQualityUseCase {

  private static final Logger log = LoggerFactory.getLogger(EvaluateRagQualityUseCase.class);

  private final GoldenDatasetRepository goldenDatasetRepository;
  private final DocumentAnalysisPort documentAnalysisPort;

  public EvaluateRagQualityUseCase(
      GoldenDatasetRepository goldenDatasetRepository,
      DocumentAnalysisPort documentAnalysisPort) {
    this.goldenDatasetRepository = Objects.requireNonNull(goldenDatasetRepository);
    this.documentAnalysisPort = Objects.requireNonNull(documentAnalysisPort);
  }

  /**
   * Evaluates all golden examples for a tenant against the current AI pipeline.
   *
   * @param tenantId      the tenant to evaluate
   * @param promptVersion the prompt version to use for evaluation
   * @return list of evaluation results
   */
  public List<EvaluationResult> evaluate(String tenantId, String promptVersion) {
    Objects.requireNonNull(tenantId, "TenantId must not be null");
    Objects.requireNonNull(promptVersion, "PromptVersion must not be null");

    List<GoldenExample> examples = goldenDatasetRepository.findByTenantId(tenantId);
    List<EvaluationResult> results = new ArrayList<>();

    for (GoldenExample example : examples) {
      try {
        DocumentAnalysisRequest request = new DocumentAnalysisRequest(
            "golden-eval", tenantId, example.getQuery(), "text/plain",
            promptVersion, "{}");

        DocumentAnalysisResult aiResult = documentAnalysisPort.analyze(request);
        double similarity = cosineSimilarity(example.getExpectedAnswer(), aiResult.summary());

        results.add(EvaluationResult.of(
            example.getId(),
            example.getQuery(),
            example.getExpectedAnswer(),
            aiResult.summary(),
            similarity,
            promptVersion));

        log.debug("Evaluated golden example {}: similarity={}", example.getId(), similarity);
      } catch (Exception e) {
        log.warn("Failed to evaluate golden example {}: {}", example.getId(), e.getMessage());
        results.add(EvaluationResult.of(
            example.getId(), example.getQuery(),
            example.getExpectedAnswer(), "EVALUATION_FAILED", 0.0, promptVersion));
      }
    }

    return results;
  }

  /**
   * Computes a simple word-overlap cosine similarity between two texts.
   * For production use, replace with proper embedding-based similarity.
   */
  static double cosineSimilarity(String textA, String textB) {
    if (textA == null || textB == null || textA.isBlank() || textB.isBlank()) return 0.0;

    String[] wordsA = textA.toLowerCase().split("\\s+");
    String[] wordsB = textB.toLowerCase().split("\\s+");

    Map<String, Long> freqA = Arrays.stream(wordsA)
        .collect(java.util.stream.Collectors.groupingBy(w -> w, java.util.stream.Collectors.counting()));
    Map<String, Long> freqB = Arrays.stream(wordsB)
        .collect(java.util.stream.Collectors.groupingBy(w -> w, java.util.stream.Collectors.counting()));

    double dotProduct = freqA.entrySet().stream()
        .mapToDouble(e -> e.getValue() * freqB.getOrDefault(e.getKey(), 0L))
        .sum();

    double normA = Math.sqrt(freqA.values().stream().mapToDouble(v -> v * v).sum());
    double normB = Math.sqrt(freqB.values().stream().mapToDouble(v -> v * v).sum());

    if (normA == 0 || normB == 0) return 0.0;
    return dotProduct / (normA * normB);
  }
}
