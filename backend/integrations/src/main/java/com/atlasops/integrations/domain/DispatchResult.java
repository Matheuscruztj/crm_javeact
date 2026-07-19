package com.atlasops.integrations.domain;

/**
 * Represents the result of a webhook dispatch operation.
 *
 * @param dispatchId the unique identifier assigned to the dispatch
 * @param status the dispatch status (e.g., DELIVERED, FAILED, RETRYING)
 * @param httpStatusCode the HTTP status code returned by the target, or -1 if unreachable
 */
public record DispatchResult(String dispatchId, String status, int httpStatusCode) {}
