package com.atlasops.boot.infrastructure.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.atlasops.shared.domain.exceptions.BusinessRuleViolationException;
import com.atlasops.shared.domain.exceptions.DuplicateResourceException;
import com.atlasops.shared.domain.exceptions.ForbiddenActionException;
import com.atlasops.shared.domain.exceptions.ResourceNotFoundException;
import com.atlasops.shared.domain.exceptions.UnauthorizedException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.MDC;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

class GlobalExceptionHandlerTest {

  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
  private static final String TRACE_ID = "test-correlation-id-123";

  @BeforeEach
  void setUp() {
    MDC.put("correlationId", TRACE_ID);
  }

  @AfterEach
  void tearDown() {
    MDC.clear();
  }

  @Test
  void should_returnBadRequestWithViolations_when_methodArgumentNotValid() {
    BindingResult bindingResult = Mockito.mock(BindingResult.class);
    FieldError fieldError = new FieldError("object", "email", "must be a valid email");
    Mockito.when(bindingResult.getFieldErrors()).thenReturn(java.util.List.of(fieldError));

    MethodParameter methodParameter = Mockito.mock(MethodParameter.class);
    MethodArgumentNotValidException ex =
        new MethodArgumentNotValidException(methodParameter, bindingResult);

    ResponseEntity<ProblemDetailResponse> response = handler.handleMethodArgumentNotValid(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    ProblemDetailResponse body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.type()).isEqualTo("https://atlasops/errors/validation");
    assertThat(body.title()).isEqualTo("Validation Failed");
    assertThat(body.status()).isEqualTo(400);
    assertThat(body.code()).isEqualTo("VALIDATION_FAILED");
    assertThat(body.detail()).isEqualTo("One or more fields have validation errors");
    assertThat(body.traceId()).isEqualTo(TRACE_ID);
    assertThat(body.violations()).hasSize(1);
    assertThat(body.violations().getFirst().field()).isEqualTo("email");
    assertThat(body.violations().getFirst().message()).isEqualTo("must be a valid email");
  }

  @Test
  @SuppressWarnings("unchecked")
  void should_returnBadRequestWithViolations_when_constraintViolation() {
    ConstraintViolation<Object> violation = Mockito.mock(ConstraintViolation.class);
    Path path = Mockito.mock(Path.class);
    Mockito.when(path.toString()).thenReturn("createCustomer.name");
    Mockito.when(violation.getPropertyPath()).thenReturn(path);
    Mockito.when(violation.getMessage()).thenReturn("must not be blank");

    ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation));

    ResponseEntity<ProblemDetailResponse> response = handler.handleConstraintViolation(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    ProblemDetailResponse body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.type()).isEqualTo("https://atlasops/errors/validation");
    assertThat(body.code()).isEqualTo("VALIDATION_FAILED");
    assertThat(body.traceId()).isEqualTo(TRACE_ID);
    assertThat(body.violations()).hasSize(1);
    assertThat(body.violations().getFirst().field()).isEqualTo("name");
    assertThat(body.violations().getFirst().message()).isEqualTo("must not be blank");
  }

  @Test
  void should_returnBadRequest_when_illegalArgument() {
    IllegalArgumentException ex = new IllegalArgumentException("Invalid page size");

    ResponseEntity<ProblemDetailResponse> response = handler.handleIllegalArgument(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    ProblemDetailResponse body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.type()).isEqualTo("https://atlasops/errors/bad-request");
    assertThat(body.code()).isEqualTo("BAD_REQUEST");
    assertThat(body.detail()).isEqualTo("Invalid page size");
    assertThat(body.traceId()).isEqualTo(TRACE_ID);
    assertThat(body.violations()).isNull();
  }

  @Test
  void should_returnNotFound_when_resourceNotFound() {
    ResourceNotFoundException ex = new ResourceNotFoundException("Customer not found");

    ResponseEntity<ProblemDetailResponse> response = handler.handleResourceNotFound(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    ProblemDetailResponse body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.type()).isEqualTo("https://atlasops/errors/resource-not-found");
    assertThat(body.code()).isEqualTo("RESOURCE_NOT_FOUND");
    assertThat(body.detail()).isEqualTo("Customer not found");
    assertThat(body.traceId()).isEqualTo(TRACE_ID);
  }

  @Test
  void should_returnConflict_when_duplicateResource() {
    DuplicateResourceException ex = new DuplicateResourceException("Email already registered");

    ResponseEntity<ProblemDetailResponse> response = handler.handleDuplicateResource(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    ProblemDetailResponse body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.type()).isEqualTo("https://atlasops/errors/duplicate-resource");
    assertThat(body.code()).isEqualTo("DUPLICATE_RESOURCE");
    assertThat(body.detail()).isEqualTo("Email already registered");
    assertThat(body.traceId()).isEqualTo(TRACE_ID);
  }

  @Test
  void should_returnForbidden_when_forbiddenAction() {
    ForbiddenActionException ex = new ForbiddenActionException("Insufficient permissions");

    ResponseEntity<ProblemDetailResponse> response = handler.handleForbiddenAction(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    ProblemDetailResponse body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.type()).isEqualTo("https://atlasops/errors/forbidden-action");
    assertThat(body.code()).isEqualTo("FORBIDDEN_ACTION");
    assertThat(body.detail()).isEqualTo("Insufficient permissions");
    assertThat(body.traceId()).isEqualTo(TRACE_ID);
  }

  @Test
  void should_returnUnauthorized_when_unauthorized() {
    UnauthorizedException ex = new UnauthorizedException("Token expired");

    ResponseEntity<ProblemDetailResponse> response = handler.handleUnauthorized(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    ProblemDetailResponse body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.type()).isEqualTo("https://atlasops/errors/unauthorized");
    assertThat(body.code()).isEqualTo("UNAUTHORIZED");
    assertThat(body.detail()).isEqualTo("Token expired");
    assertThat(body.traceId()).isEqualTo(TRACE_ID);
  }

  @Test
  void should_returnUnprocessableEntity_when_businessRuleViolation() {
    BusinessRuleViolationException ex =
        new BusinessRuleViolationException("Cannot deactivate own account");

    ResponseEntity<ProblemDetailResponse> response = handler.handleBusinessRuleViolation(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    ProblemDetailResponse body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.type()).isEqualTo("https://atlasops/errors/business-rule-violation");
    assertThat(body.code()).isEqualTo("BUSINESS_RULE_VIOLATION");
    assertThat(body.detail()).isEqualTo("Cannot deactivate own account");
    assertThat(body.traceId()).isEqualTo(TRACE_ID);
  }

  @Test
  void should_returnInternalServerError_when_unexpectedException() {
    Exception ex = new RuntimeException("Unexpected database failure");

    ResponseEntity<ProblemDetailResponse> response = handler.handleGenericException(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    ProblemDetailResponse body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.type()).isEqualTo("https://atlasops/errors/internal-error");
    assertThat(body.code()).isEqualTo("INTERNAL_ERROR");
    assertThat(body.detail()).isEqualTo("An unexpected error occurred");
    assertThat(body.traceId()).isEqualTo(TRACE_ID);
  }

  @Test
  void should_returnUnknownTraceId_when_noCorrelationIdInMdc() {
    MDC.clear();

    ResourceNotFoundException ex = new ResourceNotFoundException("Not found");

    ResponseEntity<ProblemDetailResponse> response = handler.handleResourceNotFound(ex);

    ProblemDetailResponse body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.traceId()).isEqualTo("unknown");
  }
}
