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
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.Tag;
import org.mockito.InOrder;
import org.mockito.Mockito;

/**
 * Property-based tests for activity deduplication via eventId.
 *
 * <p><b>Validates: Requirements 14.7</b>
 *
 * <p>Property 21: Activity Deduplication
 *
 * <p>Requirement 14.7: IF a domain event is received more than once (duplicate delivery), THEN THE
 * Activity_Module SHALL not create a duplicate activity entry
 */
@Tag("Feature: project-implementation-kickoff, Property 21: Activity Deduplication")
class ActivityDeduplicationPropertyTest {

  private static final Instant FIXED_NOW = Instant.parse("2025-01-15T10:00:00Z");
  private static final String GENERATED_ID = "activity-gen-001";

  /**
   * Property: For ANY eventId, if existsByEventId returns true, RecordActivityUseCase returns
   * Optional.empty() (no duplicate created).
   *
   * <p>Validates: Requirements 14.7
   */
  @Property(tries = 100)
  void should_alwaysReturnEmpty_forAnyDuplicateEventId(
      @ForAll("validEventIds") String eventId,
      @ForAll("validEntityTypes") String entityType,
      @ForAll("validEntityIds") String entityId,
      @ForAll("validActionTypes") String actionType,
      @ForAll("validActorIds") String actorId,
      @ForAll("validTenantIds") String tenantId,
      @ForAll("validSummaries") String summary) {

    // Arrange
    ActivityRepository activityRepository = Mockito.mock(ActivityRepository.class);
    IdGenerator idGenerator = Mockito.mock(IdGenerator.class);
    Clock clock = Mockito.mock(Clock.class);

    var useCase = new RecordActivityUseCase(activityRepository, idGenerator, clock);
    var command =
        new RecordActivityCommand(
            entityType, entityId, actionType, actorId, tenantId, summary, eventId);

    // Simulate existing eventId (duplicate delivery)
    when(activityRepository.existsByEventId(eventId)).thenReturn(true);

    // Act
    Optional<Activity> result = useCase.execute(command);

    // Assert: no activity created, empty returned
    assertThat(result).isEmpty();
    verify(activityRepository, never()).save(any(Activity.class));
  }

