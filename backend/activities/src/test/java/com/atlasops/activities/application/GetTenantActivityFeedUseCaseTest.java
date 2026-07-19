package com.atlasops.activities.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlasops.activities.domain.Activity;
import com.atlasops.activities.domain.ports.ActivityRepository;
import com.atlasops.activities.domain.ports.UserCustomerResolverPort;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetTenantActivityFeedUseCase")
class GetTenantActivityFeedUseCaseTest {

  @Mock private ActivityRepository activityRepository;
  @Mock private UserCustomerResolverPort userCustomerResolverPort;

  private GetTenantActivityFeedUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new GetTenantActivityFeedUseCase(activityRepository, userCustomerResolverPort);
  }

  @Test
  void should_returnAllActivities_when_roleIsAdmin() {
    // Arrange
    var query =
        new GetTenantActivityFeedUseCase.FeedQuery("tenant-alpha", "user-admin", "ADMIN", 0, 20);

    Activity activity =
        Activity.create(
            "act-1",
            "CUSTOMER",
            "cust-1",
            "CREATED",
            "user-001",
            "tenant-alpha",
            "Customer created",
            "evt-1",
            Instant.parse("2025-01-15T10:00:00Z"));

    when(activityRepository.findByTenantId("tenant-alpha", PageRequest.of(0, 20)))
        .thenReturn(new PageImpl<>(List.of(activity), PageRequest.of(0, 20), 1));

    // Act
    Page<Activity> result = useCase.execute(query);

    // Assert
    assertThat(result.getContent()).hasSize(1);
    verify(activityRepository).findByTenantId("tenant-alpha", PageRequest.of(0, 20));
    verify(userCustomerResolverPort, never()).findCustomerIdsByUserId(any(), any());
  }

  @Test
  void should_returnAllActivities_when_roleIsAnalyst() {
    // Arrange
    var query =
        new GetTenantActivityFeedUseCase.FeedQuery(
            "tenant-alpha", "user-analyst", "ANALYST", 0, 20);

    when(activityRepository.findByTenantId("tenant-alpha", PageRequest.of(0, 20)))
        .thenReturn(Page.empty());

    // Act
    Page<Activity> result = useCase.execute(query);

    // Assert
    assertThat(result).isNotNull();
    verify(activityRepository).findByTenantId("tenant-alpha", PageRequest.of(0, 20));
    verify(userCustomerResolverPort, never()).findCustomerIdsByUserId(any(), any());
  }

  @Test
  void should_filterByCustomerEntities_when_roleIsClient() {
    // Arrange
    var query =
        new GetTenantActivityFeedUseCase.FeedQuery("tenant-alpha", "user-client", "CLIENT", 0, 20);

    List<String> customerIds = List.of("cust-1", "cust-2");
    when(userCustomerResolverPort.findCustomerIdsByUserId("user-client", "tenant-alpha"))
        .thenReturn(customerIds);

    Activity activity =
        Activity.create(
            "act-1",
            "CUSTOMER",
            "cust-1",
            "CREATED",
            "user-001",
            "tenant-alpha",
            "Customer created",
            "evt-1",
            Instant.parse("2025-01-15T10:00:00Z"));

    when(activityRepository.findByTenantIdAndEntityIds(
            "tenant-alpha", customerIds, PageRequest.of(0, 20)))
        .thenReturn(new PageImpl<>(List.of(activity), PageRequest.of(0, 20), 1));

    // Act
    Page<Activity> result = useCase.execute(query);

    // Assert
    assertThat(result.getContent()).hasSize(1);
    verify(userCustomerResolverPort).findCustomerIdsByUserId("user-client", "tenant-alpha");
    verify(activityRepository)
        .findByTenantIdAndEntityIds("tenant-alpha", customerIds, PageRequest.of(0, 20));
    verify(activityRepository, never()).findByTenantId(any(), any());
  }

  @Test
  void should_returnEmptyPage_when_clientHasNoAssociatedCustomers() {
    // Arrange
    var query =
        new GetTenantActivityFeedUseCase.FeedQuery("tenant-alpha", "user-client", "CLIENT", 0, 20);

    when(userCustomerResolverPort.findCustomerIdsByUserId("user-client", "tenant-alpha"))
        .thenReturn(List.of());

    // Act
    Page<Activity> result = useCase.execute(query);

    // Assert
    assertThat(result.getContent()).isEmpty();
    verify(activityRepository, never()).findByTenantId(any(), any());
    verify(activityRepository, never()).findByTenantIdAndEntityIds(any(), any(), any());
  }

  @Test
  void should_useDefaultPageSize_when_sizeIsLessThanOne() {
    // Arrange
    var query =
        new GetTenantActivityFeedUseCase.FeedQuery("tenant-alpha", "user-admin", "ADMIN", 0, 0);

    ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
    when(activityRepository.findByTenantId(eq("tenant-alpha"), pageableCaptor.capture()))
        .thenReturn(Page.empty());

    // Act
    useCase.execute(query);

    // Assert
    assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(20);
  }

  @Test
  void should_capPageSizeAtMax_when_sizeExceedsHundred() {
    // Arrange
    var query =
        new GetTenantActivityFeedUseCase.FeedQuery("tenant-alpha", "user-admin", "ADMIN", 0, 150);

    ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
    when(activityRepository.findByTenantId(eq("tenant-alpha"), pageableCaptor.capture()))
        .thenReturn(Page.empty());

    // Act
    useCase.execute(query);

    // Assert
    assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(100);
  }

  @Test
  void should_resetPageToZero_when_pageIsNegative() {
    // Arrange
    var query =
        new GetTenantActivityFeedUseCase.FeedQuery("tenant-alpha", "user-admin", "ADMIN", -1, 20);

    ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
    when(activityRepository.findByTenantId(eq("tenant-alpha"), pageableCaptor.capture()))
        .thenReturn(Page.empty());

    // Act
    useCase.execute(query);

    // Assert
    assertThat(pageableCaptor.getValue().getPageNumber()).isEqualTo(0);
  }

  @Test
  void should_throwException_when_queryIsNull() {
    assertThatThrownBy(() -> useCase.execute(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("FeedQuery must not be null");
  }

  @Test
  void should_throwException_when_tenantIdIsBlank() {
    assertThatThrownBy(
            () ->
                useCase.execute(
                    new GetTenantActivityFeedUseCase.FeedQuery("  ", "user-001", "ADMIN", 0, 20)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("TenantId must not be blank");
  }

  @Test
  void should_beCaseInsensitive_when_checkingClientRole() {
    // Arrange
    var query =
        new GetTenantActivityFeedUseCase.FeedQuery("tenant-alpha", "user-client", "client", 0, 20);

    when(userCustomerResolverPort.findCustomerIdsByUserId("user-client", "tenant-alpha"))
        .thenReturn(List.of("cust-1"));

    when(activityRepository.findByTenantIdAndEntityIds(
            eq("tenant-alpha"), eq(List.of("cust-1")), any()))
        .thenReturn(Page.empty());

    // Act
    useCase.execute(query);

    // Assert
    verify(userCustomerResolverPort).findCustomerIdsByUserId("user-client", "tenant-alpha");
  }
}
