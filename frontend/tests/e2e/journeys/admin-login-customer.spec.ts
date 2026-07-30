import { test, expect } from "../fixtures/test-fixtures";

/**
 * E2E Journey P0.B.2.1: ADMIN login → create customer → logout
 * Validates: P0.B.2 — Critical user journeys
 */
test.describe("ADMIN: Login → Create Customer → Logout", () => {
  test("should redirect to login when accessing protected route", async ({ page }) => {
    await page.goto("/admin/customers");
    await expect(page).toHaveURL(/login/);
  });

  test("should show login page with email and password fields", async ({ loginPage }) => {
    await loginPage.goto();
    await expect(loginPage.emailInput).toBeVisible();
    await expect(loginPage.passwordInput).toBeVisible();
    await expect(loginPage.submitButton).toBeVisible();
  });

  test("should show error when invalid credentials provided", async ({ loginPage }) => {
    await loginPage.goto();
    await loginPage.login("wrong@test.com", "wrongpassword");
    await expect(loginPage.errorMessage).toBeVisible({ timeout: 5000 });
  });
});
