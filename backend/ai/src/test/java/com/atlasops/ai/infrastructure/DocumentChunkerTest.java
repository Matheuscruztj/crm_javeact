package com.atlasops.ai.infrastructure;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for DocumentChunker. Validates chunking logic, overlap, max token constraint, and edge
 * cases.
 *
 * <p>Validates: Requirements 4.3
 */
class DocumentChunkerTest {

  private DocumentChunker chunker;

  @BeforeEach
  void setUp() {
    chunker = new DocumentChunker();
  }

  @Test
  @DisplayName("should produce a single chunk when text has fewer than 1000 tokens")
  void should_produceSingleChunk_when_textSmallerThanMaxTokens() {
    String text = generateTokens(500);

    List<DocumentChunker.Chunk> chunks = chunker.chunk("doc-1", text);

    assertThat(chunks).hasSize(1);
    assertThat(chunks.get(0).tokenCount()).isEqualTo(500);
    assertThat(chunks.get(0).documentId()).isEqualTo("doc-1");
    assertThat(chunks.get(0).chunkIndex()).isEqualTo(0);
  }

  @Test
  @DisplayName("should produce a single chunk when text has exactly 1000 tokens")
  void should_produceSingleChunk_when_textExactly1000Tokens() {
    String text = generateTokens(1000);

    List<DocumentChunker.Chunk> chunks = chunker.chunk("doc-2", text);

    assertThat(chunks).hasSize(1);
    assertThat(chunks.get(0).tokenCount()).isEqualTo(1000);
  }

  @Test
  @DisplayName("should produce two chunks with correct overlap for 1200 tokens")
  void should_produceTwoChunks_when_textSlightlyAboveMaxTokens() {
    String text = generateTokens(1200);

    List<DocumentChunker.Chunk> chunks = chunker.chunk("doc-3", text);

    assertThat(chunks).hasSize(2);
    // First chunk: tokens 0-999 (1000 tokens)
    assertThat(chunks.get(0).tokenCount()).isEqualTo(1000);
    // Second chunk starts at 800 (1000-200 overlap), goes to 1199 → 400 tokens
    assertThat(chunks.get(1).tokenCount()).isEqualTo(400);
  }

  @Test
  @DisplayName("should respect max token constraint on all chunks")
  void should_respectMaxTokenConstraint_when_textIsLarge() {
    String text = generateTokens(3500);

    List<DocumentChunker.Chunk> chunks = chunker.chunk("doc-4", text);

    for (DocumentChunker.Chunk chunk : chunks) {
      assertThat(chunk.tokenCount())
          .as("Chunk at index %d should have at most 1000 tokens", chunk.chunkIndex())
          .isLessThanOrEqualTo(1000);
    }
  }

  @Test
  @DisplayName("should have 200 token overlap between consecutive chunks")
  void should_have200TokenOverlap_when_multipleChunks() {
    // With 2000 tokens:
    // Chunk 0: [0, 1000)
    // Chunk 1: [800, 1800)
    // Chunk 2: [1600, 2000)
    String text = generateTokens(2000);

    List<DocumentChunker.Chunk> chunks = chunker.chunk("doc-5", text);

    assertThat(chunks).hasSize(3);
    assertThat(chunks.get(0).tokenCount()).isEqualTo(1000);
    assertThat(chunks.get(1).tokenCount()).isEqualTo(1000);
    assertThat(chunks.get(2).tokenCount()).isEqualTo(400);

    // Verify overlap: last 200 tokens of chunk[0] should equal first 200 tokens of chunk[1]
    String[] chunk0Tokens = chunks.get(0).content().split("\\s+");
    String[] chunk1Tokens = chunks.get(1).content().split("\\s+");

    String[] overlapFromChunk0 = java.util.Arrays.copyOfRange(chunk0Tokens, 800, 1000);
    String[] overlapFromChunk1 = java.util.Arrays.copyOfRange(chunk1Tokens, 0, 200);

    assertThat(overlapFromChunk0).isEqualTo(overlapFromChunk1);
  }

