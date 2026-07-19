import { test, expect } from "@playwright/test";

test.describe("Health Check", () => {
  test("should_respondWithOk_when_healthEndpointIsReachable", async ({
    request,
  }) => {
    const response = await request.get("/actuator/health");
    expect(response.status()).toBe(200);
  });
});
