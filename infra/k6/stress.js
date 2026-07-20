/**
 * k6 Stress Test — AtlasOps AI
 * Validates: P3.4.1 — 200 VUs, 10min, escalating ramp-up
 *
 * Thresholds: p95 < 3s, error_rate < 10%, no crashes
 * Groups: health checks, auth, customer listing, search
 */

import http from "k6/http";
import { check, group, sleep } from "k6";
import { Rate, Trend } from "k6/metrics";

const BASE_URL = __ENV.K6_BASE_URL || "http://localhost:8080";

export const errorRate = new Rate("errors");
export const authLatency = new Trend("auth_latency");
export const searchLatency = new Trend("search_latency");
export const listLatency = new Trend("list_latency");

export const options = {
  stages: [
    // Ramp-up: 0 → 50 VUs over 2min
    { duration: "2m", target: 50 },
    // Ramp-up: 50 → 100 VUs over 2min
    { duration: "2m", target: 100 },
    // Ramp-up: 100 → 200 VUs over 2min
    { duration: "2m", target: 200 },
    // Sustain: 200 VUs for 2min
    { duration: "2m", target: 200 },
    // Ramp-down: 200 → 0 over 2min
    { duration: "2m", target: 0 },
  ],
  thresholds: {
    http_req_duration: ["p(95)<3000", "p(99)<5000"],
    errors: ["rate<0.10"],
    auth_latency: ["p(95)<1000"],
    search_latency: ["p(95)<2000"],
    list_latency: ["p(95)<1500"],
  },
};

export default function () {
  const rand = Math.random();

  if (rand < 0.3) {
    // 30% — Health checks (lightweight)
    group("health", () => {
      const res = http.get(`${BASE_URL}/actuator/health`, {
        tags: { scenario: "health" },
      });
      const ok = check(res, {
        "health 200": (r) => r.status === 200,
      });
      errorRate.add(!ok);
    });
  } else if (rand < 0.55) {
    // 25% — Auth (login attempt)
    group("auth", () => {
      const start = Date.now();
      const res = http.post(
        `${BASE_URL}/api/v1/auth/login`,
        JSON.stringify({ email: "stress@test.com", password: "wrong" }),
        {
          headers: { "Content-Type": "application/json" },
          tags: { scenario: "auth" },
        },
      );
      authLatency.add(Date.now() - start);
      const ok = check(res, {
        "auth endpoint reachable": (r) => r.status < 500,
      });
      errorRate.add(!ok);
    });
  } else if (rand < 0.8) {
    // 25% — API list endpoint (requires auth, test 401 path)
    group("list", () => {
      const start = Date.now();
      const res = http.get(`${BASE_URL}/api/v1/customers?page=0&size=10`, {
        headers: { "X-Tenant-ID": "tenant-alpha" },
        tags: { scenario: "list" },
      });
      listLatency.add(Date.now() - start);
      const ok = check(res, {
        "list endpoint reachable": (r) => r.status < 500,
      });
      errorRate.add(!ok);
    });
  } else {
    // 20% — Search endpoint
    group("search", () => {
      const start = Date.now();
      const res = http.get(`${BASE_URL}/api/v1/search?q=test&page=0&size=10`, {
        headers: { "X-Tenant-ID": "tenant-alpha" },
        tags: { scenario: "search" },
      });
      searchLatency.add(Date.now() - start);
      const ok = check(res, {
        "search endpoint reachable": (r) => r.status < 500,
      });
      errorRate.add(!ok);
    });
  }

  sleep(0.3 + Math.random() * 0.7);
}
