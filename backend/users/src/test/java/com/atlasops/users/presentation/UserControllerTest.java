package com.atlasops.users.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.atlasops.shared.domain.exceptions.BusinessRuleViolationException;
import com.atlasops.shared.domain.exceptions.DuplicateResourceException;
import com.atlasops.shared.domain.exceptions.ResourceNotFoundException;
import com.atlasops.users.application.CreateUserCommand;
import com.atlasops.users.application.CreateUserUseCase;
import com.atlasops.users.application.DeactivateUserCommand;
import com.atlasops.users.application.DeactivateUserUseCase;
import com.atlasops.users.application.GetUserByIdUseCase;
import com.atlasops.users.application.UpdateUserRoleCommand;
import com.atlasops.users.application.UpdateUserRoleUseCase;
import com.atlasops.users.domain.User;
import com.atlasops.users.domain.UserRole;
import com.atlasops.users.domain.UserStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Unit tests for UserController using standalone MockMvc setup. Tests the presentation layer in
 * isolation from application layer.
 */
@ExtendWith(MockitoExtension.class)
class UserControllerTest {

  private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Mock private CreateUserUseCase createUserUseCase;

  @Mock private UpdateUserRoleUseCase updateUserRoleUseCase;

  @Mock private DeactivateUserUseCase deactivateUserUseCase;

  @Mock private GetUserByIdUseCase getUserByIdUseCase;

  private static final String TENANT_ID = "tenant-001";
  private static final String USER_ID = "user-001";
  private static final Instant NOW = Instant.parse("2025-01-15T10:00:00Z");

