import { Page, Locator } from "@playwright/test";

/**
 * Page Object for global search and command palette.
 * Validates: P0.B.1 — Setup Playwright with page objects
 */
export class SearchPage {
  readonly page: Page;
  readonly searchInput: Locator;
  readonly commandPalette: Locator;
  readonly searchResults: Locator;

  constructor(page: Page) {
    this.page = page;
    this.commandPalette = page.getByRole("dialog");
    this.searchInput = page.getByPlaceholder(/search|buscar/i);
    this.searchResults = page.getByTestId("search-results");
  }

  async openCommandPalette() {
    await this.page.keyboard.press("Meta+k");
    // Also try Ctrl+K as fallback
    if (!(await this.commandPalette.isVisible())) {
      await this.page.keyboard.press("Control+k");
    }
    await this.commandPalette.waitFor({ state: "visible", timeout: 3000 });
  }

  async search(query: string) {
    await this.searchInput.fill(query);
    await this.page.waitForTimeout(500); // debounce
  }
}
