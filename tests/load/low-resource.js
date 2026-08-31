/**
 * k6 Low-Resource Load Test — AtlasOps AI
 *
 * Validates a constrained deployment profile meant to approximate a host with
 * 1 vCPU and 1 GB RAM or less.
 *
 * The goal is not raw throughput. The goal is to answer a simpler question:
 * can the core request mix survive with acceptable p95/p99 latency while the
 * backend and its dependencies are running under minimum practical resources?
 *
 * Usage:
 *   k6 run tests/load/low-resource.js
 *   k6 run --env K6_BASE_URL=http://localhost:8080 --env K6_P95_MS=1200 tests/load/low-resource.js
 */

import http from "k6/http";
import { check, group, sleep } from "k6";
import { Rate, Trend } from "k6/metrics";

const BASE_URL = __ENV.K6_BASE_URL || __ENV.BASE_URL || "http://localhost:8080";
const AUTH_TOKEN = __ENV.K6_AUTH_TOKEN || "";
const TENANT_ID = __ENV.K6_TENANT_ID || "tenant-alpha";
const P95_MS = Number(__ENV.K6_P95_MS || 1200);
const P99_MS = Number(__ENV.K6_P99_MS || 2000);
const ERROR_RATE = Number(__ENV.K6_ERROR_RATE || 0.01);

export const errorRate = new Rate("errors");
export const healthLatency = new Trend("health_latency_ms", true);
export const customerLatency = new Trend("customer_latency_ms", true);
export const searchLatency = new Trend("search_latency_ms", true);
export const metricsLatency = new Trend("metrics_latency_ms", true);

export const options = {
  stages: [
    { duration: "20s", target: 1 },
    { duration: "2m", target: 1 },
    { duration: "20s", target: 0 },
  ],
  thresholds: {
    http_req_duration: [`p(95)<${P95_MS}`, `p(99)<${P99_MS}`],
    http_req_failed: [`rate<${ERROR_RATE}`],
    errors: [`rate<${ERROR_RATE}`],
    health_latency_ms: [`p(95)<${Number(__ENV.K6_HEALTH_P95_MS || 300)}`],
    customer_latency_ms: [`p(95)<${Number(__ENV.K6_CUSTOMERS_P95_MS || 800)}`],
    search_latency_ms: [`p(95)<${Number(__ENV.K6_SEARCH_P95_MS || 1200)}`],
    metrics_latency_ms: [`p(95)<${Number(__ENV.K6_METRICS_P95_MS || 400)}`],
  },
};

function authHeaders() {
  const headers = {
    "Content-Type": "application/json",
    "X-Tenant-ID": TENANT_ID,
  };
  if (AUTH_TOKEN) {
    headers.Authorization = `Bearer ${AUTH_TOKEN}`;
  }
  return headers;
}

function healthCheck() {
  group("health", () => {
    const res = http.get(`${BASE_URL}/actuator/health`, {
      tags: { endpoint: "health" },
    });
    healthLatency.add(res.timings.duration);
    const ok = check(res, {
      "health: HTTP 200": (r) => r.status === 200,
      "health: body UP": (r) => {
        try {
          return JSON.parse(r.body).status === "UP";
        } catch {
          return false;
        }
      },
    });
    errorRate.add(!ok);
  });
}

function customerList() {
  group("customers", () => {
    const res = http.get(`${BASE_URL}/api/v1/customers?page=0&size=10`, {
      headers: authHeaders(),
      tags: { endpoint: "customers_list" },
    });
    customerLatency.add(res.timings.duration);
    const ok = check(res, {
      "customers: not 5xx": (r) => r.status < 500,
      "customers: auth enforced or success": (r) =>
        AUTH_TOKEN ? r.status === 200 : r.status === 401,
    });
    errorRate.add(!ok);
  });
}

function search() {
  group("search", () => {
    const res = http.get(`${BASE_URL}/api/v1/search?q=test&page=0&size=10`, {
      headers: authHeaders(),
      tags: { endpoint: "search" },
    });
    searchLatency.add(res.timings.duration);
    const ok = check(res, {
      "search: not 5xx": (r) => r.status < 500,
    });
    errorRate.add(!ok);
  });
}

function metrics() {
  group("metrics", () => {
    const res = http.get(`${BASE_URL}/actuator/prometheus`, {
      tags: { endpoint: "prometheus" },
    });
    metricsLatency.add(res.timings.duration);
    const ok = check(res, {
      "metrics: 200 or 403": (r) => r.status === 200 || r.status === 403,
    });
    errorRate.add(!ok);
  });
}

export default function () {
  const rand = Math.random();
  if (rand < 0.35) {
    healthCheck();
    sleep(0.4);
  } else if (rand < 0.7) {
    customerList();
    sleep(0.7);
  } else if (rand < 0.9) {
    search();
    sleep(0.8);
  } else {
    metrics();
    sleep(0.4);
  }
}
