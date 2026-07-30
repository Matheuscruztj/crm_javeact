import { test, expect } from "../fixtures/test-fixtures";

/**
 * E2E Journey P0.B.2.9: Operations page and job monitoring
 * Validates: P0.B.2 — Critical user journeys
 */
test.describe("ADMIN: Operations page and job monitoring", () => {
  test("should redirect unauthenticated user from operations", async ({ page }) => {
    await page.goto("/admin/operations");
    await expect(page).toHaveURL(/login/);
  });

  test("should show operations page after admin login", async ({
    adminLogin,
    operationsPage,
    page,
  }) => {
    await operationsPage.goto();
    await expect(page).not.toHaveURL(/login/);
    await expect(page).toHaveURL(/operations/);
  });
});
