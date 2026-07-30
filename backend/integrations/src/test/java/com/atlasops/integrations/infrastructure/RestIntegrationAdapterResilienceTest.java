package com.atlasops.integrations.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.atlasops.integrations.domain.WebhookPayload;
import java.lang.reflect.Field;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RestIntegrationAdapterResilienceTest {

  @Test
  @DisplayName("should_returnFailedResult_when_externalRestEndpointIsUnavailable")
  void should_returnFailedResult_when_externalRestEndpointIsUnavailable() throws Exception {
    RestIntegrationAdapter adapter = new RestIntegrationAdapter();
    HttpClient httpClient = mock(HttpClient.class);
    Field field = RestIntegrationAdapter.class.getDeclaredField("httpClient");
    field.setAccessible(true);
    field.set(adapter, httpClient);
    when(httpClient.send(
            org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(HttpResponse.BodyHandler.class)))
        .thenThrow(new IOException("connection reset"));

    WebhookPayload payload =
        new WebhookPayload(
            "https://example.com/internal",
            "customer.created",
            "{\"id\":\"cust-1\"}",
            null);

    var result = adapter.dispatch(payload);

    assertThat(result.status()).isEqualTo("FAILED");
    assertThat(result.httpStatusCode()).isEqualTo(-1);
    assertThat(result.dispatchId()).isNotBlank();
  }

  @Test
  @DisplayName("should_returnFailedResult_when_externalRestEndpointReturnsServerError")
  void should_returnFailedResult_when_externalRestEndpointReturnsServerError() throws Exception {
    RestIntegrationAdapter adapter = new RestIntegrationAdapter();
    HttpClient httpClient = mock(HttpClient.class);
    Field field = RestIntegrationAdapter.class.getDeclaredField("httpClient");
    field.setAccessible(true);
    field.set(adapter, httpClient);
    @SuppressWarnings("unchecked")
    HttpResponse<String> response = mock(HttpResponse.class);
    when(response.statusCode()).thenReturn(500);
    when(httpClient.send(
            org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(HttpResponse.BodyHandler.class)))
        .thenReturn(response);

    WebhookPayload payload =
        new WebhookPayload(
            "https://example.com/webhook",
            "customer.created",
            "{\"id\":\"cust-1\"}",
            null);

    var result = adapter.dispatch(payload);

    assertThat(result.status()).isEqualTo("FAILED");
    assertThat(result.dispatchId()).isNotBlank();
  }
}
