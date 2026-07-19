package com.atlasops.users.application;

import com.atlasops.shared.domain.exceptions.ResourceNotFoundException;
import com.atlasops.users.domain.User;
import com.atlasops.users.domain.ports.UserRepository;

/**
 * Use case for retrieving a user by their identifier.
 *
 * <p>Validates: Requirements 5.1
 */
public class GetUserByIdUseCase {

  private final UserRepository userRepository;

  public GetUserByIdUseCase(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  /**
   * Retrieves a user by their identifier.
   *
   * @param userId the user identifier
   * @return the User if found
   * @throws ResourceNotFoundException if no user exists with the given ID
   */
  public User execute(String userId) {
    if (userId == null || userId.isBlank()) {
      throw new IllegalArgumentException("User ID must not be null or empty");
    }

    return userRepository
        .findById(userId)
        .orElseThrow(
            () -> new ResourceNotFoundException("User with id '" + userId + "' not found"));
  }
}
