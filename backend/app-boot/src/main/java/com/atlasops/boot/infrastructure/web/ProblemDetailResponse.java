package com.atlasops.boot.infrastructure.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/**
 * RFC 7807 Problem Details response format for standardized error representation.
 *
 * @param type URI reference identifying the error type
 * @param title Human-readable summary of the error
 * @param status HTTP status code
 * @param code Stable, machine-readable error code
 * @param detail Specific occurrence details
 * @param traceId Correlation ID propagated from the request for traceability
 * @param violations Optional list of field-level validation errors
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProblemDetailResponse(
    String type,
    String title,
    int status,
    String code,
    String detail,
    String traceId,
    List<Violation> violations) {

  /** Creates a ProblemDetailResponse without violations. */
  public ProblemDetailResponse(
      String type, String title, int status, String code, String detail, String traceId) {
    this(type, title, status, code, detail, traceId, null);
  }

  /**
   * Represents a single field-level validation violation.
   *
   * @param field The field name that failed validation
   * @param message The validation error message
   */
  public record Violation(String field, String message) {}
}
