package com.atlasops.boot.infrastructure;

import com.atlasops.shared.domain.ports.FeatureFlagPort;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Implements {@link FeatureFlagPort} by reading flags from Spring {@link Environment}
 * (i.e., application.yml under {@code app.features.*}) with optional runtime overrides
 * stored in Redis under the key {@code feature:{flagName}}.
 *
 * <p>Evaluation order:
 * <ol>
 *   <li>Redis key {@code feature:{flagName}} — if value is "true", flag is ON
 *   <li>Property {@code app.features.{flagName}} from application.yml / env vars
 * </ol>
 *
 * <p>Validates: P1.20 — Feature Flag Framework
 */
public class PropertySourceFeatureFlagAdapter implements FeatureFlagPort {

    private static final Logger log = LoggerFactory.getLogger(PropertySourceFeatureFlagAdapter.class);
    private static final String PROPERTY_PREFIX = "app.features.";
    private static final String REDIS_KEY_PREFIX = "feature:";

    private final Environment environment;
    private final StringRedisTemplate redisTemplate;

    public PropertySourceFeatureFlagAdapter(
            Environment environment, StringRedisTemplate redisTemplate) {
        this.environment = environment;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean isEnabled(String flagName) {
        // 1. Check Redis runtime override
        try {
            String redisValue = redisTemplate.opsForValue().get(REDIS_KEY_PREFIX + flagName);
            if (redisValue != null) {
                boolean runtimeValue = "true".equalsIgnoreCase(redisValue.trim());
                log.debug("Feature flag '{}' resolved from Redis: {}", flagName, runtimeValue);
                return runtimeValue;
            }
        } catch (Exception e) {
            log.debug("Could not read feature flag '{}' from Redis, falling back to properties: {}",
                    flagName, e.getMessage());
        }

        // 2. Fall back to property source
        String propertyKey = PROPERTY_PREFIX + flagName;
        boolean propertyValue = Boolean.parseBoolean(
                environment.getProperty(propertyKey, "false"));
        log.debug("Feature flag '{}' resolved from properties ({}): {}", flagName, propertyKey, propertyValue);
        return propertyValue;
    }

    @Override
    public boolean isEnabled(String flagName, String tenantId) {
        // Check tenant-specific Redis key first
        try {
            String tenantKey = REDIS_KEY_PREFIX + flagName + ":tenant:" + tenantId;
            String tenantValue = redisTemplate.opsForValue().get(tenantKey);
            if (tenantValue != null) {
                boolean result = "true".equalsIgnoreCase(tenantValue.trim());
                log.debug("Feature flag '{}' for tenant '{}' resolved from Redis: {}", flagName, tenantId, result);
                return result;
            }
        } catch (Exception e) {
            log.debug("Could not read tenant feature flag from Redis, falling back: {}", e.getMessage());
        }

        // Fall back to global flag
        return isEnabled(flagName);
    }
}
