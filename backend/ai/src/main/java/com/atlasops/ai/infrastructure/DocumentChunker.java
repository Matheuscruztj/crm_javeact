package com.atlasops.ai.infrastructure;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Splits text documents into chunks of max 1000 tokens with an overlap of 200 tokens. Uses
 * whitespace-based token approximation for splitting. Each chunk is associated with the source
 * document ID.
 *
 * <p>Validates: Requirements 4.3
 */
@Component
public class DocumentChunker {

  private static final int MAX_TOKENS = 1000;
  private static final int OVERLAP_TOKENS = 200;

  /** Represents a single chunk of text with its metadata. */
  public record Chunk(
      String chunkId, String documentId, String content, int tokenCount, int chunkIndex) {
    public Chunk {
      Objects.requireNonNull(chunkId, "chunkId must not be null");
      Objects.requireNonNull(documentId, "documentId must not be null");
      Objects.requireNonNull(content, "content must not be null");
      if (tokenCount <= 0) {
        throw new IllegalArgumentException("tokenCount must be positive, got: " + tokenCount);
      }
      if (chunkIndex < 0) {
        throw new IllegalArgumentException("chunkIndex must not be negative, got: " + chunkIndex);
      }
    }
  }

  /**
   * Splits the given text into chunks of max 1000 tokens with 200 token overlap.
   *
   * @param documentId the identifier of the source document
   * @param text the text content to split
   * @return list of chunks, each associated with the document ID
   * @throws IllegalArgumentException if documentId is null/blank or text is null/empty
   */
  public List<Chunk> chunk(String documentId, String text) {
    Objects.requireNonNull(documentId, "documentId must not be null");
    Objects.requireNonNull(text, "text must not be null");

    if (documentId.isBlank()) {
      throw new IllegalArgumentException("documentId must not be blank");
    }

    String trimmedText = text.trim();
    if (trimmedText.isEmpty()) {
      throw new IllegalArgumentException("text must not be empty");
    }

    String[] tokens = tokenize(trimmedText);
    List<Chunk> chunks = new ArrayList<>();

    int start = 0;
    int chunkIndex = 0;

    while (start < tokens.length) {
      int end = Math.min(start + MAX_TOKENS, tokens.length);
      String chunkContent = joinTokens(tokens, start, end);
      int tokenCount = end - start;

      chunks.add(new Chunk(generateChunkId(), documentId, chunkContent, tokenCount, chunkIndex));

      chunkIndex++;

      // If we've reached the end, stop
      if (end >= tokens.length) {
        break;
      }

      // Move start forward by (MAX_TOKENS - OVERLAP_TOKENS) to create overlap
      start = start + MAX_TOKENS - OVERLAP_TOKENS;
    }

    return chunks;
  }

  /** Tokenizes text by splitting on whitespace (simple approximation). */
  String[] tokenize(String text) {
    return text.split("\\s+");
  }

  /** Joins tokens back into a single string separated by spaces. */
  private String joinTokens(String[] tokens, int start, int end) {
    StringBuilder sb = new StringBuilder();
    for (int i = start; i < end; i++) {
      if (i > start) {
        sb.append(' ');
      }
      sb.append(tokens[i]);
    }
    return sb.toString();
  }

  /** Generates a unique chunk ID. */
  String generateChunkId() {
    return UUID.randomUUID().toString();
  }

  public int getMaxTokens() {
    return MAX_TOKENS;
  }

  public int getOverlapTokens() {
    return OVERLAP_TOKENS;
  }
}
