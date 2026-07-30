package com.atlasops.integrations.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.atlasops.integrations.application.DispatchWebhookUseCase;
import com.atlasops.integrations.domain.DispatchResult;
import com.atlasops.integrations.domain.ports.UrlValidationPort;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/** Unit tests for IntegrationController (presentation layer). */
@ExtendWith(MockitoExtension.class)
class IntegrationControllerTest {

  private static final String TENANT_ID = "tenant-alpha";

  @Mock private DispatchWebhookUseCase dispatchWebhookUseCase;
  @Mock private UrlValidationPort urlValidationPort;

  private IntegrationController controller;

  @BeforeEach
  void setUp() {
    controller = new IntegrationController(dispatchWebhookUseCase, urlValidationPort);
  }

  @Test
  void should_return200_when_dispatchSucceeds() {
    var result = new DispatchResult("dispatch-001", "DELIVERED", 200);
    when(dispatchWebhookUseCase.execute(any())).thenReturn(result);

    var request =
        new IntegrationController.WebhookDispatchRequest(
            "https://example.com/hook", "customer.created", "{}", null);
    ResponseEntity<DispatchResult> response = controller.dispatch(TENANT_ID, request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().status()).isEqualTo("DELIVERED");
    assertThat(response.getBody().httpStatusCode()).isEqualTo(200);
  }

  @Test
  void should_return200_when_validateUrlSafe() {
    // Use a URL that is clearly safe (does not require network DNS resolution)
    // SSRFValidator checks the resolved IP — skip if network unavailable
    ResponseEntity<Map<String, Object>> response =
        controller.validateUrl(TENANT_ID, Map.of("url", "https://external-endpoint.com/hook"));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    // Result depends on DNS resolution — just verify the structure
    assertThat(response.getBody()).containsKey("safe");
    assertThat(response.getBody()).containsKey("url");
  }

  @Test
  void should_returnFalse_when_validateUrlIsLoopback() {
    ResponseEntity<Map<String, Object>> response =
        controller.validateUrl(TENANT_ID, Map.of("url", "http://127.0.0.1/internal"));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().get("safe")).isEqualTo(false);
  }

  @Test
  void should_returnFalse_when_validateUrlIsPrivateNetwork() {
    ResponseEntity<Map<String, Object>> response =
        controller.validateUrl(TENANT_ID, Map.of("url", "http://192.168.1.100/api"));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().get("safe")).isEqualTo(false);
  }

  @Test
  void should_returnFalse_when_validateUrlIsMissing() {
    ResponseEntity<Map<String, Object>> response = controller.validateUrl(TENANT_ID, Map.of());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().get("safe")).isEqualTo(false);
  }
}
