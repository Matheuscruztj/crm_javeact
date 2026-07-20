import { Page, Locator } from "@playwright/test";

/**
 * Page Object for notification and SSE stream pages.
 * Validates: P0.B.1 — Setup Playwright with page objects
 */
export class NotificationPage {
  readonly page: Page;
  readonly notificationBell: Locator;
  readonly notificationList: Locator;
  readonly unreadCount: Locator;
  readonly markAllReadButton: Locator;

  constructor(page: Page) {
    this.page = page;
    this.notificationBell = page.getByTestId("notification-bell");
    this.notificationList = page.getByTestId("notification-list");
    this.unreadCount = page.getByTestId("unread-count");
    this.markAllReadButton = page.getByRole("button", {
      name: /mark all read|marcar tudo como lido/i,
    });
  }

  async openNotifications() {
    await this.notificationBell.click();
    await this.notificationList.waitFor({ state: "visible" });
  }

  async getUnreadCount(): Promise<number> {
    const text = await this.unreadCount.textContent();
    return parseInt(text ?? "0", 10);
  }
}
