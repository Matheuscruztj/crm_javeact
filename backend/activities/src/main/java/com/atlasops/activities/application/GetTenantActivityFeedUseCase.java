package com.atlasops.activities.application;

import com.atlasops.activities.domain.Activity;
import com.atlasops.activities.domain.ports.ActivityRepository;
import com.atlasops.activities.domain.ports.UserCustomerResolverPort;
import java.util.List;
import java.util.Objects;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

/**
 * Use case for querying the global tenant activity feed. ADMIN and ANALYST roles see all activities
 * for the tenant. CLIENT role is restricted to activities whose entities belong to the user's
 * associated customers.
 */
@Service
public class GetTenantActivityFeedUseCase {

  private static final int DEFAULT_PAGE_SIZE = 20;
  private static final int MAX_PAGE_SIZE = 100;

  private final ActivityRepository activityRepository;
  private final UserCustomerResolverPort userCustomerResolverPort;

  public GetTenantActivityFeedUseCase(
      ActivityRepository activityRepository, UserCustomerResolverPort userCustomerResolverPort) {
    this.activityRepository = activityRepository;
    this.userCustomerResolverPort = userCustomerResolverPort;
  }

  /**
   * Queries the tenant activity feed with role-based filtering.
   *
   * @param query the feed query parameters
   * @return a page of activities ordered by timestamp descending
   */
  public Page<Activity> execute(FeedQuery query) {
    Objects.requireNonNull(query, "FeedQuery must not be null");
    Objects.requireNonNull(query.tenantId(), "TenantId must not be null");
    Objects.requireNonNull(query.userId(), "UserId must not be null");
    Objects.requireNonNull(query.role(), "Role must not be null");

    if (query.tenantId().isBlank()) {
      throw new IllegalArgumentException("TenantId must not be blank");
    }
    if (query.userId().isBlank()) {
      throw new IllegalArgumentException("UserId must not be blank");
    }
    if (query.role().isBlank()) {
      throw new IllegalArgumentException("Role must not be blank");
    }

    int page = query.page() < 0 ? 0 : query.page();
    int size = query.size();
    if (size < 1) {
      size = DEFAULT_PAGE_SIZE;
    }
    if (size > MAX_PAGE_SIZE) {
      size = MAX_PAGE_SIZE;
    }

    PageRequest pageable = PageRequest.of(page, size);

    if ("CLIENT".equalsIgnoreCase(query.role())) {
      List<String> customerIds =
          userCustomerResolverPort.findCustomerIdsByUserId(query.userId(), query.tenantId());

      if (customerIds.isEmpty()) {
        return Page.empty(pageable);
      }

      return activityRepository.findByTenantIdAndEntityIds(query.tenantId(), customerIds, pageable);
    }

    return activityRepository.findByTenantId(query.tenantId(), pageable);
  }

  /**
   * Query parameters for the tenant activity feed.
   *
   * @param tenantId the tenant identifier
   * @param userId the authenticated user identifier
   * @param role the authenticated user's role (ADMIN, ANALYST, CLIENT)
   * @param page zero-based page number
   * @param size page size (min 1, default 20, max 100)
   */
  public record FeedQuery(String tenantId, String userId, String role, int page, int size) {

    public FeedQuery {
      Objects.requireNonNull(tenantId, "TenantId must not be null");
      Objects.requireNonNull(userId, "UserId must not be null");
      Objects.requireNonNull(role, "Role must not be null");
    }
  }
}
