package com.atlasops.requests.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.atlasops.requests.domain.Comment;
import com.atlasops.requests.domain.RequestPriority;
import com.atlasops.requests.domain.ServiceRequest;
import com.atlasops.requests.domain.ports.CommentRepository;
import com.atlasops.requests.domain.ports.ServiceRequestRepository;
import com.atlasops.shared.domain.exceptions.ResourceNotFoundException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListCommentsUseCaseTest {

  private static final Instant NOW = Instant.parse("2025-01-15T10:00:00Z");
  private static final String TENANT = "tenant-alpha";
  private static final String REQUEST_ID = "req-001";

  @Mock private CommentRepository commentRepository;
  @Mock private ServiceRequestRepository requestRepository;

  private ListCommentsUseCase useCase;

  @BeforeEach
  void setUp() { useCase = new ListCommentsUseCase(commentRepository, requestRepository); }

  private ServiceRequest openRequest() {
    return ServiceRequest.create(REQUEST_ID, "Request Title", "Desc",
        RequestPriority.MEDIUM, "cust-001", TENANT, NOW);
  }

  @Test
  void should_returnComments_when_requestExists() {
    when(requestRepository.findByIdAndTenantId(REQUEST_ID, TENANT))
        .thenReturn(Optional.of(openRequest()));

    Comment c1 = Comment.create("c-001", "First comment", "user-001", REQUEST_ID, NOW);
    Comment c2 = Comment.create("c-002", "Second comment", "user-002", REQUEST_ID, NOW.plusSeconds(30));
    when(commentRepository.findByRequestId(REQUEST_ID)).thenReturn(List.of(c1, c2));

    List<Comment> result = useCase.execute(REQUEST_ID, TENANT);

    assertThat(result).hasSize(2);
    assertThat(result.get(0).getText()).isEqualTo("First comment");
    assertThat(result.get(1).getText()).isEqualTo("Second comment");
  }

  @Test
  void should_returnEmpty_when_noComments() {
    when(requestRepository.findByIdAndTenantId(REQUEST_ID, TENANT))
        .thenReturn(Optional.of(openRequest()));
    when(commentRepository.findByRequestId(REQUEST_ID)).thenReturn(List.of());

    assertThat(useCase.execute(REQUEST_ID, TENANT)).isEmpty();
  }

  @Test
  void should_throwNotFound_when_requestMissing() {
    when(requestRepository.findByIdAndTenantId("x", TENANT)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> useCase.execute("x", TENANT))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void should_throwNullPointer_when_requestIdIsNull() {
    assertThatThrownBy(() -> useCase.execute(null, TENANT))
        .isInstanceOf(NullPointerException.class);
  }
}
