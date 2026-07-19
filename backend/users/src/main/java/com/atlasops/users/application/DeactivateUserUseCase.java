package com.atlasops.users.application;

import com.atlasops.shared.domain.exceptions.BusinessRuleViolationException;
import com.atlasops.shared.domain.exceptions.ResourceNotFoundException;
import com.atlasops.shared.domain.ports.Clock;
import com.atlasops.users.domain.User;
import com.atlasops.users.domain.ports.UserRepository;

/** Use case for deactivating a user. Prevents self-deactivation and marks the user as INACTIVE. */
public class DeactivateUserUseCase {

  private final UserRepository userRepository;
  private final Clock clock;

  public DeactivateUserUseCase(UserRepository userRepository, Clock clock) {
    this.userRepository = userRepository;
    this.clock = clock;
  }

  /**
   * Deactivates the specified user.
   *
   * @param command the deactivate command containing userId and requestingUserId
   * @return the deactivated User
   * @throws IllegalArgumentException if required fields are missing
   * @throws BusinessRuleViolationException if the user attempts self-deactivation
   * @throws ResourceNotFoundException if the user is not found
   */
  public User execute(DeactivateUserCommand command) {
    if (command.userId() == null || command.userId().isBlank()) {
      throw new IllegalArgumentException("UserId must not be null or empty");
    }
    if (command.requestingUserId() == null || command.requestingUserId().isBlank()) {
      throw new IllegalArgumentException("RequestingUserId must not be null or empty");
    }

    if (command.userId().equals(command.requestingUserId())) {
      throw new BusinessRuleViolationException("Self-deactivation is not allowed");
    }

    User user =
        userRepository
            .findById(command.userId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "User with id '" + command.userId() + "' not found"));

    user.deactivate(clock.now());

    return userRepository.save(user);
  }
}
