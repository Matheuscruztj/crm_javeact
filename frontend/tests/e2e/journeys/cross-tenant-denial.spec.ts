import { test, expect } from "../fixtures/test-fixtures";

/**
 * E2E Journey P0.B.2.6: Cross-tenant access denial
 * Validates: P0.B.2 — Critical user journeys
 */
test.describe("Security: Cross-tenant access denial", () => {
  test("should redirect unauthenticated users to login", async ({ page }) => {
    await page.goto("/admin/customers");
    await expect(page).toHaveURL(/login/);
  });

  test("should redirect unauthenticated portal users to login", async ({
    page,
  }) => {
    await page.goto("/portal/requests");
    await expect(page).toHaveURL(/login/);
  });

  test("should not expose tenant data in page source without auth", async ({
    page,
  }) => {
    const response = await page.goto("/api/v1/customers", {
      waitUntil: "domcontentloaded",
    });
    // API should return 401 for unauthenticated requests
    expect(response?.status()).toBe(401);
  });
});
