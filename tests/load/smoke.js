/**
 * k6 Smoke Test — AtlasOps AI
 * Validates: P0.T.3.2 — k6 smoke test against running API
 *
 * Runs 2 VUs for 30 seconds, checks:
 * - /actuator/health returns 200 with status UP
 * - /api/v1/auth/login returns 200 or 401 (endpoint is reachable)
 * - Response times < 2s p95
 *
 * Usage:
 *   k6 run tests/load/smoke.js
 *   k6 run --env K6_BASE_URL=http://localhost:8080 tests/load/smoke.js
 */

import http from "k6/http";
import { check, sleep } from "k6";
import { Rate } from "k6/metrics";

const BASE_URL = __ENV.K6_BASE_URL || __ENV.BASE_URL || "http://localhost:8080";
const TENANT_ID = __ENV.K6_TENANT_ID || __ENV.TENANT_ID || "tenant-smoke";
const P95_MS = Number(__ENV.K6_P95_MS || 2000);
const ERROR_RATE = Number(__ENV.K6_ERROR_RATE || 0.1);

export const errorRate = new Rate("errors");

export const options = {
  vus: 2,
  duration: "30s",
  thresholds: {
    http_req_duration: [`p(95)<${P95_MS}`],
    errors: [`rate<${ERROR_RATE}`],
  },
};

export default function () {
  // 1. Health check
  const health = http.get(`${BASE_URL}/actuator/health`);
  const healthOk = check(health, {
    "health status 200": (r) => r.status === 200,
    "health UP": (r) => {
      try {
        return JSON.parse(r.body).status === "UP";
      } catch {
        return false;
      }
    },
  });
  errorRate.add(!healthOk);

  sleep(0.5);

  // 2. Login endpoint reachable (returns 401 without credentials)
  const login = http.post(
    `${BASE_URL}/api/v1/auth/login`,
    JSON.stringify({ email: "test@example.com", password: "wrong" }),
    {
      headers: {
        "Content-Type": "application/json",
        "X-Tenant-ID": TENANT_ID,
      },
    },
  );
  const loginOk = check(login, {
    "login endpoint reachable": (r) => r.status < 500,
  });
  errorRate.add(!loginOk);

  sleep(0.5);

  // 3. Actuator prometheus metrics endpoint
  const metrics = http.get(`${BASE_URL}/actuator/prometheus`);
  check(metrics, {
    "prometheus metrics reachable": (r) => r.status === 200 || r.status === 403,
  });

  sleep(1);
}
