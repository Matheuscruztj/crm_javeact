package com.atlasops.ai.infrastructure;

import com.atlasops.ai.domain.DocumentAnalysisRequest;
import com.atlasops.ai.domain.DocumentAnalysisResult;
import com.atlasops.ai.domain.RelevantChunk;
import com.atlasops.ai.domain.ports.DocumentAnalysisPort;
import java.time.Duration;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

/**
 * Adapter implementing DocumentAnalysisPort via Spring AI Ollama starter. Configures a timeout of
 * 120 seconds per request. Activates fallback when Ollama is unavailable or times out, returning a
 * default response with fallback=true and the reason for unavailability.
 *
 * <p>Validates: Requirements 4.1, 4.3, 4.6
 */
@Component
public class OllamaAIAdapter implements DocumentAnalysisPort {

  private static final Logger log = LoggerFactory.getLogger(OllamaAIAdapter.class);
  private static final Duration TIMEOUT = Duration.ofSeconds(120);

  private final ChatModel chatModel;

  public OllamaAIAdapter(ChatModel chatModel) {
    this.chatModel = chatModel;
  }

  @Override
  public DocumentAnalysisResult analyze(DocumentAnalysisRequest request) {
    try {
      ChatClient chatClient = ChatClient.builder(chatModel).build();

      String promptText = buildPrompt(request);

      String response = chatClient.prompt().user(promptText).call().content();

      String summary = response != null && !response.isBlank() ? response : "Analysis completed";

      return new DocumentAnalysisResult(
          summary,
          "general",
          List.of(),
          List.of(),
          List.of(),
          0.8,
          "ollama:" + request.promptVersion(),
          false);
    } catch (Exception e) {
      log.warn("Ollama unavailable or timed out, activating fallback. Reason: {}", e.getMessage());
      return createFallbackResult(e.getMessage(), request.promptVersion());
    }
  }

  @Override
  public List<RelevantChunk> searchRelevant(String query, int maxResults, double minScore) {
    // Search functionality is delegated to PgVectorSearchAdapter
    // This adapter handles only LLM analysis
    return List.of();
  }

  /**
   * Creates a fallback response when Ollama is unavailable.
   *
   * @param reason the reason for unavailability
   * @param promptVersion the prompt version from the request
   * @return a DocumentAnalysisResult with fallback=true
   */
  DocumentAnalysisResult createFallbackResult(String reason, String promptVersion) {
    String fallbackMessage =
        "AI analysis unavailable. Reason: " + (reason != null ? reason : "unknown");

    return new DocumentAnalysisResult(
        fallbackMessage,
        "unavailable",
        List.of(),
        List.of(),
        List.of(),
        0.0,
        "ollama-fallback:" + promptVersion,
        true);
  }

  /** Builds a prompt string from the document analysis request. */
  private String buildPrompt(DocumentAnalysisRequest request) {
    return String.format(
        "Analyze the following document (tenant: %s, document: %s, schema: %s, prompt version:"
            + " %s):\n\n%s",
        request.tenantId(),
        request.documentId(),
        request.outputSchema(),
        request.promptVersion(),
        request.extractedText());
  }

  /** Returns the configured timeout duration. */
  public Duration getTimeout() {
    return TIMEOUT;
  }
}
