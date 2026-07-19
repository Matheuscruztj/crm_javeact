package com.atlasops.requests.presentation;

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
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for service request management operations.
 *
 * <p>Endpoints:
 *
 * <ul>
 *   <li>POST /api/v1/requests — creates a new request
 *   <li>GET /api/v1/requests — lists requests with pagination and filters
 *   <li>GET /api/v1/requests/{id} — retrieves a request by ID
 *   <li>POST /api/v1/requests/{id}/transition — transitions request status
 *   <li>POST /api/v1/requests/{id}/assign — assigns an analyst
 *   <li>POST /api/v1/requests/{id}/comments — adds a comment
 *   <li>GET /api/v1/requests/{id}/comments — lists comments
 * </ul>
 *
 * <p>Validates: Requirements 8.1, 8.5, 8.6, 8.9
 */
@RestController
@RequestMapping("/api/v1/requests")
public class RequestController {

  private final CreateRequestUseCase createRequestUseCase;
  private final GetRequestUseCase getRequestUseCase;
  private final ListRequestsUseCase listRequestsUseCase;
  private final TransitionRequestStatusUseCase transitionRequestStatusUseCase;
  private final AssignAnalystUseCase assignAnalystUseCase;
  private final AddCommentUseCase addCommentUseCase;
  private final ListCommentsUseCase listCommentsUseCase;

  public RequestController(
      CreateRequestUseCase createRequestUseCase,
      GetRequestUseCase getRequestUseCase,
      ListRequestsUseCase listRequestsUseCase,
      TransitionRequestStatusUseCase transitionRequestStatusUseCase,
      AssignAnalystUseCase assignAnalystUseCase,
      AddCommentUseCase addCommentUseCase,
      ListCommentsUseCase listCommentsUseCase) {
    this.createRequestUseCase = createRequestUseCase;
    this.getRequestUseCase = getRequestUseCase;
    this.listRequestsUseCase = listRequestsUseCase;
    this.transitionRequestStatusUseCase = transitionRequestStatusUseCase;
    this.assignAnalystUseCase = assignAnalystUseCase;
    this.addCommentUseCase = addCommentUseCase;
    this.listCommentsUseCase = listCommentsUseCase;
  }

  /**
   * Creates a new service request.
   *
   * @param tenantId the tenant identifier from header
   * @param request the create request body
   * @return 201 Created with the request representation and Location header
   */
  @PostMapping
  public ResponseEntity<ServiceRequestResponse> create(
      @RequestHeader("X-Tenant-ID") String tenantId,
      @Valid @RequestBody CreateRequestRequest request) {

    RequestPriority priority =
        request.priority() != null ? RequestPriority.valueOf(request.priority()) : null;

    var command =
        new CreateRequestUseCase.CreateRequestCommand(
            request.title(), request.description(), priority, request.customerId(), tenantId);

    ServiceRequest created = createRequestUseCase.execute(command);
    ServiceRequestResponse response = ServiceRequestResponse.from(created);
    URI location = URI.create("/api/v1/requests/" + created.getId());
    return ResponseEntity.created(location).body(response);
  }

  /**
   * Lists service requests with pagination and optional filters.
   *
   * @param tenantId the tenant identifier from header
   * @param status optional status filter
   * @param priority optional priority filter
   * @param customerId optional customer filter
   * @param page page number (zero-based, defaults to 0)
   * @param size page size (defaults to 20, max 100)
   * @return 200 OK with paginated response
   */
  @GetMapping
  public ResponseEntity<PageResponse<ServiceRequestResponse>> list(
      @RequestHeader("X-Tenant-ID") String tenantId,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String priority,
      @RequestParam(required = false) String customerId,
      @RequestParam(defaultValue = "0") Integer page,
      @RequestParam(defaultValue = "20") Integer size) {

    RequestStatus statusFilter = status != null ? RequestStatus.valueOf(status) : null;
    RequestPriority priorityFilter = priority != null ? RequestPriority.valueOf(priority) : null;

    var query =
        new ListRequestsUseCase.ListRequestsQuery(
            tenantId, statusFilter, priorityFilter, customerId, page, size);

    ServiceRequestPageResult result = listRequestsUseCase.execute(query);

    List<ServiceRequestResponse> content =
        result.content().stream().map(ServiceRequestResponse::from).toList();

    var pageMetadata =
        new PageResponse.PageMetadata(
            result.pageNumber(), result.pageSize(),
            result.totalElements(), result.totalPages());

    return ResponseEntity.ok(new PageResponse<>(content, pageMetadata));
  }

