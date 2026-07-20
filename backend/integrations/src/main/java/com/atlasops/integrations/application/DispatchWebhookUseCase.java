package com.atlasops.integrations.application;

import com.atlasops.integrations.domain.DispatchResult;
import com.atlasops.integrations.domain.WebhookPayload;
import com.atlasops.integrations.domain.ports.IntegrationPort;
import java.util.Objects;

/**
 * Use case for dispatching a webhook payload to an external system.
 *
 * <p>Validates: P0.C.1 — Integrations module adapters
 */
public class DispatchWebhookUseCase {

    private final IntegrationPort integrationPort;

    public DispatchWebhookUseCase(IntegrationPort integrationPort) {
        this.integrationPort = Objects.requireNonNull(integrationPort, "IntegrationPort must not be null");
    }

    /**
     * Dispatches the given webhook payload.
     *
     * @param payload the webhook to dispatch
     * @return the dispatch result
     */
    public DispatchResult execute(WebhookPayload payload) {
        Objects.requireNonNull(payload, "Payload must not be null");
        if (payload.targetUrl() == null || payload.targetUrl().isBlank()) {
            throw new IllegalArgumentException("Target URL must not be blank");
        }
        return integrationPort.dispatch(payload);
    }
}
