package com.atlasops.integrations.domain;

/**
 * Represents a webhook payload to be dispatched to an external system.
 *
 * @param targetUrl the destination URL for the webhook
 * @param eventType the type of event being dispatched
 * @param payload the JSON payload content to deliver
 * @param headers additional HTTP headers to include in the request
 */
public record WebhookPayload(String targetUrl, String eventType, String payload, String headers) {}
