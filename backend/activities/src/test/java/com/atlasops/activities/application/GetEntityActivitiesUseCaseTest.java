package com.atlasops.activities.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlasops.activities.domain.Activity;
import com.atlasops.activities.domain.ports.ActivityRepository;
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
@DisplayName("GetEntityActivitiesUseCase")
class GetEntityActivitiesUseCaseTest {

  @Mock private ActivityRepository activityRepository;

  private GetEntityActivitiesUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new GetEntityActivitiesUseCase(activityRepository);
  }

  @Test
  void should_returnActivities_when_entityExists() {
    // Arrange
    Activity activity =
        Activity.create(
            "act-1",
            "CUSTOMER",
            "cust-123",
            "CREATED",
            "user-001",
            "tenant-alpha",
            "Customer created",
            "evt-1",
            Instant.parse("2025-01-15T10:00:00Z"));

    PageRequest expectedPageable = PageRequest.of(0, 20);
    when(activityRepository.findByEntityAndTenantId(
            "CUSTOMER", "cust-123", "tenant-alpha", expectedPageable))
        .thenReturn(new PageImpl<>(List.of(activity), expectedPageable, 1));

    // Act
    Page<Activity> result = useCase.execute("CUSTOMER", "cust-123", "tenant-alpha", 0, 20);

    // Assert
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getEntityId()).isEqualTo("cust-123");
  }

  @Test
  void should_useDefaultPageSize_when_sizeIsLessThanOne() {
    // Arrange
    ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
    when(activityRepository.findByEntityAndTenantId(
            eq("CUSTOMER"), eq("cust-123"), eq("tenant-alpha"), pageableCaptor.capture()))
        .thenReturn(Page.empty());

    // Act
    useCase.execute("CUSTOMER", "cust-123", "tenant-alpha", 0, 0);

    // Assert
    assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(20);
  }

  @Test
  void should_capPageSizeAtMax_when_sizeExceedsHundred() {
    // Arrange
    ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
    when(activityRepository.findByEntityAndTenantId(
            eq("CUSTOMER"), eq("cust-123"), eq("tenant-alpha"), pageableCaptor.capture()))
        .thenReturn(Page.empty());

    // Act
    useCase.execute("CUSTOMER", "cust-123", "tenant-alpha", 0, 200);

    // Assert
    assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(100);
  }

  @Test
  void should_resetPageToZero_when_pageIsNegative() {
    // Arrange
    ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
    when(activityRepository.findByEntityAndTenantId(
            eq("CUSTOMER"), eq("cust-123"), eq("tenant-alpha"), pageableCaptor.capture()))
        .thenReturn(Page.empty());

    // Act
    useCase.execute("CUSTOMER", "cust-123", "tenant-alpha", -1, 20);

    // Assert
    assertThat(pageableCaptor.getValue().getPageNumber()).isEqualTo(0);
  }

  @Test
  void should_throwException_when_entityTypeIsNull() {
    assertThatThrownBy(() -> useCase.execute(null, "cust-123", "tenant-alpha", 0, 20))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("EntityType must not be null");
  }

  @Test
  void should_throwException_when_entityTypeIsBlank() {
    assertThatThrownBy(() -> useCase.execute("  ", "cust-123", "tenant-alpha", 0, 20))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("EntityType must not be blank");
  }

  @Test
  void should_throwException_when_tenantIdIsNull() {
    assertThatThrownBy(() -> useCase.execute("CUSTOMER", "cust-123", null, 0, 20))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("TenantId must not be null");
  }

  @Test
  void should_delegateToRepository_when_validParametersProvided() {
    // Arrange
    when(activityRepository.findByEntityAndTenantId(
            "REQUEST", "req-456", "tenant-beta", PageRequest.of(2, 50)))
        .thenReturn(Page.empty());

    // Act
    useCase.execute("REQUEST", "req-456", "tenant-beta", 2, 50);

    // Assert
    verify(activityRepository)
        .findByEntityAndTenantId("REQUEST", "req-456", "tenant-beta", PageRequest.of(2, 50));
  }
}
