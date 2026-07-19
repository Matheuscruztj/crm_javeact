package com.atlasops.requests.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.atlasops.requests.application.AddCommentUseCase;
import com.atlasops.requests.application.AssignAnalystUseCase;
import com.atlasops.requests.application.CreateRequestUseCase;
import com.atlasops.requests.application.GetRequestUseCase;
import com.atlasops.requests.application.ListCommentsUseCase;
import com.atlasops.requests.application.ListRequestsUseCase;
import com.atlasops.requests.application.ServiceRequestPageResult;
import com.atlasops.requests.application.TransitionRequestStatusUseCase;
import com.atlasops.requests.domain.Comment;
import com.atlasops.requests.domain.RequestPriority;
import com.atlasops.requests.domain.RequestStatus;
import com.atlasops.requests.domain.ServiceRequest;
import com.atlasops.shared.domain.exceptions.BusinessRuleViolationException;
import com.atlasops.shared.domain.exceptions.ResourceNotFoundException;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@ExtendWith(MockitoExtension.class)
@DisplayName("RequestController")
class RequestControllerTest {

  private static final String TENANT_ID = "tenant-001";
  private static final String REQUEST_ID = "req-001";
  private static final String CUSTOMER_ID = "cust-001";
  private static final String ANALYST_ID = "user-analyst-001";
  private static final Instant FIXED_NOW = Instant.parse("2025-01-15T10:00:00Z");

  private MockMvc mockMvc;

  @Mock private CreateRequestUseCase createRequestUseCase;

  @Mock private GetRequestUseCase getRequestUseCase;

  @Mock private ListRequestsUseCase listRequestsUseCase;

  @Mock private TransitionRequestStatusUseCase transitionRequestStatusUseCase;

  @Mock private AssignAnalystUseCase assignAnalystUseCase;

  @Mock private AddCommentUseCase addCommentUseCase;

  @Mock private ListCommentsUseCase listCommentsUseCase;

