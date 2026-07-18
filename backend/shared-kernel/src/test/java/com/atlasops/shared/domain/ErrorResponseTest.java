package com.atlasops.shared.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ErrorResponseTest {

  @Test
  void should_createErrorResponse_when_allFieldsValid() {
    var error =
        new ErrorResponse(
            "https://atlasops/errors/validation-failed",
            "Validation Failed",
            400,
            "VALIDATION_FAILED",
            "Field 'name' is required",
            "550e8400-e29b-41d4-a716-446655440000");

    assertThat(error.type()).isEqualTo("https://atlasops/errors/validation-failed");
    assertThat(error.title()).isEqualTo("Validation Failed");
    assertThat(error.status()).isEqualTo(400);
    assertThat(error.code()).isEqualTo("VALIDATION_FAILED");
    assertThat(error.detail()).isEqualTo("Field 'name' is required");
    assertThat(error.traceId()).isEqualTo("550e8400-e29b-41d4-a716-446655440000");
  }

  @Test
  void should_createErrorResponse_when_detailIsNull() {
    var error =
        new ErrorResponse(
            "https://atlasops/errors/not-found",
            "Not Found",
            404,
            "RESOURCE_NOT_FOUND",
            null,
            "trace-123");

    assertThat(error.detail()).isNull();
  }

  @Test
  void should_throwException_when_typeIsNull() {
    assertThatThrownBy(() -> new ErrorResponse(null, "Title", 400, "CODE", "detail", "trace"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("type must not be null or empty");
  }

  @Test
  void should_throwException_when_typeIsBlank() {
    assertThatThrownBy(() -> new ErrorResponse("  ", "Title", 400, "CODE", "detail", "trace"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("type must not be null or empty");
  }

  @Test
  void should_throwException_when_titleIsNull() {
    assertThatThrownBy(
            () ->
                new ErrorResponse(
                    "https://atlasops/errors/err", null, 400, "CODE", "detail", "trace"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("title must not be null or empty");
  }

  @Test
  void should_throwException_when_statusBelowRange() {
    assertThatThrownBy(
            () ->
                new ErrorResponse(
                    "https://atlasops/errors/err", "Title", 99, "CODE", "detail", "trace"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("valid HTTP status code");
  }

  @Test
  void should_throwException_when_statusAboveRange() {
    assertThatThrownBy(
            () ->
                new ErrorResponse(
                    "https://atlasops/errors/err", "Title", 600, "CODE", "detail", "trace"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("valid HTTP status code");
  }

  @Test
  void should_throwException_when_codeIsNull() {
    assertThatThrownBy(
            () ->
                new ErrorResponse(
                    "https://atlasops/errors/err", "Title", 400, null, "detail", "trace"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("code must not be null or empty");
  }

  @Test
  void should_throwException_when_traceIdIsNull() {
    assertThatThrownBy(
            () ->
                new ErrorResponse(
                    "https://atlasops/errors/err", "Title", 400, "CODE", "detail", null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("traceId must not be null or empty");
  }

  @Test
  void should_beEqual_when_sameFields() {
    var error1 =
        new ErrorResponse("https://atlasops/errors/err", "Title", 400, "CODE", "detail", "trace");
    var error2 =
        new ErrorResponse("https://atlasops/errors/err", "Title", 400, "CODE", "detail", "trace");
    assertThat(error1).isEqualTo(error2);
    assertThat(error1.hashCode()).isEqualTo(error2.hashCode());
  }

  @Test
  void should_notBeEqual_when_differentFields() {
    var error1 =
        new ErrorResponse("https://atlasops/errors/err", "Title", 400, "CODE", "detail", "trace-1");
    var error2 =
        new ErrorResponse("https://atlasops/errors/err", "Title", 400, "CODE", "detail", "trace-2");
    assertThat(error1).isNotEqualTo(error2);
  }

  @Test
  void should_acceptValidBoundaryStatus_when_100() {
    var error =
        new ErrorResponse("https://atlasops/errors/err", "Title", 100, "CODE", null, "trace");
    assertThat(error.status()).isEqualTo(100);
  }

  @Test
  void should_acceptValidBoundaryStatus_when_599() {
    var error =
        new ErrorResponse("https://atlasops/errors/err", "Title", 599, "CODE", null, "trace");
    assertThat(error.status()).isEqualTo(599);
  }
}
