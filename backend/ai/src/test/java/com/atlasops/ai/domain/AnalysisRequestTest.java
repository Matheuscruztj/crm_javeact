package com.atlasops.ai.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("AnalysisRequest domain record")
class AnalysisRequestTest {

  @Test
  @DisplayName("should create valid AnalysisRequest with all fields")
  void should_createValidRequest_when_allFieldsProvided() {
    var request =
        new AnalysisRequest(
            "tenant-1", "Analyze this document content", "llama3.1:8b", "document-analysis:v3");

    assertEquals("tenant-1", request.tenantId());
    assertEquals("Analyze this document content", request.inputText());
    assertEquals("llama3.1:8b", request.model());
    assertEquals("document-analysis:v3", request.promptVersion());
  }

  @Test
  @DisplayName("should reject null tenantId")
  void should_rejectRequest_when_tenantIdIsNull() {
    assertThrows(
        NullPointerException.class, () -> new AnalysisRequest(null, "text", "model", "v1"));
  }

  @Test
  @DisplayName("should reject blank tenantId")
  void should_rejectRequest_when_tenantIdIsBlank() {
    assertThrows(
        IllegalArgumentException.class, () -> new AnalysisRequest("  ", "text", "model", "v1"));
  }

  @Test
  @DisplayName("should reject null inputText")
  void should_rejectRequest_when_inputTextIsNull() {
    assertThrows(
        NullPointerException.class, () -> new AnalysisRequest("tenant-1", null, "model", "v1"));
  }

  @Test
  @DisplayName("should reject blank inputText")
  void should_rejectRequest_when_inputTextIsBlank() {
    assertThrows(
        IllegalArgumentException.class, () -> new AnalysisRequest("tenant-1", "", "model", "v1"));
  }

  @Test
  @DisplayName("should reject null model")
  void should_rejectRequest_when_modelIsNull() {
    assertThrows(
        NullPointerException.class, () -> new AnalysisRequest("tenant-1", "text", null, "v1"));
  }

  @Test
  @DisplayName("should reject blank model")
  void should_rejectRequest_when_modelIsBlank() {
    assertThrows(
        IllegalArgumentException.class, () -> new AnalysisRequest("tenant-1", "text", "  ", "v1"));
  }

  @Test
  @DisplayName("should reject null promptVersion")
  void should_rejectRequest_when_promptVersionIsNull() {
    assertThrows(
        NullPointerException.class, () -> new AnalysisRequest("tenant-1", "text", "model", null));
  }

  @Test
  @DisplayName("should reject blank promptVersion")
  void should_rejectRequest_when_promptVersionIsBlank() {
    assertThrows(
        IllegalArgumentException.class, () -> new AnalysisRequest("tenant-1", "text", "model", ""));
  }
}