  /**
   * Property: For ANY eventId, if existsByEventId returns false, a new activity is saved and
   * returned.
   *
   * <p>Validates: Requirements 14.7
   */
  @Property(tries = 100)
  void should_alwaysSaveNewActivity_forAnyNewEventId(
      @ForAll("validEventIds") String eventId,
      @ForAll("validEntityTypes") String entityType,
      @ForAll("validEntityIds") String entityId,
      @ForAll("validActionTypes") String actionType,
      @ForAll("validActorIds") String actorId,
      @ForAll("validTenantIds") String tenantId,
      @ForAll("validSummaries") String summary) {

    // Arrange
    ActivityRepository activityRepository = Mockito.mock(ActivityRepository.class);
    IdGenerator idGenerator = Mockito.mock(IdGenerator.class);
    Clock clock = Mockito.mock(Clock.class);

    var useCase = new RecordActivityUseCase(activityRepository, idGenerator, clock);
    var command =
        new RecordActivityCommand(
            entityType, entityId, actionType, actorId, tenantId, summary, eventId);

    // Simulate new eventId (first delivery)
    when(activityRepository.existsByEventId(eventId)).thenReturn(false);
    when(idGenerator.generate()).thenReturn(GENERATED_ID);
    when(clock.now()).thenReturn(FIXED_NOW);
    when(activityRepository.save(any(Activity.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    Optional<Activity> result = useCase.execute(command);

    // Assert: new activity is created and saved
    assertThat(result).isPresent();
    Activity activity = result.get();
    assertThat(activity.getEventId()).isEqualTo(eventId);
    assertThat(activity.getEntityType()).isEqualTo(entityType);
    assertThat(activity.getEntityId()).isEqualTo(entityId);
    assertThat(activity.getActionType()).isEqualTo(actionType);
    assertThat(activity.getActorId()).isEqualTo(actorId);
    assertThat(activity.getTenantId()).isEqualTo(tenantId);
    assertThat(activity.getSummary()).isEqualTo(summary);
    verify(activityRepository).save(any(Activity.class));
  }

  /**
   * Property: The dedup check (existsByEventId) is always performed BEFORE save, regardless of
   * other parameters.
   *
   * <p>Validates: Requirements 14.7
   */
  @Property(tries = 100)
  void should_alwaysCheckDedupBeforeSave_forAnyParameters(
      @ForAll("validEventIds") String eventId,
      @ForAll("validEntityTypes") String entityType,
      @ForAll("validEntityIds") String entityId,
      @ForAll("validActionTypes") String actionType,
      @ForAll("validActorIds") String actorId,
      @ForAll("validTenantIds") String tenantId,
      @ForAll("validSummaries") String summary) {

    // Arrange
    ActivityRepository activityRepository = Mockito.mock(ActivityRepository.class);
    IdGenerator idGenerator = Mockito.mock(IdGenerator.class);
    Clock clock = Mockito.mock(Clock.class);

    var useCase = new RecordActivityUseCase(activityRepository, idGenerator, clock);
    var command =
        new RecordActivityCommand(
            entityType, entityId, actionType, actorId, tenantId, summary, eventId);

    // Simulate new event
    when(activityRepository.existsByEventId(eventId)).thenReturn(false);
    when(idGenerator.generate()).thenReturn(GENERATED_ID);
    when(clock.now()).thenReturn(FIXED_NOW);
    when(activityRepository.save(any(Activity.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    useCase.execute(command);

    // Assert: existsByEventId is always called before save
    InOrder inOrder = Mockito.inOrder(activityRepository);
    inOrder.verify(activityRepository).existsByEventId(eventId);
    inOrder.verify(activityRepository).save(any(Activity.class));
  }

  // ---- Custom Arbitraries ----

  @Provide
  Arbitrary<String> validEventIds() {
    return Arbitraries.strings()
        .withChars("abcdefghijklmnopqrstuvwxyz0123456789-".toCharArray())
        .ofMinLength(3)
        .ofMaxLength(50)
        .map(s -> "evt-" + s);
  }

  @Provide
  Arbitrary<String> validEntityTypes() {
    return Arbitraries.of("CUSTOMER", "REQUEST", "DOCUMENT", "APPROVAL", "USER", "TENANT");
  }

  @Provide
  Arbitrary<String> validEntityIds() {
    return Arbitraries.strings()
        .withChars("abcdefghijklmnopqrstuvwxyz0123456789-".toCharArray())
        .ofMinLength(3)
        .ofMaxLength(36)
        .map(s -> "id-" + s);
  }

  @Provide
  Arbitrary<String> validActionTypes() {
    return Arbitraries.of(
        "CREATED", "UPDATED", "DELETED", "STATUS_CHANGED", "ASSIGNED", "APPROVED", "REJECTED");
  }

  @Provide
  Arbitrary<String> validActorIds() {
    return Arbitraries.strings()
        .withChars("abcdefghijklmnopqrstuvwxyz0123456789-".toCharArray())
        .ofMinLength(3)
        .ofMaxLength(36)
        .map(s -> "user-" + s);
  }

  @Provide
  Arbitrary<String> validTenantIds() {
    return Arbitraries.strings()
        .withChars("abcdefghijklmnopqrstuvwxyz0123456789-".toCharArray())
        .ofMinLength(3)
        .ofMaxLength(36)
        .map(s -> "tenant-" + s);
  }

  @Provide
  Arbitrary<String> validSummaries() {
    return Arbitraries.strings()
        .withChars("abcdefghijklmnopqrstuvwxyz ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray())
        .ofMinLength(1)
        .ofMaxLength(100)
        .filter(s -> !s.isBlank());
  }
}
