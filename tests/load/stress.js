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
    http_req_duration: ["p(95)<5000"], // 95% of requests under 5s (relaxed for stress)
    http_req_failed: ["rate<0.3"], // up to 30% failures acceptable under stress
  },
};

export default function () {
  const res = http.get(`${BASE_URL}/actuator/health`);

  check(res, {
    "status is 200": (r) => r.status === 200,
    "response time < 5000ms": (r) => r.timings.duration < 5000,
  });

  sleep(0.5);
}
