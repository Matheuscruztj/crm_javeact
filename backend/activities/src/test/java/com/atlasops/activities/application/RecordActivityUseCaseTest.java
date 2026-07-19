package com.atlasops.activities.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlasops.activities.domain.Activity;
import com.atlasops.activities.domain.ports.ActivityRepository;
import com.atlasops.shared.domain.ports.Clock;
import com.atlasops.shared.domain.ports.IdGenerator;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecordActivityUseCase")
class RecordActivityUseCaseTest {

  private static final Instant FIXED_NOW = Instant.parse("2025-01-15T10:00:00Z");
  private static final String GENERATED_ID = "activity-001";

  @Mock private ActivityRepository activityRepository;
  @Mock private IdGenerator idGenerator;
  @Mock private Clock clock;

  private RecordActivityUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new RecordActivityUseCase(activityRepository, idGenerator, clock);
  }

  @Test
  void should_persistActivity_when_eventIdIsNew() {
    // Arrange
    var command =
        new RecordActivityCommand(
            "CUSTOMER",
            "cust-123",
            "CREATED",
            "user-001",
            "tenant-alpha",
            "Customer created",
            "evt-001");

    when(activityRepository.existsByEventId("evt-001")).thenReturn(false);
    when(idGenerator.generate()).thenReturn(GENERATED_ID);
    when(clock.now()).thenReturn(FIXED_NOW);
    when(activityRepository.save(any(Activity.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    Optional<Activity> result = useCase.execute(command);

    // Assert
    assertThat(result).isPresent();
    Activity activity = result.get();
    assertThat(activity.getId()).isEqualTo(GENERATED_ID);
    assertThat(activity.getEntityType()).isEqualTo("CUSTOMER");
    assertThat(activity.getEntityId()).isEqualTo("cust-123");
    assertThat(activity.getActionType()).isEqualTo("CREATED");
    assertThat(activity.getActorId()).isEqualTo("user-001");
    assertThat(activity.getTenantId()).isEqualTo("tenant-alpha");
    assertThat(activity.getSummary()).isEqualTo("Customer created");
    assertThat(activity.getEventId()).isEqualTo("evt-001");
    assertThat(activity.getTimestamp()).isEqualTo(FIXED_NOW);

    verify(activityRepository).save(any(Activity.class));
  }

  @Test
  void should_returnEmpty_when_eventIdAlreadyExists() {
    // Arrange
    var command =
        new RecordActivityCommand(
            "CUSTOMER",
            "cust-123",
            "CREATED",
            "user-001",
            "tenant-alpha",
            "Customer created",
            "evt-duplicate");

    when(activityRepository.existsByEventId("evt-duplicate")).thenReturn(true);

    // Act
    Optional<Activity> result = useCase.execute(command);

    // Assert
    assertThat(result).isEmpty();
    verify(activityRepository, never()).save(any(Activity.class));
    verify(idGenerator, never()).generate();
    verify(clock, never()).now();
  }

  @Test
  void should_checkDeduplication_when_processingEvent() {
    // Arrange
    var command =
        new RecordActivityCommand(
            "REQUEST",
            "req-456",
            "UPDATED",
            "user-002",
            "tenant-beta",
            "Request updated",
            "evt-002");

    when(activityRepository.existsByEventId("evt-002")).thenReturn(false);
    when(idGenerator.generate()).thenReturn("activity-002");
    when(clock.now()).thenReturn(FIXED_NOW);
    when(activityRepository.save(any(Activity.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    useCase.execute(command);

    // Assert
    verify(activityRepository).existsByEventId("evt-002");
  }
}
