import { test, expect } from "../fixtures/test-fixtures";

/**
 * E2E Journey P0.B.2.4: ANALYST login → approve/reject document
 * Validates: P0.B.2 — Critical user journeys
 */
test.describe("ANALYST: Approve and reject documents", () => {
  test("should redirect unauthenticated user to login from approvals", async ({ page }) => {
    await page.goto("/admin/approvals");
    await expect(page).toHaveURL(/login/);
  });

  test("should show approvals page after analyst login", async ({
    analystLogin,
    approvalPage,
    page,
  }) => {
    await approvalPage.gotoAdmin();
    await expect(page).not.toHaveURL(/login/);
  });
});
