package com.atlasops.integrations.infrastructure;

import com.atlasops.integrations.domain.ports.UrlValidationPort;
import org.springframework.stereotype.Component;

/**
 * Adapter implementing {@link UrlValidationPort} using {@link SSRFValidator}
 * to protect against Server-Side Request Forgery attacks.
 *
 * <p>Validates: P0.J.1 — SSRF Protection
 */
@Component
public class SSRFUrlValidationAdapter implements UrlValidationPort {

    @Override
    public boolean isSafe(String url) {
        return SSRFValidator.isSafe(url);
    }
}
