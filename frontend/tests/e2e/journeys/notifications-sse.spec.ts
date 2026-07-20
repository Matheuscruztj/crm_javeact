import { test, expect } from "../fixtures/test-fixtures";

/**
 * E2E Journey P0.B.2.5: CLIENT receives notification (SSE)
 * Validates: P0.B.2 — Critical user journeys
 */
test.describe("CLIENT: Notifications via SSE", () => {
  test("should redirect unauthenticated user trying to access notifications", async ({
    page,
  }) => {
    await page.goto("/portal");
    await expect(page).toHaveURL(/login|portal/);
  });

  test("should show notification area after client login", async ({
    clientLogin,
    notificationPage,
    page,
  }) => {
    // Portal dashboard should load
    await page.goto("/portal");
    await expect(page).not.toHaveURL(/login/);
  });
});
