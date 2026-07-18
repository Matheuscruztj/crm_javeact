package com.atlasops.shared.domain;

/**
 * Standard error model following RFC 7807 (Problem Details for HTTP APIs) conventions. Used across
 * all modules for consistent error representation.
 *
 * @param type URI reference identifying the error type (e.g.,
 *     "https://atlasops/errors/validation-failed")
 * @param title Human-readable summary of the error
 * @param status HTTP status code (e.g., 400, 404, 500)
 * @param code Stable, machine-readable error code (e.g., "VALIDATION_FAILED")
 * @param detail Specific occurrence details for this instance of the error
 * @param traceId Correlation ID propagated from the request for traceability
 */
public record ErrorResponse(
    String type, String title, int status, String code, String detail, String traceId) {

  /** Creates an ErrorResponse ensuring mandatory fields are present. */
  public ErrorResponse {
    if (type == null || type.isBlank()) {
      throw new IllegalArgumentException("ErrorResponse type must not be null or empty");
    }
    if (title == null || title.isBlank()) {
      throw new IllegalArgumentException("ErrorResponse title must not be null or empty");
    }
    if (status < 100 || status > 599) {
      throw new IllegalArgumentException(
          "ErrorResponse status must be a valid HTTP status code (100-599)");
    }
    if (code == null || code.isBlank()) {
      throw new IllegalArgumentException("ErrorResponse code must not be null or empty");
    }
    if (traceId == null || traceId.isBlank()) {
      throw new IllegalArgumentException("ErrorResponse traceId must not be null or empty");
    }
  }
}
