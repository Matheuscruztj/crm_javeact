package com.atlasops.integrations.presentation;

import com.atlasops.integrations.application.DispatchWebhookUseCase;
import com.atlasops.integrations.domain.DispatchResult;
import com.atlasops.integrations.domain.WebhookPayload;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for integration operations (webhook dispatch).
 *
 * <p>Endpoints:
 * <ul>
 *   <li>POST /api/v1/integrations/webhooks/dispatch — dispatch a webhook
 * </ul>
 *
 * <p>Validates: P0.C.1 — Integrations Module (Adapters)
 */
@RestController
@RequestMapping("/api/v1/integrations")
public class IntegrationController {

  private final DispatchWebhookUseCase dispatchWebhookUseCase;

  public IntegrationController(DispatchWebhookUseCase dispatchWebhookUseCase) {
    this.dispatchWebhookUseCase = dispatchWebhookUseCase;
  }

  /**
   * Dispatches a webhook payload to an external URL.
   *
   * @param tenantId the tenant identifier
   * @param request the webhook dispatch request
   * @return 200 OK with dispatch result
   */
  @PostMapping("/webhooks/dispatch")
  public ResponseEntity<DispatchResult> dispatch(
      @RequestHeader("X-Tenant-ID") String tenantId,
      @RequestBody WebhookDispatchRequest request) {

    var payload = new WebhookPayload(
        request.targetUrl(),
        request.eventType(),
        request.payload(),
        request.headers());

    DispatchResult result = dispatchWebhookUseCase.execute(payload);
    return ResponseEntity.ok(result);
  }

  /**
   * Request body for webhook dispatch.
   */
  public record WebhookDispatchRequest(
      String targetUrl,
      String eventType,
      String payload,
      String headers) {}

  /**
   * Returns SSRF validation status for a target URL (admin utility).
   */
  @PostMapping("/webhooks/validate-url")
  public ResponseEntity<Map<String, Object>> validateUrl(
      @RequestHeader("X-Tenant-ID") String tenantId,
      @RequestBody Map<String, String> body) {

    String url = body.get("url");
    boolean safe = com.atlasops.integrations.infrastructure.SSRFValidator.isSafe(url);
    return ResponseEntity.ok(Map.of(
        "url", url != null ? url : "",
        "safe", safe));
  }
}
