package com.atlasops.integrations.domain.ports;

import com.atlasops.integrations.domain.DispatchResult;
import com.atlasops.integrations.domain.WebhookPayload;

/**
 * Port defining the contract for external integration operations. Implementations handle webhook
 * dispatch to external systems.
 */
public interface IntegrationPort {

  /**
   * Dispatches a webhook payload to an external system.
   *
   * @param payload the webhook payload to dispatch
   * @return the result of the dispatch operation
   */
  DispatchResult dispatch(WebhookPayload payload);
}
