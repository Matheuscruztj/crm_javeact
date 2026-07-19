package com.atlasops.analytics.domain;

/** Well-known metric names tracked by the analytics module. */
public enum MetricName {
  /** Total number of customers in the tenant. */
  CUSTOMER_COUNT,
  /** Total number of service requests. */
  REQUEST_COUNT,
  /** Number of requests currently in open/in-progress status. */
  ACTIVE_REQUEST_COUNT,
  /** Number of requests created in the last 30 days. */
  REQUEST_COUNT_LAST_30D,
  /** Total number of documents. */
  DOCUMENT_COUNT,
  /** Number of documents processed by AI. */
  AI_ANALYZED_DOCUMENT_COUNT,
  /** Average AI analysis confidence score (0.0–1.0). */
  AI_AVG_CONFIDENCE,
  /** Number of pending approvals. */
  PENDING_APPROVAL_COUNT
}
