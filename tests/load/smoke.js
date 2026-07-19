import http from "k6/http";
import { check, sleep } from "k6";

/**
 * Smoke Load Test — AtlasOps AI
 *
 * Minimal load scenario to verify the API is responsive under light traffic.
 * Max 5 VUs, max 60 seconds total duration.
 *
 * Usage:
 *   k6 run tests/load/smoke.js
 *   k6 run --env BASE_URL=http://localhost:9090 tests/load/smoke.js
 */

const BASE_URL = __ENV.BASE_URL || "http://localhost:8080";

export const options = {
  stages: [
    { duration: "15s", target: 5 }, // ramp-up to 5 VUs over 15s
    { duration: "30s", target: 5 }, // stay at 5 VUs for 30s
    { duration: "15s", target: 0 }, // ramp-down to 0 over 15s
  ],
  thresholds: {
    http_req_duration: ["p(95)<2000"], // 95% of requests under 2s
    http_req_failed: ["rate<0.1"], // less than 10% failure rate
  },
};

export default function () {
  const res = http.get(`${BASE_URL}/actuator/health`);

  check(res, {
    "status is 200": (r) => r.status === 200,
    "response time < 2000ms": (r) => r.timings.duration < 2000,
  });

  sleep(1);
}
