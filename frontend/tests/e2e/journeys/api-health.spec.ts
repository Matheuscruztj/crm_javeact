import { test, expect } from "@playwright/test";

/**
 * API Health Check — AtlasOps AI
 *
 * Validates that the backend API health endpoint is reachable.
 * Migrated from tests/functional/tests/health.spec.ts.
 */
test.describe("API Health Check", () => {
  test("should_respondWithOk_when_healthEndpointIsReachable", async ({ request }) => {
    const response = await request.get(
      `${process.env.API_URL ?? "http://localhost:8080"}/actuator/health`
    );
    expect(response.status()).toBe(200);
  });
});
