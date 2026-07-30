/**
 * k6 Stress Test — AtlasOps AI (P3.4)
 *
 * Pushes the system beyond normal load to find breaking points.
 * Ramps up to 200 VUs in stages to identify degradation thresholds.
 *
 * Usage:
 *   k6 run tests/load/stress.js
 *   k6 run --env K6_BASE_URL=http://localhost:8080 tests/load/stress.js
 *
 * Makefile: make test-load-stress
 */

import http from "k6/http";
import { check, sleep } from "k6";

const BASE_URL = __ENV.K6_BASE_URL || __ENV.BASE_URL || "http://localhost:8080";
const P95_MS = Number(__ENV.K6_P95_MS || 5000);
const ERROR_RATE = Number(__ENV.K6_ERROR_RATE || 0.3);

export const options = {
  stages: [
    { duration: "30s", target: 10 }, // warm-up
    { duration: "1m", target: 30 }, // ramp to moderate load
    { duration: "1m", target: 50 }, // ramp to high load
    { duration: "2m", target: 50 }, // sustain high load
    { duration: "1m", target: 80 }, // push beyond normal capacity
    { duration: "1m", target: 80 }, // sustain peak
    { duration: "30s", target: 0 }, // ramp-down
  ],
  thresholds: {
    http_req_duration: [`p(95)<${P95_MS}`], // 95% of requests under configurable limit
    http_req_failed: [`rate<${ERROR_RATE}`], // acceptable failure envelope under stress
  },
};

export function setup() {
  console.log(
    `[Setup] Stress scenario baseUrl=${BASE_URL} p95=${P95_MS}ms errorRate=${ERROR_RATE}`,
  );
  return { scenario: "stress", baseUrl: BASE_URL };
}

export default function () {
  const res = http.get(`${BASE_URL}/actuator/health`);

  check(res, {
    "status is 200": (r) => r.status === 200,
    "response time < 5000ms": (r) => r.timings.duration < 5000,
  });

  sleep(0.5);
}

export function teardown(data) {
  console.log(`[Teardown] Scenario=${data.scenario} baseUrl=${data.baseUrl}`);
}
