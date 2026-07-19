package com.atlasops.users.presentation;

import com.atlasops.shared.domain.exceptions.BusinessRuleViolationException;
import com.atlasops.shared.domain.exceptions.DuplicateResourceException;
import com.atlasops.shared.domain.exceptions.ResourceNotFoundException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Test-scoped exception handler that mirrors the GlobalExceptionHandler behavior for standalone
 * MockMvc tests.
 */
@RestControllerAdvice
class TestExceptionHandler {

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(Map.of("status", 400, "detail", ex.getMessage()));
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(Map.of("status", 404, "detail", ex.getMessage()));
  }

  @ExceptionHandler(DuplicateResourceException.class)
  public ResponseEntity<Map<String, Object>> handleDuplicateResource(
      DuplicateResourceException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(Map.of("status", 409, "detail", ex.getMessage()));
  }

  @ExceptionHandler(BusinessRuleViolationException.class)
  public ResponseEntity<Map<String, Object>> handleBusinessRuleViolation(
      BusinessRuleViolationException ex) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
        .body(Map.of("status", 422, "detail", ex.getMessage()));
  }
}