  /**
   * Retrieves a service request by its identifier.
   *
   * @param tenantId the tenant identifier from header
   * @param id the request identifier
   * @return 200 OK with the request representation
   */
  @GetMapping("/{id}")
  public ResponseEntity<ServiceRequestResponse> getById(
      @RequestHeader("X-Tenant-ID") String tenantId, @PathVariable String id) {
    ServiceRequest request = getRequestUseCase.execute(id, tenantId);
    return ResponseEntity.ok(ServiceRequestResponse.from(request));
  }

  /**
   * Transitions a service request to a new status.
   *
   * @param tenantId the tenant identifier from header
   * @param id the request identifier
   * @param request the transition request body
   * @return 200 OK with the updated request representation
   */
  @PostMapping("/{id}/transition")
  public ResponseEntity<ServiceRequestResponse> transition(
      @RequestHeader("X-Tenant-ID") String tenantId,
      @PathVariable String id,
      @Valid @RequestBody TransitionStatusRequest request) {

    RequestStatus targetStatus = RequestStatus.valueOf(request.targetStatus());

    var command = new TransitionRequestStatusUseCase.TransitionCommand(id, targetStatus, tenantId);

    ServiceRequest updated = transitionRequestStatusUseCase.execute(command);
    return ResponseEntity.ok(ServiceRequestResponse.from(updated));
  }

  /**
   * Assigns an analyst to a service request.
   *
   * @param tenantId the tenant identifier from header
   * @param id the request identifier
   * @param request the assign analyst request body
   * @return 200 OK with the updated request representation
   */
  @PostMapping("/{id}/assign")
  public ResponseEntity<ServiceRequestResponse> assign(
      @RequestHeader("X-Tenant-ID") String tenantId,
      @PathVariable String id,
      @Valid @RequestBody AssignAnalystRequest request) {

    var command = new AssignAnalystUseCase.AssignAnalystCommand(id, request.analystId(), tenantId);

    ServiceRequest updated = assignAnalystUseCase.execute(command);
    return ResponseEntity.ok(ServiceRequestResponse.from(updated));
  }

  /**
   * Adds a comment to a service request.
   *
   * @param tenantId the tenant identifier from header
   * @param id the request identifier
   * @param request the add comment request body
   * @return 201 Created with the comment representation
   */
  @PostMapping("/{id}/comments")
  public ResponseEntity<CommentResponse> addComment(
      @RequestHeader("X-Tenant-ID") String tenantId,
      @PathVariable String id,
      @Valid @RequestBody AddCommentRequest request) {

    var command =
        new AddCommentUseCase.AddCommentCommand(id, request.text(), request.authorId(), tenantId);

    Comment comment = addCommentUseCase.execute(command);
    CommentResponse response = CommentResponse.from(comment);
    URI location = URI.create("/api/v1/requests/" + id + "/comments/" + comment.getId());
    return ResponseEntity.created(location).body(response);
  }

  /**
   * Lists all comments for a service request.
   *
   * @param tenantId the tenant identifier from header
   * @param id the request identifier
   * @return 200 OK with the list of comments
   */
  @GetMapping("/{id}/comments")
  public ResponseEntity<List<CommentResponse>> listComments(
      @RequestHeader("X-Tenant-ID") String tenantId, @PathVariable String id) {

    List<Comment> comments = listCommentsUseCase.execute(id, tenantId);
    List<CommentResponse> response = comments.stream().map(CommentResponse::from).toList();

    return ResponseEntity.ok(response);
  }
}
