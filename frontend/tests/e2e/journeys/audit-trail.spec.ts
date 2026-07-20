import { test, expect } from "../fixtures/test-fixtures";

/**
 * E2E Journey P0.B.2.10: Audit trail verification
 * Validates: P0.B.2 — Critical user journeys
 */
test.describe("ADMIN: Audit trail", () => {
  test("should redirect unauthenticated user from audit", async ({ page }) => {
    await page.goto("/admin/audit");
    await expect(page).toHaveURL(/login/);
  });

  test("should show audit page after admin login", async ({
    adminLogin,
    page,
  }) => {
    await page.goto("/admin/audit");
    await expect(page).not.toHaveURL(/login/);
  });
});
