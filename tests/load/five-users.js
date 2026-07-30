/**
 * k6 Load Test — 5 Concurrent Users
 *
 * Purpose: Validate that the system handles 5 simultaneous users correctly
 * across a realistic traffic mix. This is the minimum viable load scenario
 * for confirming basic concurrency safety and response time guarantees.
 *
 * Scenario design:
 *   - Ramp up: 0 → 5 VUs over 30s
 *   - Sustained: 5 VUs for 2 minutes
 *   - Ramp down: 5 → 0 VUs over 30s
 *   - Total duration: ~3 minutes
 *
 * Traffic mix per VU:
 *   - 40% health checks      (public endpoint, fast baseline)
 *   - 25% login attempts     (auth endpoint, validates JWT issuance speed)
 *   - 20% list resources     (authenticated, validates DB query under load)
 *   - 10% search queries     (text search, validates Postgres FTS performance)
 *   -  5% actuator metrics   (Prometheus scrape, validates observability overhead)
 *
 * Thresholds (strict for 5 users — should easily pass):
 *   - p95 response time < 500ms  (5 users should be well under capacity)
 *   - p99 response time < 1500ms
 *   - error rate < 1%            (strict — 5 users should never error)
 *   - health check p95 < 200ms
 *
 * Usage:
 *   k6 run tests/load/five-users.js --env K6_BASE_URL=http://localhost:8080
 *   k6 run tests/load/five-users.js --env K6_BASE_URL=http://localhost:8080 --env K6_AUTH_TOKEN=<jwt>
 *
 * Makefile: make test-load-5vu
 */

import http from "k6/http";
import { check, group, sleep } from "k6";
import { Rate, Trend, Counter } from "k6/metrics";

const BASE_URL = __ENV.K6_BASE_URL || __ENV.BASE_URL || "http://localhost:8080";
const AUTH_TOKEN = __ENV.K6_AUTH_TOKEN || "";
const TENANT_ID = __ENV.K6_TENANT_ID || "tenant-alpha";
const P95_MS = Number(__ENV.K6_P95_MS || 500);
const P99_MS = Number(__ENV.K6_P99_MS || 1500);
const ERROR_RATE = Number(__ENV.K6_ERROR_RATE || 0.01);

// ─── Custom Metrics ────────────────────────────────────────────────────────

export const errorRate = new Rate("errors");
export const healthLatency = new Trend("health_latency_ms", true);
export const loginLatency = new Trend("login_latency_ms", true);
export const listLatency = new Trend("list_resources_ms", true);
export const searchLatency = new Trend("search_latency_ms", true);
export const metricsLatency = new Trend("metrics_latency_ms", true);
export const concurrentReqs = new Counter("concurrent_requests_total");

// ─── Test Configuration ────────────────────────────────────────────────────

export const options = {
  stages: [
    { duration: "30s", target: 5 }, // Ramp up to 5 users
    { duration: "2m", target: 5 }, // Hold at 5 users for 2 minutes
    { duration: "30s", target: 0 }, // Ramp down
  ],
  thresholds: {
    http_req_duration: [`p(95)<${P95_MS}`, `p(99)<${P99_MS}`],
    errors: [`rate<${ERROR_RATE}`],
    health_latency_ms: [`p(95)<${Number(__ENV.K6_HEALTH_P95_MS || 200)}`],
    login_latency_ms: [`p(95)<${Number(__ENV.K6_LOGIN_P95_MS || 800)}`],
    list_resources_ms: [`p(95)<${Number(__ENV.K6_LIST_P95_MS || 600)}`],
    search_latency_ms: [`p(95)<${Number(__ENV.K6_SEARCH_P95_MS || 1000)}`],
    metrics_latency_ms: [`p(95)<${Number(__ENV.K6_METRICS_P95_MS || 300)}`],
    http_req_failed: [`rate<${ERROR_RATE}`],
  },
};

// ─── Headers ──────────────────────────────────────────────────────────────

function authHeaders() {
  const h = {
    "Content-Type": "application/json",
    "X-Tenant-ID": TENANT_ID,
  };
  if (AUTH_TOKEN) {
    h["Authorization"] = `Bearer ${AUTH_TOKEN}`;
  }
  return h;
}

// ─── Scenarios ────────────────────────────────────────────────────────────

function doHealthCheck() {
  group("health_check", () => {
    concurrentReqs.add(1);
    const res = http.get(`${BASE_URL}/actuator/health`, {
      tags: { endpoint: "health" },
    });
    healthLatency.add(res.timings.duration);

    const passed = check(res, {
      "health: HTTP 200": (r) => r.status === 200,
      "health: status field present": (r) => {
        try {
          return JSON.parse(r.body).status !== undefined;
        } catch {
          return false;
        }
      },
      "health: response time < 200ms": (r) => r.timings.duration < 200,
    });
    errorRate.add(!passed);
  });
}

