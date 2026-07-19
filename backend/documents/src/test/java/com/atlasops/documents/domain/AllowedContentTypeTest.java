package com.atlasops.documents.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.Test;

/** Unit tests for the AllowedContentType enum. */
class AllowedContentTypeTest {

  @Test
  void should_returnPdf_when_mimeTypeIsApplicationPdf() {
    Optional<AllowedContentType> result = AllowedContentType.fromMimeType("application/pdf");
    assertThat(result).contains(AllowedContentType.PDF);
  }

  @Test
  void should_returnDocx_when_mimeTypeIsDocx() {
    Optional<AllowedContentType> result =
        AllowedContentType.fromMimeType(
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    assertThat(result).contains(AllowedContentType.DOCX);
  }

  @Test
  void should_returnPng_when_mimeTypeIsImagePng() {
    Optional<AllowedContentType> result = AllowedContentType.fromMimeType("image/png");
    assertThat(result).contains(AllowedContentType.PNG);
  }

  @Test
  void should_returnJpeg_when_mimeTypeIsImageJpeg() {
    Optional<AllowedContentType> result = AllowedContentType.fromMimeType("image/jpeg");
    assertThat(result).contains(AllowedContentType.JPEG);
  }

  @Test
  void should_returnEmpty_when_mimeTypeIsUnsupported() {
    Optional<AllowedContentType> result = AllowedContentType.fromMimeType("text/plain");
    assertThat(result).isEmpty();
  }

  @Test
  void should_returnEmpty_when_mimeTypeIsNull() {
    Optional<AllowedContentType> result = AllowedContentType.fromMimeType(null);
    assertThat(result).isEmpty();
  }

  @Test
  void should_returnEmpty_when_mimeTypeIsBlank() {
    Optional<AllowedContentType> result = AllowedContentType.fromMimeType("   ");
    assertThat(result).isEmpty();
  }

  @Test
  void should_matchCaseInsensitive_when_mimeTypeHasDifferentCase() {
    Optional<AllowedContentType> result = AllowedContentType.fromMimeType("APPLICATION/PDF");
    assertThat(result).contains(AllowedContentType.PDF);
  }

  @Test
  void should_returnTrue_when_isSupportedWithValidMimeType() {
    assertThat(AllowedContentType.isSupported("application/pdf")).isTrue();
    assertThat(AllowedContentType.isSupported("image/png")).isTrue();
  }

  @Test
  void should_returnFalse_when_isSupportedWithInvalidMimeType() {
    assertThat(AllowedContentType.isSupported("text/plain")).isFalse();
    assertThat(AllowedContentType.isSupported("video/mp4")).isFalse();
  }

  @Test
  void should_returnAllMimeTypes_when_supportedMimeTypesCalled() {
    String supported = AllowedContentType.supportedMimeTypes();
    assertThat(supported).contains("application/pdf");
    assertThat(supported).contains("image/png");
    assertThat(supported).contains("image/jpeg");
    assertThat(supported)
        .contains("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
  }

  @Test
  void should_returnCorrectMimeType_when_getMimeTypeCalled() {
    assertThat(AllowedContentType.PDF.getMimeType()).isEqualTo("application/pdf");
    assertThat(AllowedContentType.DOCX.getMimeType())
        .isEqualTo("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    assertThat(AllowedContentType.PNG.getMimeType()).isEqualTo("image/png");
    assertThat(AllowedContentType.JPEG.getMimeType()).isEqualTo("image/jpeg");
  }
}
