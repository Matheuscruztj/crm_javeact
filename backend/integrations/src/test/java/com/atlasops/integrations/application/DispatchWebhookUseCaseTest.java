package com.atlasops.integrations.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlasops.integrations.domain.DispatchResult;
import com.atlasops.integrations.domain.WebhookPayload;
import com.atlasops.integrations.domain.ports.IntegrationPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link DispatchWebhookUseCase}.
 * Validates: P0.C.1 — Integrations module
 */
@ExtendWith(MockitoExtension.class)
class DispatchWebhookUseCaseTest {

    @Mock
    private IntegrationPort integrationPort;

    @InjectMocks
    private DispatchWebhookUseCase useCase;

    @Test
    void should_dispatchWebhook_when_payloadIsValid() {
        WebhookPayload payload = new WebhookPayload(
                "https://external.example.com/webhook", "customer.created", "{}", null);
        DispatchResult expected = new DispatchResult("dispatch-001", "DELIVERED", 200);
        when(integrationPort.dispatch(any())).thenReturn(expected);

        DispatchResult result = useCase.execute(payload);

        assertThat(result.status()).isEqualTo("DELIVERED");
        assertThat(result.httpStatusCode()).isEqualTo(200);
        verify(integrationPort).dispatch(payload);
    }

    @Test
    void should_rejectDispatch_when_targetUrlIsBlank() {
        WebhookPayload payload = new WebhookPayload("", "customer.created", "{}", null);

        assertThatThrownBy(() -> useCase.execute(payload))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Target URL");
    }

    @Test
    void should_rejectDispatch_when_payloadIsNull() {
        assertThatThrownBy(() -> useCase.execute(null))
                .isInstanceOf(NullPointerException.class);
    }
}
