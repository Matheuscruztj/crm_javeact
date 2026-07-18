package com.atlasops.ai.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.jqwik.api.*;

/**
 * Property-based tests for document chunking constraints.
 *
 * <p><b>Validates: Requirements 4.3</b>
 *
 * <p>Property 4: For any text document of size > 0, the chunking algorithm SHALL produce chunks
 * where: each chunk has at most 1000 tokens, consecutive chunks overlap by exactly 200 tokens
 * (except the last), and every chunk is associated with the source document's identifier.
 */
@Tag("Feature: monorepo-sdd-harness, Property 4: Document Chunking Constraints")
class DocumentChunkerPropertyTest {

  private static final int MAX_TOKENS = 1000;
  private static final int OVERLAP_TOKENS = 200;

  private final DocumentChunker chunker = new DocumentChunker();

  // ─── Property: Each chunk has at most 1000 tokens ────────────────────────────

  @Property(tries = 100)
  void eachChunk_shouldHaveAtMost1000Tokens(@ForAll("nonEmptyDocuments") DocumentInput input) {

    List<DocumentChunker.Chunk> chunks = chunker.chunk(input.documentId(), input.text());

    for (DocumentChunker.Chunk chunk : chunks) {
      assertThat(chunk.tokenCount())
          .as(
              "Chunk at index %d should have at most %d tokens, but has %d",
              chunk.chunkIndex(), MAX_TOKENS, chunk.tokenCount())
          .isLessThanOrEqualTo(MAX_TOKENS);
    }
  }

  // ─── Property: Consecutive chunks overlap by exactly 200 tokens (except last) ─

  @Property(tries = 100)
  void consecutiveChunks_shouldOverlapByExactly200Tokens(
      @ForAll("multiChunkDocuments") DocumentInput input) {

    List<DocumentChunker.Chunk> chunks = chunker.chunk(input.documentId(), input.text());

    if (chunks.size() < 2) {
      return; // No overlap to verify with single chunk
    }

    for (int i = 0; i < chunks.size() - 1; i++) {
      String[] currentTokens = chunks.get(i).content().split("\\s+");
      String[] nextTokens = chunks.get(i + 1).content().split("\\s+");

      // The last OVERLAP_TOKENS of the current chunk should equal
      // the first OVERLAP_TOKENS of the next chunk
      int overlapStart = currentTokens.length - OVERLAP_TOKENS;

      // Only validate overlap if the current chunk is full (1000 tokens)
      // The last chunk may be smaller and doesn't need to satisfy overlap
      if (currentTokens.length == MAX_TOKENS) {
        assertThat(overlapStart)
            .as("Overlap start index should be non-negative for chunk %d", i)
            .isGreaterThanOrEqualTo(0);

        String[] overlapFromCurrent = new String[OVERLAP_TOKENS];
        System.arraycopy(currentTokens, overlapStart, overlapFromCurrent, 0, OVERLAP_TOKENS);

        int overlapInNext = Math.min(OVERLAP_TOKENS, nextTokens.length);
        String[] overlapFromNext = new String[overlapInNext];
        System.arraycopy(nextTokens, 0, overlapFromNext, 0, overlapInNext);

        assertThat(overlapFromCurrent)
            .as(
                "Last %d tokens of chunk %d should equal first %d tokens of chunk %d",
                OVERLAP_TOKENS, i, OVERLAP_TOKENS, i + 1)
            .containsExactly(overlapFromNext);
      }
    }
  }

  // ─── Property: Every chunk is associated with the source document's identifier ─

  @Property(tries = 100)
  void everyChunk_shouldBeAssociatedWithSourceDocumentId(
      @ForAll("nonEmptyDocuments") DocumentInput input) {

    List<DocumentChunker.Chunk> chunks = chunker.chunk(input.documentId(), input.text());

    for (DocumentChunker.Chunk chunk : chunks) {
      assertThat(chunk.documentId())
          .as(
              "Chunk at index %d should reference document '%s'",
              chunk.chunkIndex(), input.documentId())
          .isEqualTo(input.documentId());
    }
  }

  // ─── Property: Non-empty input always produces at least one chunk ─────────────

  @Property(tries = 100)
  void nonEmptyInput_shouldAlwaysProduceAtLeastOneChunk(
      @ForAll("nonEmptyDocuments") DocumentInput input) {

    List<DocumentChunker.Chunk> chunks = chunker.chunk(input.documentId(), input.text());

    assertThat(chunks).as("Non-empty text should produce at least one chunk").isNotEmpty();
  }

  // ─── Generators ──────────────────────────────────────────────────────────────

  @Provide
  Arbitrary<DocumentInput> nonEmptyDocuments() {
    Arbitrary<String> documentIds =
        Arbitraries.strings()
            .withCharRange('a', 'z')
            .withCharRange('0', '9')
            .withChars('-', '_')
            .ofMinLength(1)
            .ofMaxLength(36)
            .filter(s -> !s.isBlank());

    Arbitrary<Integer> tokenCounts = Arbitraries.integers().between(1, 3000);

    return Combinators.combine(documentIds, tokenCounts)
        .as((docId, count) -> new DocumentInput(docId, generateTokens(count)));
  }

  @Provide
  Arbitrary<DocumentInput> multiChunkDocuments() {
    Arbitrary<String> documentIds =
        Arbitraries.strings()
            .withCharRange('a', 'z')
            .withCharRange('0', '9')
            .withChars('-', '_')
            .ofMinLength(1)
            .ofMaxLength(36)
            .filter(s -> !s.isBlank());

    // Generate documents that require more than one chunk (> 1000 tokens)
    Arbitrary<Integer> tokenCounts = Arbitraries.integers().between(1001, 5000);

    return Combinators.combine(documentIds, tokenCounts)
        .as((docId, count) -> new DocumentInput(docId, generateTokens(count)));
  }

  // ─── Helpers ─────────────────────────────────────────────────────────────────

  private String generateTokens(int count) {
    return IntStream.range(0, count).mapToObj(i -> "word" + i).collect(Collectors.joining(" "));
  }

  /** Input record combining document ID and text for property generators. */
  record DocumentInput(String documentId, String text) {}
}
