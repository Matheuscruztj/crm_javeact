package com.atlasops.auth.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.atlasops.auth.application.AuthenticateCommand;
import com.atlasops.auth.application.AuthenticateUserUseCase;
import com.atlasops.auth.application.LogoutUseCase;
import com.atlasops.auth.application.RefreshTokenUseCase;
import com.atlasops.auth.domain.AuthenticationResult;
import com.atlasops.shared.domain.exceptions.TooManyRequestsException;
import com.atlasops.shared.domain.exceptions.UnauthorizedException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController")
class AuthControllerTest {

  private static final String TENANT_ID = "tenant-alpha";
  private static final String ACCESS_TOKEN = "eyJhbGciOiJIUzI1NiJ9.valid.token";
  private static final String REFRESH_TOKEN = "refresh-token-value";

  @Mock private AuthenticateUserUseCase authenticateUserUseCase;

  @Mock private RefreshTokenUseCase refreshTokenUseCase;

  @Mock private LogoutUseCase logoutUseCase;

  private MockMvc mockMvc;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    AuthController controller =
        new AuthController(authenticateUserUseCase, refreshTokenUseCase, logoutUseCase);

    mockMvc =
        MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new TestExceptionHandler())
            .build();
    objectMapper = new ObjectMapper();
  }

  @Nested
  @DisplayName("POST /api/v1/auth/login")
  class LoginEndpoint {

    @Test
    void should_returnTokenResponse_when_validCredentials() throws Exception {
      // Arrange
      var result = AuthenticationResult.of(ACCESS_TOKEN, REFRESH_TOKEN, 900L);
      when(authenticateUserUseCase.execute(any(AuthenticateCommand.class))).thenReturn(result);

      var request = new LoginRequest("user@example.com", "password123");

      // Act & Assert
      mockMvc
          .perform(
              post("/api/v1/auth/login")
                  .contentType(MediaType.APPLICATION_JSON)
                  .header("X-Tenant-ID", TENANT_ID)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.accessToken").value(ACCESS_TOKEN))
          .andExpect(jsonPath("$.refreshToken").value(REFRESH_TOKEN))
          .andExpect(jsonPath("$.expiresIn").value(900))
          .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void should_return400_when_emailIsBlank() throws Exception {
      // Arrange
      var request = new LoginRequest("", "password123");

      // Act & Assert
      mockMvc
          .perform(
              post("/api/v1/auth/login")
                  .contentType(MediaType.APPLICATION_JSON)
                  .header("X-Tenant-ID", TENANT_ID)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void should_return400_when_passwordIsBlank() throws Exception {
      // Arrange
      var request = new LoginRequest("user@example.com", "");

      // Act & Assert
      mockMvc
          .perform(
              post("/api/v1/auth/login")
                  .contentType(MediaType.APPLICATION_JSON)
                  .header("X-Tenant-ID", TENANT_ID)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void should_return400_when_emailIsMissing() throws Exception {
      // Arrange — email field omitted
      String body = "{\"password\": \"password123\"}";

      // Act & Assert
      mockMvc
          .perform(
              post("/api/v1/auth/login")
                  .contentType(MediaType.APPLICATION_JSON)
                  .header("X-Tenant-ID", TENANT_ID)
                  .content(body))
          .andExpect(status().isBadRequest());
    }

    @Test
    void should_return400_when_passwordIsMissing() throws Exception {
      // Arrange — password field omitted
      String body = "{\"email\": \"user@example.com\"}";

      // Act & Assert
      mockMvc
          .perform(
              post("/api/v1/auth/login")
                  .contentType(MediaType.APPLICATION_JSON)
                  .header("X-Tenant-ID", TENANT_ID)
                  .content(body))
          .andExpect(status().isBadRequest());
    }

    @Test
    void should_return401_when_invalidCredentials() throws Exception {
      // Arrange
      when(authenticateUserUseCase.execute(any(AuthenticateCommand.class)))
          .thenThrow(new UnauthorizedException("Invalid email or password"));

      var request = new LoginRequest("user@example.com", "wrongpassword");

      // Act & Assert
      mockMvc
          .perform(
              post("/api/v1/auth/login")
                  .contentType(MediaType.APPLICATION_JSON)
                  .header("X-Tenant-ID", TENANT_ID)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isUnauthorized());
    }

    @Test
    void should_return429_when_accountIsLocked() throws Exception {
      // Arrange
      when(authenticateUserUseCase.execute(any(AuthenticateCommand.class)))
          .thenThrow(
              new TooManyRequestsException(
                  "Account temporarily locked due to too many failed attempts"));

      var request = new LoginRequest("user@example.com", "password123");

      // Act & Assert
      mockMvc
          .perform(
              post("/api/v1/auth/login")
                  .contentType(MediaType.APPLICATION_JSON)
                  .header("X-Tenant-ID", TENANT_ID)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isTooManyRequests());
    }

    @Test
    void should_passTenantIdToUseCase_when_headerProvided() throws Exception {
      // Arrange
      var result = AuthenticationResult.of(ACCESS_TOKEN, REFRESH_TOKEN, 900L);
      when(authenticateUserUseCase.execute(any(AuthenticateCommand.class))).thenReturn(result);

      var request = new LoginRequest("user@example.com", "password123");

      // Act
      mockMvc
          .perform(
              post("/api/v1/auth/login")
                  .contentType(MediaType.APPLICATION_JSON)
                  .header("X-Tenant-ID", TENANT_ID)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk());

      // Assert — verify the command was created with the correct tenantId
      verify(authenticateUserUseCase)
          .execute(eq(new AuthenticateCommand("user@example.com", "password123", TENANT_ID)));
    }
  }

  @Nested
  @DisplayName("POST /api/v1/auth/refresh")
  class RefreshEndpoint {

    @Test
    void should_returnNewTokenResponse_when_validRefreshToken() throws Exception {
      // Arrange
      var result = AuthenticationResult.of(ACCESS_TOKEN, "new-refresh-token", 900L);
      when(refreshTokenUseCase.execute(REFRESH_TOKEN)).thenReturn(result);

      var request = new RefreshRequest(REFRESH_TOKEN);

      // Act & Assert
      mockMvc
          .perform(
              post("/api/v1/auth/refresh")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.accessToken").value(ACCESS_TOKEN))
          .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"))
          .andExpect(jsonPath("$.expiresIn").value(900))
          .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void should_return400_when_refreshTokenIsBlank() throws Exception {
      // Arrange
      var request = new RefreshRequest("");

      // Act & Assert
      mockMvc
          .perform(
              post("/api/v1/auth/refresh")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void should_return400_when_refreshTokenIsMissing() throws Exception {
      // Arrange
      String body = "{}";

      // Act & Assert
      mockMvc
          .perform(
              post("/api/v1/auth/refresh").contentType(MediaType.APPLICATION_JSON).content(body))
          .andExpect(status().isBadRequest());
    }

    @Test
    void should_return401_when_invalidRefreshToken() throws Exception {
      // Arrange
      when(refreshTokenUseCase.execute("invalid-token"))
          .thenThrow(new UnauthorizedException("Invalid refresh token"));

      var request = new RefreshRequest("invalid-token");

      // Act & Assert
      mockMvc
          .perform(
              post("/api/v1/auth/refresh")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isUnauthorized());
    }
  }

  @Nested
  @DisplayName("POST /api/v1/auth/logout")
  class LogoutEndpoint {

    @Test
    void should_return204_when_validLogout() throws Exception {
      // Arrange
      doNothing().when(logoutUseCase).execute(REFRESH_TOKEN);

      var request = new LogoutRequest(REFRESH_TOKEN);

      // Act & Assert
      mockMvc
          .perform(
              post("/api/v1/auth/logout")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isNoContent());

      verify(logoutUseCase).execute(REFRESH_TOKEN);
    }

    @Test
    void should_return400_when_refreshTokenIsBlank() throws Exception {
      // Arrange
      var request = new LogoutRequest("");

      // Act & Assert
      mockMvc
          .perform(
              post("/api/v1/auth/logout")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void should_return400_when_refreshTokenIsMissing() throws Exception {
      // Arrange
      String body = "{}";

      // Act & Assert
      mockMvc
          .perform(
              post("/api/v1/auth/logout").contentType(MediaType.APPLICATION_JSON).content(body))
          .andExpect(status().isBadRequest());
    }
  }

  /**
   * Test-local exception handler to simulate the GlobalExceptionHandler from app-boot. In
   * production, the GlobalExceptionHandler handles these exceptions.
   */
  @RestControllerAdvice
  static class TestExceptionHandler {

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorized(UnauthorizedException ex) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(Map.of("status", 401, "code", "UNAUTHORIZED", "detail", ex.getMessage()));
    }

    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<Map<String, Object>> handleTooManyRequests(TooManyRequestsException ex) {
      return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
          .body(Map.of("status", 429, "code", "TOO_MANY_REQUESTS", "detail", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
        MethodArgumentNotValidException ex) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(
              Map.of(
                  "status", 400,
                  "code", "VALIDATION_FAILED",
                  "detail", "One or more fields have validation errors"));
    }
  }
}
