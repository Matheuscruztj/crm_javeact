package com.atlasops.users.presentation;

import com.atlasops.users.application.CreateUserCommand;
import com.atlasops.users.application.CreateUserUseCase;
import com.atlasops.users.application.DeactivateUserCommand;
import com.atlasops.users.application.DeactivateUserUseCase;
import com.atlasops.users.application.GetUserByIdUseCase;
import com.atlasops.users.application.UpdateUserRoleCommand;
import com.atlasops.users.application.UpdateUserRoleUseCase;
import com.atlasops.users.domain.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for user management operations. All endpoints require ADMIN role (enforced by
 * security configuration).
 *
 * <p>Validates: Requirements 5.1, 5.5, 5.7
 */
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

  private final CreateUserUseCase createUserUseCase;
  private final UpdateUserRoleUseCase updateUserRoleUseCase;
  private final DeactivateUserUseCase deactivateUserUseCase;
  private final GetUserByIdUseCase getUserByIdUseCase;

  public UserController(
      CreateUserUseCase createUserUseCase,
      UpdateUserRoleUseCase updateUserRoleUseCase,
      DeactivateUserUseCase deactivateUserUseCase,
      GetUserByIdUseCase getUserByIdUseCase) {
    this.createUserUseCase = createUserUseCase;
    this.updateUserRoleUseCase = updateUserRoleUseCase;
    this.deactivateUserUseCase = deactivateUserUseCase;
    this.getUserByIdUseCase = getUserByIdUseCase;
  }

  /** Creates a new user within the tenant. Requires ADMIN role. */
  @PostMapping
  public ResponseEntity<UserResponse> createUser(
      @RequestBody CreateUserRequest request, @RequestHeader("X-Tenant-ID") String tenantId) {

    CreateUserCommand command =
        new CreateUserCommand(
            request.email(), request.name(), request.password(), request.role(), tenantId);

    User user = createUserUseCase.execute(command);

    return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(user));
  }

  /** Gets a user by their identifier. */
  @GetMapping("/{id}")
  public ResponseEntity<UserResponse> getUserById(@PathVariable String id) {
    User user = getUserByIdUseCase.execute(id);
    return ResponseEntity.ok(toResponse(user));
  }

  /** Updates a user's role. Requires ADMIN role. */
  @PatchMapping("/{id}/role")
  public ResponseEntity<UserResponse> updateUserRole(
      @PathVariable String id, @RequestBody UpdateUserRoleRequest request) {

    UpdateUserRoleCommand command = new UpdateUserRoleCommand(id, request.role());
    User user = updateUserRoleUseCase.execute(command);

    return ResponseEntity.ok(toResponse(user));
  }

  /** Deactivates a user. Requires ADMIN role. Self-deactivation is not allowed. */
  @PatchMapping("/{id}/deactivate")
  public ResponseEntity<UserResponse> deactivateUser(
      @PathVariable String id,
      @RequestHeader(value = "X-User-ID", required = false) String requestingUserId) {

    DeactivateUserCommand command = new DeactivateUserCommand(id, requestingUserId);
    User user = deactivateUserUseCase.execute(command);

    return ResponseEntity.ok(toResponse(user));
  }

  private UserResponse toResponse(User user) {
    return new UserResponse(
        user.getId(),
        user.getEmail(),
        user.getName(),
        user.getRole().name(),
        user.getTenantId(),
        user.getStatus().name(),
        user.getCreatedAt(),
        user.getUpdatedAt());
  }
}
