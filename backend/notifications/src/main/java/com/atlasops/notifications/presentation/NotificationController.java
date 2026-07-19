package com.atlasops.notifications.presentation;

import com.atlasops.notifications.application.GetUnreadCountQuery;
import com.atlasops.notifications.application.GetUnreadCountUseCase;
import com.atlasops.notifications.application.ListNotificationsQuery;
import com.atlasops.notifications.application.ListNotificationsUseCase;
import com.atlasops.notifications.application.MarkNotificationsReadCommand;
import com.atlasops.notifications.application.MarkNotificationsReadUseCase;
import com.atlasops.notifications.domain.Notification;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for notification management operations.
 *
 * <p>Endpoints:
 *
 * <ul>
 *   <li>GET /api/v1/notifications — list notifications (paginated, ordered by createdAt desc)
 *   <li>PATCH /api/v1/notifications/mark-read — mark notifications as read (single/bulk)
 *   <li>GET /api/v1/notifications/unread-count — get unread notification count
 * </ul>
 *
 * <p>Validates: Requirements 15.4, 15.5, 15.7, 15.8
 */
@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

  private final ListNotificationsUseCase listNotificationsUseCase;
  private final MarkNotificationsReadUseCase markNotificationsReadUseCase;
  private final GetUnreadCountUseCase getUnreadCountUseCase;

  public NotificationController(
      ListNotificationsUseCase listNotificationsUseCase,
      MarkNotificationsReadUseCase markNotificationsReadUseCase,
      GetUnreadCountUseCase getUnreadCountUseCase) {
    this.listNotificationsUseCase = listNotificationsUseCase;
    this.markNotificationsReadUseCase = markNotificationsReadUseCase;
    this.getUnreadCountUseCase = getUnreadCountUseCase;
  }

  /**
   * Lists notifications for the authenticated user with pagination.
   *
   * @param tenantId the tenant identifier from header
   * @param userId the authenticated user identifier
   * @param page page number (zero-based, defaults to 0)
   * @param size page size (defaults to 20, max 100)
   * @return 200 OK with paginated response ordered by createdAt descending
   */
  @GetMapping
  public ResponseEntity<PageResponse<NotificationResponse>> list(
      @RequestHeader("X-Tenant-ID") String tenantId,
      @RequestHeader("X-User-ID") String userId,
      @RequestParam(defaultValue = "0") Integer page,
      @RequestParam(defaultValue = "20") Integer size) {

    var query = new ListNotificationsQuery(userId, tenantId, page, size);
    Page<Notification> result = listNotificationsUseCase.execute(query);

    List<NotificationResponse> content =
        result.getContent().stream().map(NotificationResponse::from).toList();

    var pageMetadata =
        new PageResponse.PageMetadata(
            result.getNumber(),
            result.getSize(),
            result.getTotalElements(),
            result.getTotalPages());

    return ResponseEntity.ok(new PageResponse<>(content, pageMetadata));
  }

  /**
   * Marks notifications as read (single or bulk).
   *
   * @param tenantId the tenant identifier from header
   * @param userId the authenticated user identifier
   * @param request the request containing notification IDs to mark as read
   * @return 200 OK with the list of IDs that were marked as read
   */
  @PatchMapping("/mark-read")
  public ResponseEntity<MarkReadResponse> markAsRead(
      @RequestHeader("X-Tenant-ID") String tenantId,
      @RequestHeader("X-User-ID") String userId,
      @Valid @RequestBody MarkNotificationsReadRequest request) {

    var command = new MarkNotificationsReadCommand(request.ids(), userId, tenantId);
    List<String> markedIds = markNotificationsReadUseCase.execute(command);

    return ResponseEntity.ok(new MarkReadResponse(markedIds));
  }

  /**
   * Returns the count of unread notifications for the authenticated user.
   *
   * @param tenantId the tenant identifier from header
   * @param userId the authenticated user identifier
   * @return 200 OK with the unread count
   */
  @GetMapping("/unread-count")
  public ResponseEntity<UnreadCountResponse> getUnreadCount(
      @RequestHeader("X-Tenant-ID") String tenantId, @RequestHeader("X-User-ID") String userId) {

    var query = new GetUnreadCountQuery(userId, tenantId);
    long count = getUnreadCountUseCase.execute(query);

    return ResponseEntity.ok(new UnreadCountResponse(count));
  }

  /**
   * Response for the mark-read operation.
   *
   * @param markedIds the list of notification IDs that were marked as read
   */
  public record MarkReadResponse(List<String> markedIds) {}

  /**
   * Response for the unread count query.
   *
   * @param count the number of unread notifications
   */
  public record UnreadCountResponse(long count) {}
}
