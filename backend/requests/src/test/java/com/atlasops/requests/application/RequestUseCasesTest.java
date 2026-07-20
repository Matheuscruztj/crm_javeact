package com.atlasops.requests.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlasops.requests.domain.Comment;
import com.atlasops.requests.domain.RequestPriority;
import com.atlasops.requests.domain.RequestStatus;
import com.atlasops.requests.domain.ServiceRequest;
import com.atlasops.requests.domain.ports.CommentRepository;
import com.atlasops.requests.domain.ports.ServiceRequestRepository;
import com.atlasops.requests.domain.ports.ServiceRequestRepository.ServiceRequestPage;
import com.atlasops.shared.domain.exceptions.BusinessRuleViolationException;
import com.atlasops.shared.domain.exceptions.ResourceNotFoundException;
import com.atlasops.shared.domain.ports.Clock;
import com.atlasops.shared.domain.ports.IdGenerator;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for requests use cases: GetRequest, ListRequests, AddComment, AssignAnalyst.
 */
@ExtendWith(MockitoExtension.class)
class RequestUseCasesTest {

  private static final Instant NOW = Instant.parse("2025-01-15T10:00:00Z");
  private static final String TENANT = "tenant-alpha";
  private static final String REQUEST_ID = "req-001";

  @Mock private ServiceRequestRepository requestRepository;
  @Mock private CommentRepository commentRepository;
  @Mock private IdGenerator idGenerator;
  @Mock private Clock clock;

  private ServiceRequest openRequest() {
    return ServiceRequest.create(REQUEST_ID, "Support Issue", "Details here.",
        RequestPriority.MEDIUM, "cust-001", TENANT, NOW);
  }

  // ─── GetRequestUseCase ────────────────────────────────────────────────────

  @Nested
  class GetRequest {
    GetRequestUseCase useCase;
    @BeforeEach void init() { useCase = new GetRequestUseCase(requestRepository); }

    @Test
    void should_returnRequest_when_found() {
      when(requestRepository.findByIdAndTenantId(REQUEST_ID, TENANT))
          .thenReturn(Optional.of(openRequest()));
      ServiceRequest result = useCase.execute(REQUEST_ID, TENANT);
      assertThat(result.getId()).isEqualTo(REQUEST_ID);
    }

    @Test
    void should_throwNotFound_when_missing() {
      when(requestRepository.findByIdAndTenantId(anyString(), anyString()))
          .thenReturn(Optional.empty());
      assertThatThrownBy(() -> useCase.execute("x", TENANT))
          .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void should_throwNullPointer_when_idIsNull() {
      assertThatThrownBy(() -> useCase.execute(null, TENANT))
          .isInstanceOf(NullPointerException.class);
    }
  }

  // ─── ListRequestsUseCase ──────────────────────────────────────────────────

  @Nested
  class ListRequests {
    ListRequestsUseCase useCase;
    @BeforeEach void init() { useCase = new ListRequestsUseCase(requestRepository); }

    @Test
    void should_returnPage_when_tenantHasRequests() {
      var domainPage = new ServiceRequestPage(List.of(openRequest()), 0, 20, 1L, 1);
      when(requestRepository.findAllByTenantId(TENANT, null, null, null, 0, 20))
          .thenReturn(domainPage);

      var result = useCase.execute(new ListRequestsUseCase.ListRequestsQuery(TENANT, null, null, null, 0, 20));
      assertThat(result.content()).hasSize(1);
    }

    @Test
    void should_capPageSizeAt100() {
      when(requestRepository.findAllByTenantId(anyString(), any(), any(), any(), any(int.class), any(int.class)))
          .thenReturn(new ServiceRequestPage(List.of(), 0, 100, 0L, 0));

      useCase.execute(new ListRequestsUseCase.ListRequestsQuery(TENANT, null, null, null, 0, 500));

      verify(requestRepository).findAllByTenantId(TENANT, null, null, null, 0, 100);
    }

    @Test
    void should_throwNullPointer_when_queryIsNull() {
      assertThatThrownBy(() -> useCase.execute(null))
          .isInstanceOf(NullPointerException.class);
    }
  }

  // ─── AddCommentUseCase ────────────────────────────────────────────────────

  @Nested
  class AddComment {
    AddCommentUseCase useCase;
    @BeforeEach void init() {
      useCase = new AddCommentUseCase(commentRepository, requestRepository, idGenerator, clock);
    }

    @Test
    void should_createComment_when_requestExists() {
      when(requestRepository.findByIdAndTenantId(REQUEST_ID, TENANT))
          .thenReturn(Optional.of(openRequest()));
      when(idGenerator.generate()).thenReturn("comment-001");
      when(clock.now()).thenReturn(NOW);
      when(commentRepository.save(any(Comment.class))).thenAnswer(i -> i.getArgument(0));

      var command = new AddCommentUseCase.AddCommentCommand(REQUEST_ID, "Great work!", "user-001", TENANT);
      Comment result = useCase.execute(command);

      assertThat(result.getId()).isEqualTo("comment-001");
      assertThat(result.getText()).isEqualTo("Great work!");
      assertThat(result.getRequestId()).isEqualTo(REQUEST_ID);
      verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void should_throwNotFound_when_requestMissing() {
      when(requestRepository.findByIdAndTenantId(anyString(), anyString()))
          .thenReturn(Optional.empty());

      var command = new AddCommentUseCase.AddCommentCommand("x", "Text", "user-001", TENANT);
      assertThatThrownBy(() -> useCase.execute(command))
          .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void should_throwNullPointer_when_commandIsNull() {
      assertThatThrownBy(() -> useCase.execute(null))
          .isInstanceOf(NullPointerException.class);
    }
  }

  // ─── AssignAnalystUseCase ─────────────────────────────────────────────────

  @Nested
  class AssignAnalyst {
    AssignAnalystUseCase useCase;
    @BeforeEach void init() { useCase = new AssignAnalystUseCase(requestRepository, clock); }

    @Test
    void should_assignAnalyst_when_requestIsOpen() {
      when(requestRepository.findByIdAndTenantId(REQUEST_ID, TENANT))
          .thenReturn(Optional.of(openRequest()));
      when(clock.now()).thenReturn(NOW.plusSeconds(60));
      when(requestRepository.save(any())).thenAnswer(i -> i.getArgument(0));

      var command = new AssignAnalystUseCase.AssignAnalystCommand(REQUEST_ID, "analyst-001", TENANT);
      ServiceRequest result = useCase.execute(command);

      assertThat(result.getAssignedAnalystId()).isEqualTo("analyst-001");
      assertThat(result.getStatus()).isEqualTo(RequestStatus.IN_PROGRESS);
    }

    @Test
    void should_throwNotFound_when_requestMissing() {
      when(requestRepository.findByIdAndTenantId(anyString(), anyString()))
          .thenReturn(Optional.empty());

      assertThatThrownBy(() -> useCase.execute(
          new AssignAnalystUseCase.AssignAnalystCommand("x", "analyst-001", TENANT)))
          .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void should_throwBusinessRule_when_requestNotOpen() {
      ServiceRequest req = openRequest();
      req.assignAnalyst("analyst-001", NOW); // already IN_PROGRESS
      when(requestRepository.findByIdAndTenantId(REQUEST_ID, TENANT))
          .thenReturn(Optional.of(req));
      when(clock.now()).thenReturn(NOW.plusSeconds(60));

      assertThatThrownBy(() -> useCase.execute(
          new AssignAnalystUseCase.AssignAnalystCommand(REQUEST_ID, "analyst-002", TENANT)))
          .isInstanceOf(BusinessRuleViolationException.class);
    }
  }
}
