/**
 * k6 Average Load Test — AtlasOps AI
 * Validates: P1.18 — k6 average load scenario
 *
 * Runs 50 VUs for 5 minutes with a realistic traffic mix:
 * - 40% health checks
 * - 30% login attempts
 * - 20% list customers
 * - 10% search
 *
 * Thresholds: p95 < 1000ms, p99 < 2000ms, error_rate < 2%
 *
 * Usage:
 *   k6 run tests/load/average.js
 *   k6 run --env K6_BASE_URL=http://localhost:8080 --env K6_AUTH_TOKEN=<jwt> tests/load/average.js
 */

import http from "k6/http";
import { check, group, sleep } from "k6";
import { Rate, Trend } from "k6/metrics";

const BASE_URL = __ENV.K6_BASE_URL || __ENV.BASE_URL || "http://localhost:8080";
const AUTH_TOKEN = __ENV.K6_AUTH_TOKEN || "";

export const errorRate = new Rate("errors");
export const healthDuration = new Trend("health_duration");
export const loginDuration = new Trend("login_duration");
export const listCustomersDuration = new Trend("list_customers_duration");
export const searchDuration = new Trend("search_duration");

export const options = {
  vus: 50,
  duration: "5m",
  thresholds: {
    http_req_duration: ["p(95)<1000", "p(99)<2000"],
    errors: ["rate<0.02"],
    health_duration: ["p(95)<500"],
    login_duration: ["p(95)<1000"],
    list_customers_duration: ["p(95)<1500"],
    search_duration: ["p(95)<2000"],
  },
};

function buildHeaders() {
  const headers = { "Content-Type": "application/json" };
  if (AUTH_TOKEN) {
    headers["Authorization"] = `Bearer ${AUTH_TOKEN}`;
  }
  return headers;
}

function scenarioHealth() {
  group("health", () => {
    const res = http.get(`${BASE_URL}/actuator/health`);
    healthDuration.add(res.timings.duration);

    const ok = check(res, {
      "health: status 200": (r) => r.status === 200,
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

function scenarioLogin() {
  group("login", () => {
    const res = http.post(
      `${BASE_URL}/api/v1/auth/login`,
      JSON.stringify({
        email: "perf-test@atlasops.test",
        password: "wrong-password",
      }),
      { headers: { "Content-Type": "application/json" } },
    );
    loginDuration.add(res.timings.duration);

    const ok = check(res, {
      "login: endpoint reachable": (r) => r.status < 500,
    });
    errorRate.add(!ok);
  });
}

function scenarioListCustomers() {
  group("list_customers", () => {
    const headers = buildHeaders();
    const res = http.get(`${BASE_URL}/api/v1/customers?page=0&size=20`, {
      headers,
    });
    listCustomersDuration.add(res.timings.duration);

    const ok = check(res, {
      "list customers: status not 5xx": (r) => r.status < 500,
    });
    errorRate.add(!ok);
  });
}

function scenarioSearch() {
  group("search", () => {
    const headers = buildHeaders();
    const res = http.get(`${BASE_URL}/api/v1/search?q=test&page=0&size=10`, {
      headers,
    });
    searchDuration.add(res.timings.duration);

    const ok = check(res, {
      "search: status not 5xx": (r) => r.status < 500,
    });
    errorRate.add(!ok);
  });
}

export default function () {
  const rand = Math.random();

  if (rand < 0.4) {
    // 40% — health checks
    scenarioHealth();
    sleep(0.5);
  } else if (rand < 0.7) {
    // 30% — login
    scenarioLogin();
    sleep(1);
  } else if (rand < 0.9) {
    // 20% — list customers
    scenarioListCustomers();
    sleep(1);
  } else {
    // 10% — search
    scenarioSearch();
    sleep(1.5);
  }
}
