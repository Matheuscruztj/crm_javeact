package com.atlasops.boot.config;

import com.atlasops.boot.infrastructure.PropertySourceFeatureFlagAdapter;
import com.atlasops.shared.domain.ports.FeatureFlagPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Configuration for the Feature Flag infrastructure.
 * Reads flags from {@code app.features.*} in application.yml and supports
 * runtime overrides via Redis keys.
 *
 * <p>Validates: P1.20 — Feature Flag Framework
 */
@Configuration
public class FeatureFlagConfig {

    /**
     * Registers a {@link PropertySourceFeatureFlagAdapter} bean that reads from
     * Spring Environment (application.yml) and Redis for runtime overrides.
     */
    @Bean
    public FeatureFlagPort featureFlagPort(
            Environment environment, StringRedisTemplate redisTemplate) {
        return new PropertySourceFeatureFlagAdapter(environment, redisTemplate);
    }
}
