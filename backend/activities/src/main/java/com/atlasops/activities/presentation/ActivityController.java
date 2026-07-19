package com.atlasops.activities.presentation;

import com.atlasops.activities.application.GetEntityActivitiesUseCase;
import com.atlasops.activities.application.GetTenantActivityFeedUseCase;
import com.atlasops.activities.application.GetTenantActivityFeedUseCase.FeedQuery;
import com.atlasops.activities.domain.Activity;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for activity feed operations.
 *
 * <p>Endpoints:
 *
 * <ul>
 *   <li>GET /api/v1/activities — global activity feed for the tenant (role-filtered)
 *   <li>GET /api/v1/activities?entityType=...&entityId=... — activities for a specific entity
 * </ul>
 *
 * <p>Validates: Requirements 14.2, 14.3, 14.4, 14.5
 */
@RestController
@RequestMapping("/api/v1/activities")
public class ActivityController {

  private final GetTenantActivityFeedUseCase getTenantActivityFeedUseCase;
  private final GetEntityActivitiesUseCase getEntityActivitiesUseCase;

  public ActivityController(
      GetTenantActivityFeedUseCase getTenantActivityFeedUseCase,
      GetEntityActivitiesUseCase getEntityActivitiesUseCase) {
    this.getTenantActivityFeedUseCase = getTenantActivityFeedUseCase;
    this.getEntityActivitiesUseCase = getEntityActivitiesUseCase;
  }

  /**
   * Lists activities for the tenant or a specific entity.
   *
   * <p>If entityType and entityId are provided, returns activities for that entity. Otherwise,
   * returns the global tenant activity feed filtered by the user's role.
   *
   * @param tenantId the tenant identifier from header
   * @param userId the authenticated user identifier
   * @param role the user's role (ADMIN, ANALYST, or CLIENT)
   * @param entityType optional entity type filter
   * @param entityId optional entity identifier filter
   * @param page page number (zero-based, defaults to 0)
   * @param size page size (defaults to 20, max 100)
   * @return 200 OK with paginated response
   */
  @GetMapping
  public ResponseEntity<PageResponse<ActivityResponse>> list(
      @RequestHeader("X-Tenant-ID") String tenantId,
      @RequestHeader("X-User-ID") String userId,
      @RequestHeader("X-User-Role") String role,
      @RequestParam(required = false) String entityType,
      @RequestParam(required = false) String entityId,
      @RequestParam(defaultValue = "0") Integer page,
      @RequestParam(defaultValue = "20") Integer size) {

    Page<Activity> result;

    if (entityType != null && entityId != null) {
      result = getEntityActivitiesUseCase.execute(entityType, entityId, tenantId, page, size);
    } else {
      var query = new FeedQuery(tenantId, userId, role, page, size);
      result = getTenantActivityFeedUseCase.execute(query);
    }

    List<ActivityResponse> content =
        result.getContent().stream().map(ActivityResponse::from).toList();

    var pageMetadata =
        new PageResponse.PageMetadata(
            result.getNumber(),
            result.getSize(),
            result.getTotalElements(),
            result.getTotalPages());

    return ResponseEntity.ok(new PageResponse<>(content, pageMetadata));
  }
}
