import { test, expect } from "../fixtures/test-fixtures";

/**
 * E2E Journey P0.B.2.7: Activity feed and dashboard updates
 * Validates: P0.B.2 — Critical user journeys
 */
test.describe("ADMIN: Activity feed and dashboard", () => {
  test("should redirect unauthenticated user from activity feed", async ({
    page,
  }) => {
    await page.goto("/admin/activities");
    await expect(page).toHaveURL(/login/);
  });

  test("should show admin dashboard after login", async ({
    adminLogin,
    page,
  }) => {
    await page.goto("/admin");
    await expect(page).not.toHaveURL(/login/);
    await expect(page).toHaveURL(/admin/);
  });

  test("should show analytics page after admin login", async ({
    adminLogin,
    page,
  }) => {
    await page.goto("/admin/analytics");
    await expect(page).not.toHaveURL(/login/);
  });
});