  @BeforeEach
  void setUp() {
    UserController controller =
        new UserController(
            createUserUseCase, updateUserRoleUseCase, deactivateUserUseCase, getUserByIdUseCase);

    mockMvc =
        MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new TestExceptionHandler())
            .build();
  }

  @Test
  void should_createUser_when_validRequest() throws Exception {
    User user =
        User.reconstitute(
            USER_ID,
            "user@test.com",
            "John Doe",
            "$2a$10$hash",
            UserRole.ANALYST,
            TENANT_ID,
            UserStatus.ACTIVE,
            NOW,
            NOW);

    when(createUserUseCase.execute(any(CreateUserCommand.class))).thenReturn(user);

    String requestBody =
        """
                {
                    "email": "user@test.com",
                    "name": "John Doe",
                    "password": "securepass123",
                    "role": "ANALYST"
                }
                """;

    mockMvc
        .perform(
            post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Tenant-ID", TENANT_ID)
                .content(requestBody))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(USER_ID))
        .andExpect(jsonPath("$.email").value("user@test.com"))
        .andExpect(jsonPath("$.name").value("John Doe"))
        .andExpect(jsonPath("$.role").value("ANALYST"))
        .andExpect(jsonPath("$.tenantId").value(TENANT_ID))
        .andExpect(jsonPath("$.status").value("ACTIVE"));
  }

  @Test
  void should_return409_when_emailAlreadyExists() throws Exception {
    when(createUserUseCase.execute(any(CreateUserCommand.class)))
        .thenThrow(
            new DuplicateResourceException(
                "User with email 'user@test.com' already exists in this tenant"));

    String requestBody =
        """
                {
                    "email": "user@test.com",
                    "name": "John Doe",
                    "password": "securepass123",
                    "role": "ANALYST"
                }
                """;

    mockMvc
        .perform(
            post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Tenant-ID", TENANT_ID)
                .content(requestBody))
        .andExpect(status().isConflict());
  }

  @Test
  void should_return400_when_invalidInput() throws Exception {
    when(createUserUseCase.execute(any(CreateUserCommand.class)))
        .thenThrow(new IllegalArgumentException("Email must not be null or empty"));

    String requestBody =
        """
                {
                    "email": "",
                    "name": "John Doe",
                    "password": "securepass123",
                    "role": "ANALYST"
                }
                """;

    mockMvc
        .perform(
            post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Tenant-ID", TENANT_ID)
                .content(requestBody))
        .andExpect(status().isBadRequest());
  }

  @Test
  void should_returnUser_when_foundById() throws Exception {
    User user =
        User.reconstitute(
            USER_ID,
            "user@test.com",
            "John Doe",
            "$2a$10$hash",
            UserRole.ADMIN,
            TENANT_ID,
            UserStatus.ACTIVE,
            NOW,
            NOW);

    when(getUserByIdUseCase.execute(USER_ID)).thenReturn(user);

    mockMvc
        .perform(get("/api/v1/users/{id}", USER_ID).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(USER_ID))
        .andExpect(jsonPath("$.email").value("user@test.com"))
        .andExpect(jsonPath("$.name").value("John Doe"))
        .andExpect(jsonPath("$.role").value("ADMIN"))
        .andExpect(jsonPath("$.status").value("ACTIVE"));
  }

  @Test
  void should_return404_when_userNotFoundById() throws Exception {
    when(getUserByIdUseCase.execute("nonexistent"))
        .thenThrow(new ResourceNotFoundException("User with id 'nonexistent' not found"));

    mockMvc
        .perform(get("/api/v1/users/{id}", "nonexistent").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  void should_updateRole_when_validRequest() throws Exception {
    User user =
        User.reconstitute(
            USER_ID,
            "user@test.com",
            "John Doe",
            "$2a$10$hash",
            UserRole.ADMIN,
            TENANT_ID,
            UserStatus.ACTIVE,
            NOW,
            NOW);

    when(updateUserRoleUseCase.execute(any(UpdateUserRoleCommand.class))).thenReturn(user);

    String requestBody =
        """
                {
                    "role": "ADMIN"
                }
                """;

    mockMvc
        .perform(
            patch("/api/v1/users/{id}/role", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(USER_ID))
        .andExpect(jsonPath("$.role").value("ADMIN"));
  }

  @Test
  void should_return400_when_invalidRole() throws Exception {
    when(updateUserRoleUseCase.execute(any(UpdateUserRoleCommand.class)))
        .thenThrow(
            new IllegalArgumentException(
                "Invalid role: SUPERUSER. Must be one of: ADMIN, ANALYST, CLIENT"));

    String requestBody =
        """
                {
                    "role": "SUPERUSER"
                }
                """;

    mockMvc
        .perform(
            patch("/api/v1/users/{id}/role", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isBadRequest());
  }

  @Test
  void should_deactivateUser_when_validRequest() throws Exception {
    User user =
        User.reconstitute(
            USER_ID,
            "user@test.com",
            "John Doe",
            "$2a$10$hash",
            UserRole.ANALYST,
            TENANT_ID,
            UserStatus.INACTIVE,
            NOW,
            NOW);

    when(deactivateUserUseCase.execute(any(DeactivateUserCommand.class))).thenReturn(user);

    mockMvc
        .perform(
            patch("/api/v1/users/{id}/deactivate", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-ID", "admin-001"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(USER_ID))
        .andExpect(jsonPath("$.status").value("INACTIVE"));
  }

  @Test
  void should_return422_when_selfDeactivation() throws Exception {
    when(deactivateUserUseCase.execute(any(DeactivateUserCommand.class)))
        .thenThrow(new BusinessRuleViolationException("Self-deactivation is not allowed"));

    mockMvc
        .perform(
            patch("/api/v1/users/{id}/deactivate", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-ID", USER_ID))
        .andExpect(status().isUnprocessableEntity());
  }

  @Test
  void should_return404_when_deactivatingNonexistentUser() throws Exception {
    when(deactivateUserUseCase.execute(any(DeactivateUserCommand.class)))
        .thenThrow(new ResourceNotFoundException("User with id 'nonexistent' not found"));

    mockMvc
        .perform(
            patch("/api/v1/users/{id}/deactivate", "nonexistent")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-ID", "admin-001"))
        .andExpect(status().isNotFound());
  }
}
