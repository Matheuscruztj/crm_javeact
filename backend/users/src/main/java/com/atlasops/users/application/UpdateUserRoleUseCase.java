package com.atlasops.users.application;

import com.atlasops.shared.domain.exceptions.ResourceNotFoundException;
import com.atlasops.shared.domain.ports.Clock;
import com.atlasops.users.domain.User;
import com.atlasops.users.domain.UserRole;
import com.atlasops.users.domain.ports.UserRepository;
import org.springframework.stereotype.Service;

/**
 * Use case for updating a user's role. Validates that the new role is one of ADMIN, ANALYST, or
 * CLIENT, then persists the change.
 */
@Service
public class UpdateUserRoleUseCase {

  private final UserRepository userRepository;
  private final Clock clock;

  public UpdateUserRoleUseCase(UserRepository userRepository, Clock clock) {
    this.userRepository = userRepository;
    this.clock = clock;
  }

  /**
   * Updates the role of the specified user.
   *
   * @param command the update role command containing userId and new role
   * @return the updated User
   * @throws IllegalArgumentException if the role is invalid
   * @throws ResourceNotFoundException if the user is not found
   */
  public User execute(UpdateUserRoleCommand command) {
    if (command.userId() == null || command.userId().isBlank()) {
      throw new IllegalArgumentException("UserId must not be null or empty");
    }
    if (command.newRole() == null || command.newRole().isBlank()) {
      throw new IllegalArgumentException("Role must not be null or empty");
    }

    UserRole newRole = UserRole.fromString(command.newRole());

    User user =
        userRepository
            .findById(command.userId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "User with id '" + command.userId() + "' not found"));

    user.updateRole(newRole, clock.now());

    return userRepository.save(user);
  }
}
