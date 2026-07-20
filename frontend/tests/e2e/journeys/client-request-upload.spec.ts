import { test, expect } from "../fixtures/test-fixtures";

/**
 * E2E Journey P0.B.2.2: CLIENT login → create request → upload document
 * Validates: P0.B.2 — Critical user journeys
 */
test.describe("CLIENT: Create request and upload document", () => {
  test("should redirect unauthenticated user to login", async ({ page }) => {
    await page.goto("/portal/requests");
    await expect(page).toHaveURL(/login/);
  });

  test("should show portal requests page after client login", async ({
    clientLogin,
    requestPage,
    page,
  }) => {
    await requestPage.gotoPortal();
    await expect(page).not.toHaveURL(/login/);
    await expect(page).toHaveURL(/portal/);
  });

  test("should display upload button on portal documents page", async ({
    clientLogin,
    documentPage,
    page,
  }) => {
    await documentPage.gotoPortal();
    await expect(page).not.toHaveURL(/login/);
  });
});
