import http from "k6/http";
import { check, sleep, group } from "k6";

/**
 * Average Load Test — AtlasOps AI
 *
 * Simulates typical production traffic with multiple endpoints.
 * 20 VUs sustained for 2 minutes.
 *
 * Usage:
 *   k6 run tests/load/average.js
 *   k6 run --env BASE_URL=http://localhost:9090 tests/load/average.js
 */

const BASE_URL = __ENV.BASE_URL || "http://localhost:8080";

export const options = {
  stages: [
    { duration: "30s", target: 20 }, // ramp-up to 20 VUs
    { duration: "2m", target: 20 }, // stay at 20 VUs for 2 minutes
    { duration: "30s", target: 0 }, // ramp-down to 0
  ],
  thresholds: {
    http_req_duration: ["p(95)<3000"], // 95% of requests under 3s
    http_req_failed: ["rate<0.05"], // less than 5% failure rate
  },
};

export default function () {
  group("Health Check", () => {
    const res = http.get(`${BASE_URL}/actuator/health`);
    check(res, {
      "health status is 200": (r) => r.status === 200,
    });
  });

  sleep(0.5);

  group("API Info", () => {
    const res = http.get(`${BASE_URL}/actuator/info`);
    check(res, {
      "info status is 200": (r) => r.status === 200,
    });
  });

  sleep(0.5);

  group("Customers List", () => {
    const res = http.get(`${BASE_URL}/api/v1/customers?page=0&size=10`);
    check(res, {
      "customers responds (2xx or 401)": (r) => r.status < 500,
    });
  });

  sleep(1);
}
