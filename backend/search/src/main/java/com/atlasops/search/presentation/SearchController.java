package com.atlasops.search.presentation;

import com.atlasops.search.application.SearchResultView;
import com.atlasops.search.application.UnifiedSearchCommand;
import com.atlasops.search.application.UnifiedSearchUseCase;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for unified cross-entity search operations.
 *
 * <p>Endpoint:
 *
 * <ul>
 *   <li>GET /api/v1/search?q=...&page=...&size=... — unified search with pagination
 * </ul>
 *
 * <p>Validates: Requirements 18.1, 18.2, 18.3, 18.6
 */
@RestController
@RequestMapping("/api/v1/search")
public class SearchController {

  private final UnifiedSearchUseCase unifiedSearchUseCase;

  public SearchController(UnifiedSearchUseCase unifiedSearchUseCase) {
    this.unifiedSearchUseCase = unifiedSearchUseCase;
  }

  /**
   * Executes a unified search across entities with tenant and role-based filtering.
   *
   * @param tenantId the tenant identifier from header
   * @param userId the authenticated user identifier
   * @param role the user's role (ADMIN, ANALYST, or CLIENT)
   * @param q the search query (2-200 characters)
   * @param entityType optional filter by entity type (CUSTOMER, REQUEST, DOCUMENT)
   * @param page page number (zero-based, defaults to 0)
   * @param size page size (defaults to 20, max 50)
   * @return 200 OK with paginated search results
   */
  @GetMapping
  public ResponseEntity<PageResponse<SearchResultResponse>> search(
      @RequestHeader("X-Tenant-ID") String tenantId,
      @RequestHeader("X-User-ID") String userId,
      @RequestHeader("X-User-Role") String role,
      @RequestParam("q") String q,
      @RequestParam(required = false) String entityType,
      @RequestParam(defaultValue = "0") Integer page,
      @RequestParam(defaultValue = "20") Integer size) {

    var command = new UnifiedSearchCommand(q, entityType, tenantId, userId, role, page, size);

    Page<com.atlasops.search.domain.SearchResult> result = unifiedSearchUseCase.execute(command);

    List<SearchResultResponse> content =
        result.getContent().stream()
            .map(SearchResultView::from)
            .map(SearchResultResponse::from)
            .toList();

    var pageMetadata =
        new PageResponse.PageMetadata(
            result.getNumber(),
            result.getSize(),
            result.getTotalElements(),
            result.getTotalPages());

    return ResponseEntity.ok(new PageResponse<>(content, pageMetadata));
  }
}