  @Test
  @DisplayName("should associate all chunks with the source document ID")
  void should_associateChunksWithDocumentId_when_chunking() {
    String text = generateTokens(2500);

    List<DocumentChunker.Chunk> chunks = chunker.chunk("my-document-id", text);

    for (DocumentChunker.Chunk chunk : chunks) {
      assertThat(chunk.documentId()).isEqualTo("my-document-id");
    }
  }

  @Test
  @DisplayName("should assign sequential chunk indices")
  void should_assignSequentialChunkIndices_when_multipleChunks() {
    String text = generateTokens(2500);

    List<DocumentChunker.Chunk> chunks = chunker.chunk("doc-idx", text);

    for (int i = 0; i < chunks.size(); i++) {
      assertThat(chunks.get(i).chunkIndex()).isEqualTo(i);
    }
  }

  @Test
  @DisplayName("should generate unique chunk IDs")
  void should_generateUniqueChunkIds_when_multipleChunks() {
    String text = generateTokens(2500);

    List<DocumentChunker.Chunk> chunks = chunker.chunk("doc-uid", text);

    List<String> ids = chunks.stream().map(DocumentChunker.Chunk::chunkId).toList();
    assertThat(ids).doesNotHaveDuplicates();
  }

  @Test
  @DisplayName("should handle single token text")
  void should_handleSingleToken_when_textIsMinimal() {
    List<DocumentChunker.Chunk> chunks = chunker.chunk("doc-single", "hello");

    assertThat(chunks).hasSize(1);
    assertThat(chunks.get(0).content()).isEqualTo("hello");
    assertThat(chunks.get(0).tokenCount()).isEqualTo(1);
  }

  @Test
  @DisplayName("should throw IllegalArgumentException when documentId is null")
  void should_throwException_when_documentIdIsNull() {
    assertThatThrownBy(() -> chunker.chunk(null, "some text"))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("documentId");
  }

  @Test
  @DisplayName("should throw IllegalArgumentException when documentId is blank")
  void should_throwException_when_documentIdIsBlank() {
    assertThatThrownBy(() -> chunker.chunk("   ", "some text"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("documentId must not be blank");
  }

  @Test
  @DisplayName("should throw NullPointerException when text is null")
  void should_throwException_when_textIsNull() {
    assertThatThrownBy(() -> chunker.chunk("doc-1", null))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("text");
  }

  @Test
  @DisplayName("should throw IllegalArgumentException when text is empty")
  void should_throwException_when_textIsEmpty() {
    assertThatThrownBy(() -> chunker.chunk("doc-1", ""))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("text must not be empty");
  }

  @Test
  @DisplayName("should throw IllegalArgumentException when text is only whitespace")
  void should_throwException_when_textIsBlank() {
    assertThatThrownBy(() -> chunker.chunk("doc-1", "   \t\n  "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("text must not be empty");
  }

  @Test
  @DisplayName("should cover all tokens from the original text without gaps")
  void should_coverAllTokens_when_chunking() {
    int totalTokens = 2500;
    String text = generateTokens(totalTokens);
    String[] originalTokens = text.split("\\s+");

    List<DocumentChunker.Chunk> chunks = chunker.chunk("doc-cov", text);

    // First token of first chunk should be first token of original
    String[] firstChunkTokens = chunks.get(0).content().split("\\s+");
    assertThat(firstChunkTokens[0]).isEqualTo(originalTokens[0]);

    // Last token of last chunk should be last token of original
    DocumentChunker.Chunk lastChunk = chunks.get(chunks.size() - 1);
    String[] lastChunkTokens = lastChunk.content().split("\\s+");
    assertThat(lastChunkTokens[lastChunkTokens.length - 1])
        .isEqualTo(originalTokens[originalTokens.length - 1]);
  }

  /** Generates a string with the specified number of whitespace-separated tokens. */
  private String generateTokens(int count) {
    return IntStream.range(0, count).mapToObj(i -> "token" + i).collect(Collectors.joining(" "));
  }
}
