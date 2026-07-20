package com.atlasops.ai.domain;

/**
 * Result of evaluating an AI response against a golden example.
 *
 * @param goldenExampleId the golden example used for evaluation
 * @param query           the original query
 * @param expectedAnswer  the expected answer from the golden example
 * @param actualAnswer    the actual answer returned by the AI
 * @param cosineSimilarity cosine similarity score between expected and actual (0.0–1.0)
 * @param passed          whether the response meets the quality threshold
 * @param promptVersion   the prompt version used for this evaluation
 *
 * <p>Validates: P0.G.2 — Golden Dataset + AI Evaluation Framework
 */
public record EvaluationResult(
    String goldenExampleId,
    String query,
    String expectedAnswer,
    String actualAnswer,
    double cosineSimilarity,
    boolean passed,
    String promptVersion) {

  public static final double DEFAULT_PASS_THRESHOLD = 0.7;

  public static EvaluationResult of(
      String goldenExampleId,
      String query,
      String expectedAnswer,
      String actualAnswer,
      double cosineSimilarity,
      String promptVersion) {
    return new EvaluationResult(
        goldenExampleId,
        query,
        expectedAnswer,
        actualAnswer,
        cosineSimilarity,
        cosineSimilarity >= DEFAULT_PASS_THRESHOLD,
        promptVersion);
  }
}
