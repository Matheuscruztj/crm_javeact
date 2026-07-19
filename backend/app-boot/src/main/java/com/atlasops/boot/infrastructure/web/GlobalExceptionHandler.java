package com.atlasops.boot.infrastructure.web;

import com.atlasops.shared.domain.exceptions.BusinessRuleViolationException;
import com.atlasops.shared.domain.exceptions.DuplicateResourceException;
import com.atlasops.shared.domain.exceptions.ForbiddenActionException;
import com.atlasops.shared.domain.exceptions.ResourceNotFoundException;
import com.atlasops.shared.domain.exceptions.TooManyRequestsException;
import com.atlasops.shared.domain.exceptions.UnauthorizedException;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler that maps exceptions to RFC 7807 Problem Details responses.
 *
 * <p>All error responses follow a consistent format with type, title, status, code, detail, and
 * traceId. Validation errors additionally include a violations array with field-level details.
 *
 * <p>Validates: Requirements 1.2, 2.2, 4.2, 27.1
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
  private static final String ERROR_TYPE_BASE = "https://atlasops/errors/";

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ProblemDetailResponse> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex) {

    List<ProblemDetailResponse.Violation> violations =
        ex.getBindingResult().getFieldErrors().stream()
            .map(
                fieldError ->
                    new ProblemDetailResponse.Violation(
                        fieldError.getField(), fieldError.getDefaultMessage()))
            .toList();

    ProblemDetailResponse response =
        new ProblemDetailResponse(
            ERROR_TYPE_BASE + "validation",
            "Validation Failed",
            HttpStatus.BAD_REQUEST.value(),
            "VALIDATION_FAILED",
            "One or more fields have validation errors",
            getTraceId(),
            violations);

    log.warn("Validation failed: {} violations", violations.size());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ProblemDetailResponse> handleConstraintViolation(
      ConstraintViolationException ex) {

    List<ProblemDetailResponse.Violation> violations =
        ex.getConstraintViolations().stream()
            .map(
                violation ->
                    new ProblemDetailResponse.Violation(
                        extractFieldName(violation.getPropertyPath().toString()),
                        violation.getMessage()))
            .toList();

    ProblemDetailResponse response =
        new ProblemDetailResponse(
            ERROR_TYPE_BASE + "validation",
            "Validation Failed",
            HttpStatus.BAD_REQUEST.value(),
            "VALIDATION_FAILED",
            "One or more fields have validation errors",
            getTraceId(),
            violations);

    log.warn("Constraint violation: {} violations", violations.size());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ProblemDetailResponse> handleIllegalArgument(IllegalArgumentException ex) {

    ProblemDetailResponse response =
        new ProblemDetailResponse(
            ERROR_TYPE_BASE + "bad-request",
            "Bad Request",
            HttpStatus.BAD_REQUEST.value(),
            "BAD_REQUEST",
            ex.getMessage(),
            getTraceId());

    log.warn("Bad request: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ProblemDetailResponse> handleResourceNotFound(
      ResourceNotFoundException ex) {

    ProblemDetailResponse response =
        new ProblemDetailResponse(
            ERROR_TYPE_BASE + "resource-not-found",
            "Resource Not Found",
            HttpStatus.NOT_FOUND.value(),
            "RESOURCE_NOT_FOUND",
            ex.getMessage(),
            getTraceId());

    log.warn("Resource not found: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
  }

  @ExceptionHandler(DuplicateResourceException.class)
  public ResponseEntity<ProblemDetailResponse> handleDuplicateResource(
      DuplicateResourceException ex) {

    ProblemDetailResponse response =
        new ProblemDetailResponse(
            ERROR_TYPE_BASE + "duplicate-resource",
            "Duplicate Resource",
            HttpStatus.CONFLICT.value(),
            "DUPLICATE_RESOURCE",
            ex.getMessage(),
            getTraceId());

    log.warn("Duplicate resource: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
  }

  @ExceptionHandler(ForbiddenActionException.class)
  public ResponseEntity<ProblemDetailResponse> handleForbiddenAction(ForbiddenActionException ex) {

    ProblemDetailResponse response =
        new ProblemDetailResponse(
            ERROR_TYPE_BASE + "forbidden-action",
            "Forbidden",
            HttpStatus.FORBIDDEN.value(),
            "FORBIDDEN_ACTION",
            ex.getMessage(),
            getTraceId());

    log.warn("Forbidden action: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
  }

  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<ProblemDetailResponse> handleUnauthorized(UnauthorizedException ex) {

    ProblemDetailResponse response =
        new ProblemDetailResponse(
            ERROR_TYPE_BASE + "unauthorized",
            "Unauthorized",
            HttpStatus.UNAUTHORIZED.value(),
            "UNAUTHORIZED",
            ex.getMessage(),
            getTraceId());

    log.warn("Unauthorized: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
  }

  @ExceptionHandler(TooManyRequestsException.class)
  public ResponseEntity<ProblemDetailResponse> handleTooManyRequests(TooManyRequestsException ex) {

    ProblemDetailResponse response =
        new ProblemDetailResponse(
            ERROR_TYPE_BASE + "too-many-requests",
            "Too Many Requests",
            HttpStatus.TOO_MANY_REQUESTS.value(),
            "TOO_MANY_REQUESTS",
            ex.getMessage(),
            getTraceId());

    log.warn("Too many requests: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
  }

  @ExceptionHandler(BusinessRuleViolationException.class)
  public ResponseEntity<ProblemDetailResponse> handleBusinessRuleViolation(
      BusinessRuleViolationException ex) {

    ProblemDetailResponse response =
        new ProblemDetailResponse(
            ERROR_TYPE_BASE + "business-rule-violation",
            "Business Rule Violation",
            HttpStatus.UNPROCESSABLE_ENTITY.value(),
            "BUSINESS_RULE_VIOLATION",
            ex.getMessage(),
            getTraceId());

    log.warn("Business rule violation: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ProblemDetailResponse> handleGenericException(Exception ex) {

    ProblemDetailResponse response =
        new ProblemDetailResponse(
            ERROR_TYPE_BASE + "internal-error",
            "Internal Server Error",
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "INTERNAL_ERROR",
            "An unexpected error occurred",
            getTraceId());

    log.error("Unexpected error occurred", ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
  }

  private String getTraceId() {
    String correlationId = MDC.get("correlationId");
    return correlationId != null ? correlationId : "unknown";
  }

  /** Extracts the field name from a property path like "methodName.paramName" → "paramName". */
  private String extractFieldName(String propertyPath) {
    if (propertyPath == null || propertyPath.isBlank()) {
      return "unknown";
    }
    int lastDot = propertyPath.lastIndexOf('.');
    return lastDot >= 0 ? propertyPath.substring(lastDot + 1) : propertyPath;
  }
}
