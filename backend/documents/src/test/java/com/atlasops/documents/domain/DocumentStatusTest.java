package com.atlasops.documents.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Unit tests for the DocumentStatus enum validating allowed transitions. */
class DocumentStatusTest {

  // --- PENDING_UPLOAD Transitions ---

  @Test
  void should_allowTransitionToUploaded_when_pendingUpload() {
    assertThat(DocumentStatus.PENDING_UPLOAD.canTransitionTo(DocumentStatus.UPLOADED)).isTrue();
  }

  @Test
  void should_allowTransitionToUploadFailed_when_pendingUpload() {
    assertThat(DocumentStatus.PENDING_UPLOAD.canTransitionTo(DocumentStatus.UPLOAD_FAILED))
        .isTrue();
  }

  @Test
  void should_rejectTransitionToTextExtracted_when_pendingUpload() {
    assertThat(DocumentStatus.PENDING_UPLOAD.canTransitionTo(DocumentStatus.TEXT_EXTRACTED))
        .isFalse();
  }

  @Test
  void should_rejectTransitionToAnalyzed_when_pendingUpload() {
    assertThat(DocumentStatus.PENDING_UPLOAD.canTransitionTo(DocumentStatus.ANALYZED)).isFalse();
  }

  @Test
  void should_rejectTransitionToProcessingFailed_when_pendingUpload() {
    assertThat(DocumentStatus.PENDING_UPLOAD.canTransitionTo(DocumentStatus.PROCESSING_FAILED))
        .isFalse();
  }

  // --- UPLOADED Transitions ---

  @Test
  void should_allowTransitionToTextExtracted_when_uploaded() {
    assertThat(DocumentStatus.UPLOADED.canTransitionTo(DocumentStatus.TEXT_EXTRACTED)).isTrue();
  }

  @Test
  void should_allowTransitionToProcessingFailed_when_uploaded() {
    assertThat(DocumentStatus.UPLOADED.canTransitionTo(DocumentStatus.PROCESSING_FAILED)).isTrue();
  }

  @Test
  void should_rejectTransitionToAnalyzed_when_uploaded() {
    assertThat(DocumentStatus.UPLOADED.canTransitionTo(DocumentStatus.ANALYZED)).isFalse();
  }

  @Test
  void should_rejectTransitionToUploaded_when_uploaded() {
    assertThat(DocumentStatus.UPLOADED.canTransitionTo(DocumentStatus.UPLOADED)).isFalse();
  }

  // --- TEXT_EXTRACTED Transitions ---

  @Test
  void should_allowTransitionToAnalyzed_when_textExtracted() {
    assertThat(DocumentStatus.TEXT_EXTRACTED.canTransitionTo(DocumentStatus.ANALYZED)).isTrue();
  }

  @Test
  void should_allowTransitionToProcessingFailed_when_textExtracted() {
    assertThat(DocumentStatus.TEXT_EXTRACTED.canTransitionTo(DocumentStatus.PROCESSING_FAILED))
        .isTrue();
  }

  @Test
  void should_rejectTransitionToUploaded_when_textExtracted() {
    assertThat(DocumentStatus.TEXT_EXTRACTED.canTransitionTo(DocumentStatus.UPLOADED)).isFalse();
  }

  // --- Terminal States ---

  @Test
  void should_allowOnlyReprocess_when_analyzed() {
    assertThat(DocumentStatus.ANALYZED.canTransitionTo(DocumentStatus.UPLOADED)).isTrue();

    for (DocumentStatus target : DocumentStatus.values()) {
      if (target != DocumentStatus.UPLOADED) {
        assertThat(DocumentStatus.ANALYZED.canTransitionTo(target)).isFalse();
      }
    }
  }

  @Test
  void should_rejectAllTransitions_when_uploadFailed() {
    for (DocumentStatus target : DocumentStatus.values()) {
      assertThat(DocumentStatus.UPLOAD_FAILED.canTransitionTo(target)).isFalse();
    }
  }

  @Test
  void should_allowOnlyReprocess_when_processingFailed() {
    assertThat(DocumentStatus.PROCESSING_FAILED.canTransitionTo(DocumentStatus.UPLOADED)).isTrue();

    for (DocumentStatus target : DocumentStatus.values()) {
      if (target != DocumentStatus.UPLOADED) {
        assertThat(DocumentStatus.PROCESSING_FAILED.canTransitionTo(target)).isFalse();
      }
    }
  }
}
