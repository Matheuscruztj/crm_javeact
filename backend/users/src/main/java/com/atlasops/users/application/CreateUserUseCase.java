package com.atlasops.users.application;

import com.atlasops.shared.domain.exceptions.DuplicateResourceException;
import com.atlasops.shared.domain.ports.Clock;
import com.atlasops.shared.domain.ports.IdGenerator;
import com.atlasops.shared.domain.types.Email;
import com.atlasops.users.domain.User;
import com.atlasops.users.domain.UserRole;
import com.atlasops.users.domain.ports.PasswordHashPort;
import com.atlasops.users.domain.ports.UserRepository;
import org.springframework.stereotype.Service;

/**
 * Use case for creating a new user within a tenant. Validates input (email format, name length,
 * password length, valid role), hashes password with bcrypt (cost >= 10), enforces email uniqueness
 * within tenant, and persists the user.
 */
@Service
public class CreateUserUseCase {

  private static final int MIN_NAME_LENGTH = 2;
  private static final int MAX_NAME_LENGTH = 100;
  private static final int MIN_PASSWORD_LENGTH = 8;

  private final UserRepository userRepository;
  private final PasswordHashPort passwordHashPort;
  private final IdGenerator idGenerator;
  private final Clock clock;

  public CreateUserUseCase(
      UserRepository userRepository,
      PasswordHashPort passwordHashPort,
      IdGenerator idGenerator,
      Clock clock) {
    this.userRepository = userRepository;
    this.passwordHashPort = passwordHashPort;
    this.idGenerator = idGenerator;
    this.clock = clock;
  }

  /**
   * Creates a new user with the given command parameters.
   *
   * @param command the create user command containing email, name, password, role, and tenantId
   * @return the persisted User
   * @throws IllegalArgumentException if any input validation fails
   * @throws DuplicateResourceException if email already exists within the tenant
   */
  public User execute(CreateUserCommand command) {
    validateCommand(command);

    Email email = new Email(command.email());

    UserRole role = UserRole.fromString(command.role());

    if (userRepository.existsByEmailAndTenantId(email.getValue(), command.tenantId())) {
      throw new DuplicateResourceException(
          "User with email '" + email.getValue() + "' already exists in this tenant");
    }

    String passwordHash = passwordHashPort.hash(command.password());

    String id = idGenerator.generate();
    User user =
        User.create(
            id,
            email.getValue(),
            command.name().trim(),
            passwordHash,
            role,
            command.tenantId(),
            clock.now());

    return userRepository.save(user);
  }

  private void validateCommand(CreateUserCommand command) {
    if (command.email() == null || command.email().isBlank()) {
      throw new IllegalArgumentException("Email must not be null or empty");
    }
    if (command.name() == null || command.name().isBlank()) {
      throw new IllegalArgumentException("Name must not be null or empty");
    }
    String trimmedName = command.name().trim();
    if (trimmedName.length() < MIN_NAME_LENGTH || trimmedName.length() > MAX_NAME_LENGTH) {
      throw new IllegalArgumentException(
          "Name must be between " + MIN_NAME_LENGTH + " and " + MAX_NAME_LENGTH + " characters");
    }
    if (command.password() == null || command.password().length() < MIN_PASSWORD_LENGTH) {
      throw new IllegalArgumentException(
          "Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
    }
    if (command.role() == null || command.role().isBlank()) {
      throw new IllegalArgumentException("Role must not be null or empty");
    }
    if (command.tenantId() == null || command.tenantId().isBlank()) {
      throw new IllegalArgumentException("TenantId must not be null or empty");
    }
  }
}
