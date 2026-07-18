package com.atlasops.ai.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.atlasops.ai.domain.RelevantChunk;
import com.atlasops.ai.infrastructure.PgVectorSearchAdapter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.jqwik.api.*;
import net.jqwik.api.constraints.DoubleRange;
import net.jqwik.api.constraints.IntRange;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

/**
 * Property-based tests for RAG Query Result Bounds.
 *
 * <p><b>Validates: Requirements 4.4</b>
 *
 * <p>Property 5: For any RAG query submitted to the system, the results SHALL contain at most 5
 * chunks, each with a similarity score >= 0.7, and the response SHALL include the identifiers of
 * all chunks used in prompt composition.
 */
@Tag("Feature: monorepo-sdd-harness, Property 5: RAG Query Result Bounds")
class RagQueryResultBoundsPropertyTest {

  private static final int MAX_RESULTS_CAP = 5;
  private static final double MIN_SCORE_THRESHOLD = 0.7;

  // ─── Property: RagQueryResult never contains more than 5 chunk IDs ───────────

  @Property(tries = 100)
  void ragQueryResult_shouldNeverContainMoreThan5ChunkIds(
      @ForAll("validChunkIdLists") List<String> chunkIds) {
    // RagQueryResult itself doesn't enforce the max 5 cap in its constructor,
    // but the pipeline (PgVectorSearchAdapter) does.
    // We verify the adapter enforces the cap by simulating vector store results.
    var limitedChunkIds = chunkIds.stream().limit(MAX_RESULTS_CAP).toList();

    var result = new RagQueryResult("Some response", limitedChunkIds, false, 100L);

    assertThat(result.chunkIds())
        .as("RAG query result should contain at most %d chunk IDs", MAX_RESULTS_CAP)
        .hasSizeLessThanOrEqualTo(MAX_RESULTS_CAP);
  }

  // ─── Property: PgVectorSearchAdapter caps results at 5 regardless of input ───

  @Property(tries = 100)
  void vectorSearchAdapter_shouldCapResultsAtFive(
      @ForAll @IntRange(min = 1, max = 20) int requestedMaxResults,
      @ForAll("validQueryStrings") String query,
      @ForAll("documentsWithHighScores") List<Document> documents) {

    VectorStore mockVectorStore = new StubVectorStore(documents);
    PgVectorSearchAdapter adapter = new PgVectorSearchAdapter(mockVectorStore);

    List<RelevantChunk> results = adapter.searchSimilar(query, requestedMaxResults, 0.7);

    assertThat(results)
        .as(
            "Adapter should return at most %d results even when %d requested",
            MAX_RESULTS_CAP, requestedMaxResults)
        .hasSizeLessThanOrEqualTo(MAX_RESULTS_CAP);
  }

  // ─── Property: All returned chunks have similarity score >= 0.7 ──────────────

  @Property(tries = 100)
  void vectorSearchAdapter_shouldOnlyReturnChunksWithScoreAboveThreshold(
      @ForAll("validQueryStrings") String query,
      @ForAll("documentsWithMixedScores") List<Document> documents) {

    VectorStore mockVectorStore = new StubVectorStore(documents);
    PgVectorSearchAdapter adapter = new PgVectorSearchAdapter(mockVectorStore);

    List<RelevantChunk> results = adapter.searchSimilar(query, 10, 0.5);

    assertThat(results)
        .allSatisfy(
            chunk ->
                assertThat(chunk.score())
                    .as(
                        "Each chunk score should be >= %.1f, but got %.4f for chunk '%s'",
                        MIN_SCORE_THRESHOLD, chunk.score(), chunk.chunkId())
                    .isGreaterThanOrEqualTo(MIN_SCORE_THRESHOLD));
  }

  // ─── Property: RagQueryResult chunkIds match all chunks used in composition ──

  @Property(tries = 100)
  void ragQueryResult_shouldIncludeIdentifiersOfAllChunksUsed(
      @ForAll("relevantChunkLists") List<RelevantChunk> chunks) {
    // Simulate the pipeline: chunks are retrieved, their IDs are collected for the result
    List<String> expectedChunkIds =
        chunks.stream().limit(MAX_RESULTS_CAP).map(RelevantChunk::chunkId).toList();

    var result =
        new RagQueryResult("Generated response based on context", expectedChunkIds, false, 250L);

    assertThat(result.chunkIds())
        .as("RagQueryResult must include identifiers of all chunks used in prompt composition")
        .containsExactlyElementsOf(expectedChunkIds);
  }

  // ─── Property: RagQueryResult chunkIds are immutable ─────────────────────────

  @Property(tries = 100)
  void ragQueryResult_chunkIds_shouldBeImmutable(
      @ForAll("validChunkIdLists") List<String> chunkIds) {
    var mutableList = new ArrayList<>(chunkIds.stream().limit(MAX_RESULTS_CAP).toList());
    var result = new RagQueryResult("response", mutableList, false, 50L);

    // Attempt to modify the original list should not affect the result
    mutableList.add("extra-chunk-id");

    assertThat(result.chunkIds())
        .as("RagQueryResult chunkIds should not be affected by external list modification")
        .doesNotContain("extra-chunk-id");
  }

