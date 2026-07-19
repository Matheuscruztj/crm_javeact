package com.atlasops.auth.infrastructure;

import com.atlasops.auth.domain.RefreshToken;
import com.atlasops.auth.domain.ports.RefreshTokenRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Redis-backed implementation of RefreshTokenRepository.
 *
 * <p>Key patterns:
 *
 * <ul>
 *   <li>{@code refresh_token:{tokenHash}} → JSON with userId, tenantId, expiresAt, revoked,
 *       createdAt
 *   <li>{@code user_tokens:{userId}} → Set of tokenHashes for bulk invalidation
 * </ul>
 */
@Component
public class RedisRefreshTokenAdapter implements RefreshTokenRepository {

  private static final String TOKEN_KEY_PREFIX = "refresh_token:";
  private static final String USER_TOKENS_KEY_PREFIX = "user_tokens:";
  private static final Duration DEFAULT_TTL = Duration.ofDays(7);

  private final StringRedisTemplate redisTemplate;
  private final ObjectMapper objectMapper;

  public RedisRefreshTokenAdapter(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
    this.redisTemplate = redisTemplate;
    this.objectMapper = objectMapper;
  }

  @Override
  public void save(RefreshToken token) {
    String tokenKey = TOKEN_KEY_PREFIX + token.getTokenHash();
    String userTokensKey = USER_TOKENS_KEY_PREFIX + token.getUserId();

    String json = serializeToken(token);
    redisTemplate.opsForValue().set(tokenKey, json, DEFAULT_TTL);
    redisTemplate.opsForSet().add(userTokensKey, token.getTokenHash());
  }

  @Override
  public Optional<RefreshToken> findByTokenHash(String tokenHash) {
    String tokenKey = TOKEN_KEY_PREFIX + tokenHash;
    String json = redisTemplate.opsForValue().get(tokenKey);

    if (json == null) {
      return Optional.empty();
    }

    return Optional.of(deserializeToken(tokenHash, json));
  }

  @Override
  public void revokeByTokenHash(String tokenHash) {
    String tokenKey = TOKEN_KEY_PREFIX + tokenHash;
    redisTemplate.delete(tokenKey);
  }

  @Override
  public void revokeAllByUserId(String userId) {
    String userTokensKey = USER_TOKENS_KEY_PREFIX + userId;
    Set<String> tokenHashes = redisTemplate.opsForSet().members(userTokensKey);

    if (tokenHashes != null && !tokenHashes.isEmpty()) {
      for (String tokenHash : tokenHashes) {
        String tokenKey = TOKEN_KEY_PREFIX + tokenHash;
        redisTemplate.delete(tokenKey);
      }
    }

    redisTemplate.delete(userTokensKey);
  }

  private String serializeToken(RefreshToken token) {
    try {
      ObjectNode node = objectMapper.createObjectNode();
      node.put("id", token.getId());
      node.put("userId", token.getUserId());
      node.put("tenantId", token.getTenantId());
      node.put("role", token.getRole() != null ? token.getRole() : "");
      node.put("familyId", token.getFamilyId() != null ? token.getFamilyId() : "");
      node.put("expiresAt", token.getExpiresAt().toString());
      node.put("revoked", token.isRevoked());
      node.put("createdAt", token.getCreatedAt().toString());
      return objectMapper.writeValueAsString(node);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Failed to serialize refresh token", e);
    }
  }

  private RefreshToken deserializeToken(String tokenHash, String json) {
    try {
      ObjectNode node = (ObjectNode) objectMapper.readTree(json);
      String role = node.has("role") && !node.get("role").asText().isEmpty()
          ? node.get("role").asText() : null;
      String familyId = node.has("familyId") && !node.get("familyId").asText().isEmpty()
          ? node.get("familyId").asText() : null;
      return new RefreshToken(
          node.get("id").asText(),
          tokenHash,
          node.get("userId").asText(),
          node.get("tenantId").asText(),
          role,
          familyId,
          Instant.parse(node.get("expiresAt").asText()),
          node.get("revoked").asBoolean(),
          Instant.parse(node.get("createdAt").asText()));
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Failed to deserialize refresh token", e);
    }
  }
}
