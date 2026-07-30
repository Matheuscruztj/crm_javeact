package com.atlasops.ai.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.atlasops.ai.domain.DocumentAnalysisRequest;
import com.atlasops.ai.domain.DocumentAnalysisResult;
import static org.mockito.Mockito.mock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;

class OllamaAIAdapterResilienceTest {

  @Test
  @DisplayName("should_returnDeterministicFallback_when_ollamaIsUnavailable")
  void should_returnDeterministicFallback_when_ollamaIsUnavailable() {
    ChatModel failingChatModel =
        mock(
            ChatModel.class,
            invocation -> {
              throw new IllegalStateException("ollama unavailable");
            });

    OllamaAIAdapter adapter = new OllamaAIAdapter(failingChatModel);
    DocumentAnalysisRequest request =
        new DocumentAnalysisRequest("tenant-alpha", "doc-001", "some text", "analysis:v1", "schema");

    DocumentAnalysisResult result = adapter.analyze(request);

    assertThat(result.fallback()).isTrue();
    assertThat(result.confidenceScore()).isZero();
    assertThat(result.providerMetadata()).contains("ollama-fallback:analysis:v1");
    assertThat(result.summary()).contains("ollama unavailable");
  }

  @Test
  @DisplayName("should_returnFallbackResult_when_directFallbackMethodInvoked")
  void should_returnFallbackResult_when_directFallbackMethodInvoked() {
    OllamaAIAdapter adapter = new OllamaAIAdapter(mock(ChatModel.class));
    DocumentAnalysisResult result = adapter.createFallbackResult("timeout", "analysis:v1");

    assertThat(result.fallback()).isTrue();
    assertThat(result.providerMetadata()).isEqualTo("ollama-fallback:analysis:v1");
    assertThat(result.summary()).contains("timeout");
  }
}
