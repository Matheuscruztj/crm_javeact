package com.atlasops.activities.application;

import com.atlasops.activities.domain.Activity;
import com.atlasops.activities.domain.ports.ActivityRepository;
import com.atlasops.shared.domain.ports.Clock;
import com.atlasops.shared.domain.ports.IdGenerator;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * Use case for recording a new activity entry from a domain event. Performs deduplication check via
 * eventId before persisting to prevent duplicate entries from duplicate event delivery.
 */
@Service
public class RecordActivityUseCase {

  private final ActivityRepository activityRepository;
  private final IdGenerator idGenerator;
  private final Clock clock;

  public RecordActivityUseCase(
      ActivityRepository activityRepository, IdGenerator idGenerator, Clock clock) {
    this.activityRepository = activityRepository;
    this.idGenerator = idGenerator;
    this.clock = clock;
  }

  /**
   * Records a new activity entry if the event has not already been processed.
   *
   * @param command the record activity command
   * @return the persisted Activity if it was new, or empty if it was a duplicate
   */
  public Optional<Activity> execute(RecordActivityCommand command) {
    if (activityRepository.existsByEventId(command.eventId())) {
      return Optional.empty();
    }

    String id = idGenerator.generate();

    Activity activity =
        Activity.create(
            id,
            command.entityType(),
            command.entityId(),
            command.actionType(),
            command.actorId(),
            command.tenantId(),
            command.summary(),
            command.eventId(),
            clock.now());

    Activity saved = activityRepository.save(activity);
    return Optional.of(saved);
  }
}