function doLoginAttempt() {
  group("login_attempt", () => {
    concurrentReqs.add(1);
    const res = http.post(
      `${BASE_URL}/api/v1/auth/login`,
      JSON.stringify({
        email: `load-test-${__VU}-${__ITER}@atlasops.test`,
        password: "load-test-wrong-password",
      }),
      {
        headers: {
          "Content-Type": "application/json",
          "X-Tenant-ID": TENANT_ID,
        },
        tags: { endpoint: "auth_login" },
      },
    );
    loginLatency.add(res.timings.duration);

    const passed = check(res, {
      "login: endpoint reachable (not 5xx)": (r) => r.status < 500,
      "login: returns 401 for bad creds": (r) =>
        r.status === 401 || r.status === 400,
      "login: response time < 800ms": (r) => r.timings.duration < 800,
    });
    errorRate.add(!passed);
  });
}

function doListResources() {
  group("list_resources", () => {
    concurrentReqs.add(1);
    const res = http.get(`${BASE_URL}/api/v1/customers?page=0&size=10`, {
      headers: authHeaders(),
      tags: { endpoint: "customers_list" },
    });
    listLatency.add(res.timings.duration);

    const passed = check(res, {
      "list: not a server error (5xx)": (r) => r.status < 500,
      "list: response time < 600ms": (r) => r.timings.duration < 600,
      "list: auth enforced (401 without token)": (r) =>
        AUTH_TOKEN ? r.status === 200 : r.status === 401,
    });
    errorRate.add(!passed);
  });
}

function doSearchQuery() {
  group("search_query", () => {
    concurrentReqs.add(1);
    const terms = ["alpha", "customer", "request", "document", "beta"];
    const term = terms[Math.floor(Math.random() * terms.length)];

    const res = http.get(`${BASE_URL}/api/v1/search?q=${term}&page=0&size=10`, {
      headers: authHeaders(),
      tags: { endpoint: "search" },
    });
    searchLatency.add(res.timings.duration);

    const passed = check(res, {
      "search: not a server error (5xx)": (r) => r.status < 500,
      "search: response time < 1000ms": (r) => r.timings.duration < 1000,
    });
    errorRate.add(!passed);
  });
}

function doActuatorMetrics() {
  group("actuator_metrics", () => {
    concurrentReqs.add(1);
    const res = http.get(`${BASE_URL}/actuator/prometheus`, {
      tags: { endpoint: "prometheus" },
    });
    metricsLatency.add(res.timings.duration);

    const passed = check(res, {
      "metrics: HTTP 200": (r) => r.status === 200,
      "metrics: contains jvm data": (r) => r.body && r.body.includes("jvm_"),
      "metrics: response time < 300ms": (r) => r.timings.duration < 300,
    });
    errorRate.add(!passed);
  });
}

// ─── Main VU Loop ─────────────────────────────────────────────────────────

export default function () {
  const rand = Math.random();

  if (rand < 0.4) {
    doHealthCheck();
    sleep(0.3);
  } else if (rand < 0.65) {
    doLoginAttempt();
    sleep(0.8);
  } else if (rand < 0.85) {
    doListResources();
    sleep(0.5);
  } else if (rand < 0.95) {
    doSearchQuery();
    sleep(1.0);
  } else {
    doActuatorMetrics();
    sleep(0.5);
  }
}

// ─── Setup / Teardown ─────────────────────────────────────────────────────

export function setup() {
  console.log(`[Setup] Starting 5-VU load test against ${BASE_URL}`);
  console.log(`[Setup] Tenant: ${TENANT_ID}`);
  console.log(
    `[Setup] Auth token: ${AUTH_TOKEN ? "provided" : "not provided (expect 401 on protected endpoints)"}`,
  );
  console.log(
    `[Setup] Thresholds: p95=${P95_MS}ms p99=${P99_MS}ms errorRate=${ERROR_RATE}`,
  );

  const health = http.get(`${BASE_URL}/actuator/health`);
  if (health.status !== 200) {
    console.warn(
      `[Setup] WARNING: Health check returned ${health.status} — API may not be ready`,
    );
  } else {
    console.log(`[Setup] API is healthy: ${health.body}`);
  }

  return { startTime: new Date().toISOString(), baseUrl: BASE_URL };
}

export function teardown(data) {
  console.log(`[Teardown] Test completed. Started: ${data.startTime}`);

  const health = http.get(`${BASE_URL}/actuator/health`);
  console.log(`[Teardown] Post-test health: ${health.status} — ${health.body}`);

  if (health.status === 200) {
    console.log("[Teardown] ✓ System is healthy after load test");
  } else {
    console.warn(
      `[Teardown] ⚠ System returned ${health.status} after load test`,
    );
  }
}
