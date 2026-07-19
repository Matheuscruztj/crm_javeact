package com.atlasops.auth.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlasops.auth.domain.RefreshToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class RedisRefreshTokenAdapterTest {

  @Mock private StringRedisTemplate redisTemplate;

  @Mock private ValueOperations<String, String> valueOperations;

  @Mock private SetOperations<String, String> setOperations;

  private RedisRefreshTokenAdapter adapter;

  private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

  @BeforeEach
  void setUp() {
    adapter = new RedisRefreshTokenAdapter(redisTemplate, objectMapper);
  }

  @Test
  void should_saveToken_when_validTokenProvided() {
    // Arrange
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(redisTemplate.opsForSet()).thenReturn(setOperations);

    RefreshToken token =
        new RefreshToken(
            "token-id-1",
            "hash123",
            "user-001",
            "tenant-alpha",
            Instant.parse("2025-01-22T10:00:00Z"),
            false,
            Instant.parse("2025-01-15T10:00:00Z"));

    // Act
    adapter.save(token);

    // Assert
    verify(valueOperations).set(eq("refresh_token:hash123"), anyString(), eq(Duration.ofDays(7)));
    verify(setOperations).add("user_tokens:user-001", "hash123");
  }

  @Test
  void should_returnToken_when_tokenHashExists() {
    // Arrange
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);

    String json =
        """
                {"id":"token-id-1","userId":"user-001","tenantId":"tenant-alpha","expiresAt":"2025-01-22T10:00:00Z","revoked":false,"createdAt":"2025-01-15T10:00:00Z"}
                """;
    when(valueOperations.get("refresh_token:hash123")).thenReturn(json);

    // Act
    Optional<RefreshToken> result = adapter.findByTokenHash("hash123");

    // Assert
    assertThat(result).isPresent();
    RefreshToken token = result.get();
    assertThat(token.getId()).isEqualTo("token-id-1");
    assertThat(token.getTokenHash()).isEqualTo("hash123");
    assertThat(token.getUserId()).isEqualTo("user-001");
    assertThat(token.getTenantId()).isEqualTo("tenant-alpha");
    assertThat(token.getExpiresAt()).isEqualTo(Instant.parse("2025-01-22T10:00:00Z"));
    assertThat(token.isRevoked()).isFalse();
    assertThat(token.getCreatedAt()).isEqualTo(Instant.parse("2025-01-15T10:00:00Z"));
  }

  @Test
  void should_returnEmpty_when_tokenHashNotFound() {
    // Arrange
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.get("refresh_token:nonexistent")).thenReturn(null);

    // Act
    Optional<RefreshToken> result = adapter.findByTokenHash("nonexistent");

    // Assert
    assertThat(result).isEmpty();
  }

  @Test
  void should_deleteTokenKey_when_revokeByTokenHash() {
    // Act
    adapter.revokeByTokenHash("hash123");

    // Assert
    verify(redisTemplate).delete("refresh_token:hash123");
  }

  @Test
  void should_deleteAllTokensAndSet_when_revokeAllByUserId() {
    // Arrange
    when(redisTemplate.opsForSet()).thenReturn(setOperations);
    when(setOperations.members("user_tokens:user-001"))
        .thenReturn(Set.of("hash1", "hash2", "hash3"));

    // Act
    adapter.revokeAllByUserId("user-001");

    // Assert
    verify(redisTemplate).delete("refresh_token:hash1");
    verify(redisTemplate).delete("refresh_token:hash2");
    verify(redisTemplate).delete("refresh_token:hash3");
    verify(redisTemplate).delete("user_tokens:user-001");
  }

  @Test
  void should_onlyDeleteSet_when_revokeAllByUserIdWithNoTokens() {
    // Arrange
    when(redisTemplate.opsForSet()).thenReturn(setOperations);
    when(setOperations.members("user_tokens:user-001")).thenReturn(Set.of());

    // Act
    adapter.revokeAllByUserId("user-001");

    // Assert
    verify(redisTemplate).delete("user_tokens:user-001");
  }
}