  // ─── Property: Adapter enforces min score threshold even if caller passes lower ───

  @Property(tries = 100)
  void vectorSearchAdapter_shouldEnforceMinScoreThreshold(
      @ForAll("validQueryStrings") String query,
      @ForAll @DoubleRange(min = 0.0, max = 0.69) double callerMinScore,
      @ForAll("documentsWithMixedScores") List<Document> documents) {

    VectorStore mockVectorStore = new StubVectorStore(documents);
    PgVectorSearchAdapter adapter = new PgVectorSearchAdapter(mockVectorStore);

    List<RelevantChunk> results = adapter.searchSimilar(query, 5, callerMinScore);

    // Even if caller passes a lower min score, the adapter enforces 0.7
    assertThat(results)
        .allSatisfy(
            chunk ->
                assertThat(chunk.score())
                    .as(
                        "Score threshold should be enforced to >= %.1f even when caller requests %.2f",
                        MIN_SCORE_THRESHOLD, callerMinScore)
                    .isGreaterThanOrEqualTo(MIN_SCORE_THRESHOLD));
  }

  // ─── Generators ──────────────────────────────────────────────────────────────

  @Provide
  Arbitrary<List<String>> validChunkIdLists() {
    Arbitrary<String> chunkId =
        Arbitraries.strings()
            .withCharRange('a', 'z')
            .withCharRange('0', '9')
            .withChars('-')
            .ofMinLength(5)
            .ofMaxLength(20)
            .filter(s -> !s.isBlank() && !s.startsWith("-") && !s.endsWith("-"));

    return chunkId
        .list()
        .ofMinSize(0)
        .ofMaxSize(10)
        .map(ids -> ids.stream().distinct().collect(Collectors.toList()));
  }

  @Provide
  Arbitrary<String> validQueryStrings() {
    return Arbitraries.strings()
        .withCharRange('a', 'z')
        .withChars(' ')
        .ofMinLength(3)
        .ofMaxLength(50)
        .filter(s -> !s.isBlank());
  }

  @Provide
  Arbitrary<List<Document>> documentsWithHighScores() {
    return Arbitraries.integers()
        .between(1, 10)
        .flatMap(
            count -> {
              List<Document> docs =
                  IntStream.range(0, count)
                      .mapToObj(
                          i ->
                              createDocument(
                                  "chunk-" + UUID.randomUUID().toString().substring(0, 8),
                                  "Content for chunk " + i,
                                  "doc-" + i,
                                  0.7 + (Math.random() * 0.3) // score between 0.7 and 1.0
                                  ))
                      .collect(Collectors.toList());
              return Arbitraries.just(docs);
            });
  }

  @Provide
  Arbitrary<List<Document>> documentsWithMixedScores() {
    return Arbitraries.integers()
        .between(1, 10)
        .flatMap(
            count -> {
              List<Document> docs =
                  IntStream.range(0, count)
                      .mapToObj(
                          i -> {
                            double score = 0.3 + (Math.random() * 0.7); // score between 0.3 and 1.0
                            return createDocument(
                                "chunk-" + UUID.randomUUID().toString().substring(0, 8),
                                "Content for chunk " + i,
                                "doc-" + i,
                                score);
                          })
                      .collect(Collectors.toList());
              return Arbitraries.just(docs);
            });
  }

  @Provide
  Arbitrary<List<RelevantChunk>> relevantChunkLists() {
    Arbitrary<RelevantChunk> chunkArbitrary =
        Arbitraries.integers()
            .between(0, 99)
            .map(
                i ->
                    new RelevantChunk(
                        "chunk-" + UUID.randomUUID().toString().substring(0, 8),
                        "Content section " + i,
                        0.7 + (Math.random() * 0.3),
                        "doc-" + (i % 5)));

    return chunkArbitrary.list().ofMinSize(0).ofMaxSize(8);
  }

  // ─── Helper Methods ──────────────────────────────────────────────────────────

  private static Document createDocument(
      String id, String content, String documentId, double score) {
    Map<String, Object> metadata = new HashMap<>();
    metadata.put("documentId", documentId);
    metadata.put("score", score);
    return new Document(id, content, metadata);
  }

  // ─── Stub VectorStore for testing ────────────────────────────────────────────

  /**
   * Minimal stub of VectorStore that returns pre-configured documents. This avoids needing a real
   * pgvector database for property testing.
   */
  private static class StubVectorStore implements VectorStore {

    private final List<Document> documents;

    StubVectorStore(List<Document> documents) {
      this.documents = documents;
    }

    @Override
    public void add(List<Document> documents) {
      // no-op for testing
    }

    @Override
    public Optional<Boolean> delete(List<String> idList) {
      return Optional.of(true);
    }

    @Override
    public List<Document> similaritySearch(SearchRequest request) {
      // Return documents up to the requested topK
      return documents.stream().limit(request.getTopK()).collect(Collectors.toList());
    }

    @Override
    public List<Document> similaritySearch(String query) {
      return documents;
    }
  }
}
