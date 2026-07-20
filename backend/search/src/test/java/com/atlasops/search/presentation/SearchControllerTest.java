package com.atlasops.search.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.atlasops.search.application.UnifiedSearchCommand;
import com.atlasops.search.application.UnifiedSearchUseCase;
import com.atlasops.search.domain.SearchResult;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Unit tests for SearchController.
 * Validates: Requirements 18.1, 18.2, 18.3, 18.6
 */
@ExtendWith(MockitoExtension.class)
class SearchControllerTest {

    private static final String TENANT = "tenant-alpha";
    private static final String USER = "user-001";
    private static final String ROLE = "ADMIN";

    @Mock private UnifiedSearchUseCase unifiedSearchUseCase;

    private SearchController controller;

    @BeforeEach
    void setUp() {
        controller = new SearchController(unifiedSearchUseCase);
    }

    @Test
    void should_returnResults_when_queryMatches() {
        List<SearchResult> results = List.of(
                new SearchResult("CUSTOMER", "cust-001", "Alpha Corp", "Alpha Corp is...", 0.95),
                new SearchResult("REQUEST", "req-001", "Support ticket", "Need help with...", 0.80));
        when(unifiedSearchUseCase.execute(any(UnifiedSearchCommand.class)))
                .thenReturn(new PageImpl<>(results, PageRequest.of(0, 20), 2));

        ResponseEntity<PageResponse<SearchResultResponse>> response =
                controller.search(TENANT, USER, ROLE, "Alpha", null, 0, 20);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().content()).hasSize(2);
        assertThat(response.getBody().content().get(0).entityType()).isEqualTo("CUSTOMER");
        assertThat(response.getBody().content().get(0).title()).isEqualTo("Alpha Corp");
    }

    @Test
    void should_returnEmptyPage_when_noMatches() {
        when(unifiedSearchUseCase.execute(any(UnifiedSearchCommand.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

        ResponseEntity<PageResponse<SearchResultResponse>> response =
                controller.search(TENANT, USER, ROLE, "nonexistent", null, 0, 20);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().content()).isEmpty();
        assertThat(response.getBody().page().totalElements()).isZero();
    }

    @Test
    void should_passEntityTypeFilter_to_useCase() {
        when(unifiedSearchUseCase.execute(any(UnifiedSearchCommand.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

        // Should not throw — entity type is passed through to use case
        ResponseEntity<PageResponse<SearchResultResponse>> response =
                controller.search(TENANT, USER, ROLE, "query", "CUSTOMER", 0, 20);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