  @InjectMocks private RequestController requestController;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(requestController)
            .setControllerAdvice(new TestExceptionHandler())
            .build();
  }

  @Test
  void should_createRequest_when_validDataProvided() throws Exception {
    ServiceRequest request = buildServiceRequest(RequestStatus.OPEN, null);
    when(createRequestUseCase.execute(any(CreateRequestUseCase.CreateRequestCommand.class)))
        .thenReturn(request);

    mockMvc
        .perform(
            post("/api/v1/requests")
                .header("X-Tenant-ID", TENANT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                {
                                    "title": "Fix login issue",
                                    "description": "Cannot login with valid credentials",
                                    "priority": "HIGH",
                                    "customerId": "cust-001"
                                }
                                """))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "/api/v1/requests/" + REQUEST_ID))
        .andExpect(jsonPath("$.id").value(REQUEST_ID))
        .andExpect(jsonPath("$.title").value("Fix login issue"))
        .andExpect(jsonPath("$.status").value("OPEN"))
        .andExpect(jsonPath("$.priority").value("MEDIUM"))
        .andExpect(jsonPath("$.customerId").value(CUSTOMER_ID))
        .andExpect(jsonPath("$.tenantId").value(TENANT_ID));
  }

  @Test
  void should_return400_when_titleIsBlank() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/requests")
                .header("X-Tenant-ID", TENANT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                {
                                    "title": "",
                                    "description": "Some description",
                                    "customerId": "cust-001"
                                }
                                """))
        .andExpect(status().isBadRequest());
  }

  @Test
  void should_return400_when_descriptionIsBlank() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/requests")
                .header("X-Tenant-ID", TENANT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                {
                                    "title": "Valid title",
                                    "description": "",
                                    "customerId": "cust-001"
                                }
                                """))
        .andExpect(status().isBadRequest());
  }

  @Test
  void should_returnRequest_when_validIdProvided() throws Exception {
    ServiceRequest request = buildServiceRequest(RequestStatus.OPEN, null);
    when(getRequestUseCase.execute(REQUEST_ID, TENANT_ID)).thenReturn(request);

    mockMvc
        .perform(get("/api/v1/requests/" + REQUEST_ID).header("X-Tenant-ID", TENANT_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(REQUEST_ID))
        .andExpect(jsonPath("$.title").value("Fix login issue"))
        .andExpect(jsonPath("$.status").value("OPEN"))
        .andExpect(jsonPath("$.createdAt").exists());
  }

  @Test
  void should_return404_when_requestNotFound() throws Exception {
    when(getRequestUseCase.execute("nonexistent", TENANT_ID))
        .thenThrow(new ResourceNotFoundException("Request with id 'nonexistent' not found"));

    mockMvc
        .perform(get("/api/v1/requests/nonexistent").header("X-Tenant-ID", TENANT_ID))
        .andExpect(status().isNotFound());
  }

  @Test
  void should_listRequests_when_paginationProvided() throws Exception {
    ServiceRequest request = buildServiceRequest(RequestStatus.OPEN, null);
    ServiceRequestPageResult page = new ServiceRequestPageResult(List.of(request), 0, 20, 1, 1);
    when(listRequestsUseCase.execute(any(ListRequestsUseCase.ListRequestsQuery.class)))
        .thenReturn(page);

    mockMvc
        .perform(
            get("/api/v1/requests")
                .header("X-Tenant-ID", TENANT_ID)
                .param("page", "0")
                .param("size", "20"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content[0].id").value(REQUEST_ID))
        .andExpect(jsonPath("$.page.number").value(0))
        .andExpect(jsonPath("$.page.size").value(20))
        .andExpect(jsonPath("$.page.totalElements").value(1))
        .andExpect(jsonPath("$.page.totalPages").value(1));
  }

  @Test
  void should_listRequests_when_statusFilterApplied() throws Exception {
    ServiceRequestPageResult page = new ServiceRequestPageResult(List.of(), 0, 20, 0, 0);
    when(listRequestsUseCase.execute(any(ListRequestsUseCase.ListRequestsQuery.class)))
        .thenReturn(page);

    mockMvc
        .perform(
            get("/api/v1/requests").header("X-Tenant-ID", TENANT_ID).param("status", "IN_PROGRESS"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content").isEmpty());
  }

  @Test
  void should_transitionStatus_when_validTransition() throws Exception {
    ServiceRequest request = buildServiceRequest(RequestStatus.IN_PROGRESS, null);
    when(transitionRequestStatusUseCase.execute(
            any(TransitionRequestStatusUseCase.TransitionCommand.class)))
        .thenReturn(request);

    mockMvc
        .perform(
            post("/api/v1/requests/" + REQUEST_ID + "/transition")
                .header("X-Tenant-ID", TENANT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                {"targetStatus": "IN_PROGRESS"}
                                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(REQUEST_ID))
        .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
  }

  @Test
  void should_return422_when_invalidTransition() throws Exception {
    when(transitionRequestStatusUseCase.execute(
            any(TransitionRequestStatusUseCase.TransitionCommand.class)))
        .thenThrow(new BusinessRuleViolationException("Cannot transition from OPEN to COMPLETED"));

    mockMvc
        .perform(
            post("/api/v1/requests/" + REQUEST_ID + "/transition")
                .header("X-Tenant-ID", TENANT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                {"targetStatus": "COMPLETED"}
                                """))
        .andExpect(status().isUnprocessableEntity());
  }

  @Test
  void should_assignAnalyst_when_requestInOpenStatus() throws Exception {
    ServiceRequest request = buildServiceRequest(RequestStatus.IN_PROGRESS, ANALYST_ID);
    when(assignAnalystUseCase.execute(any(AssignAnalystUseCase.AssignAnalystCommand.class)))
        .thenReturn(request);

    mockMvc
        .perform(
            post("/api/v1/requests/" + REQUEST_ID + "/assign")
                .header("X-Tenant-ID", TENANT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                {"analystId": "user-analyst-001"}
                                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(REQUEST_ID))
        .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
        .andExpect(jsonPath("$.assignedAnalystId").value(ANALYST_ID));
  }

  @Test
  void should_return422_when_assigningToNonOpenRequest() throws Exception {
    when(assignAnalystUseCase.execute(any(AssignAnalystUseCase.AssignAnalystCommand.class)))
        .thenThrow(
            new BusinessRuleViolationException(
                "Can only assign analyst to a request in OPEN status"));

    mockMvc
        .perform(
            post("/api/v1/requests/" + REQUEST_ID + "/assign")
                .header("X-Tenant-ID", TENANT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                {"analystId": "user-analyst-001"}
                                """))
        .andExpect(status().isUnprocessableEntity());
  }

  @Test
  void should_addComment_when_validTextProvided() throws Exception {
    Comment comment =
        Comment.reconstitute("comment-001", "This is a comment", "user-001", REQUEST_ID, FIXED_NOW);
    when(addCommentUseCase.execute(any(AddCommentUseCase.AddCommentCommand.class)))
        .thenReturn(comment);

    mockMvc
        .perform(
            post("/api/v1/requests/" + REQUEST_ID + "/comments")
                .header("X-Tenant-ID", TENANT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                {"text": "This is a comment", "authorId": "user-001"}
                                """))
        .andExpect(status().isCreated())
        .andExpect(header().exists("Location"))
        .andExpect(jsonPath("$.id").value("comment-001"))
        .andExpect(jsonPath("$.text").value("This is a comment"))
        .andExpect(jsonPath("$.authorId").value("user-001"))
        .andExpect(jsonPath("$.requestId").value(REQUEST_ID));
  }

  @Test
  void should_return400_when_commentTextIsBlank() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/requests/" + REQUEST_ID + "/comments")
                .header("X-Tenant-ID", TENANT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                {"text": "", "authorId": "user-001"}
                                """))
        .andExpect(status().isBadRequest());
  }

  @Test
  void should_listComments_when_requestExists() throws Exception {
    Comment comment1 =
        Comment.reconstitute("comment-001", "First comment", "user-001", REQUEST_ID, FIXED_NOW);
    Comment comment2 =
        Comment.reconstitute(
            "comment-002", "Second comment", "user-002", REQUEST_ID, FIXED_NOW.plusSeconds(60));
    when(listCommentsUseCase.execute(REQUEST_ID, TENANT_ID))
        .thenReturn(List.of(comment1, comment2));

    mockMvc
        .perform(
            get("/api/v1/requests/" + REQUEST_ID + "/comments").header("X-Tenant-ID", TENANT_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0].id").value("comment-001"))
        .andExpect(jsonPath("$[0].text").value("First comment"))
        .andExpect(jsonPath("$[1].id").value("comment-002"))
        .andExpect(jsonPath("$[1].text").value("Second comment"));
  }

  @Test
  void should_return404_when_listingCommentsForNonexistentRequest() throws Exception {
    when(listCommentsUseCase.execute("nonexistent", TENANT_ID))
        .thenThrow(new ResourceNotFoundException("Request with id 'nonexistent' not found"));

    mockMvc
        .perform(get("/api/v1/requests/nonexistent/comments").header("X-Tenant-ID", TENANT_ID))
        .andExpect(status().isNotFound());
  }

  private ServiceRequest buildServiceRequest(RequestStatus status, String analystId) {
    return ServiceRequest.reconstitute(
        REQUEST_ID,
        "Fix login issue",
        "Cannot login with valid credentials",
        status,
        RequestPriority.MEDIUM,
        CUSTOMER_ID,
        analystId,
        TENANT_ID,
        FIXED_NOW,
        analystId != null ? FIXED_NOW : null,
        List.of());
  }

  /** Minimal exception handler for standalone MockMvc tests. */
  @RestControllerAdvice
  static class TestExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleNotFound(ResourceNotFoundException ex) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(java.util.Map.of("status", 404, "detail", ex.getMessage()));
    }

    @ExceptionHandler(BusinessRuleViolationException.class)
    public ResponseEntity<Object> handleBusinessRule(BusinessRuleViolationException ex) {
      return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
          .body(java.util.Map.of("status", 422, "detail", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleBadRequest(IllegalArgumentException ex) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(java.util.Map.of("status", 400, "detail", ex.getMessage()));
    }

    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidation(
        org.springframework.web.bind.MethodArgumentNotValidException ex) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(java.util.Map.of("status", 400, "detail", "Validation failed"));
    }
  }
}
