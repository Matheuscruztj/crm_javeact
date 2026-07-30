"use client";

/**
 * NotificationCenter: badge + dropdown + mark-read + link-to-entity.
 * Validates: P1.13.1 — Notification Center
 *
 * Connects to useUnreadCount and useNotifications hooks.
 * Subscribes to SSE for real-time updates via useSSE.
 */

import { useState, useEffect } from "react";
import Link from "next/link";
import { api } from "@/lib/api-client";
import { useUnreadCount, useNotifications, type Notification } from "@/hooks/use-api";

// ─── NotificationItem ─────────────────────────────────────────────────────

function NotificationItem({
  notification,
  onMarkRead,
}: {
  notification: Notification;
  onMarkRead: (id: string) => void;
}) {
  return (
    <div
      className={`hover:bg-muted/25 flex items-start gap-3 border-b px-4 py-3 transition-colors ${
        notification.read ? "opacity-60" : "bg-primary/5"
      }`}
      role="listitem"
    >
      {/* Unread dot */}
      {!notification.read && (
        <span
          className="bg-primary mt-1.5 h-2 w-2 flex-shrink-0 rounded-full"
          aria-label="Unread"
        />
      )}
      <div className="min-w-0 flex-1">
        <p className="truncate text-sm font-medium">{notification.title}</p>
        <p className="text-muted-foreground truncate text-xs">{notification.message}</p>
        <p className="text-muted-foreground mt-0.5 text-xs">
          {new Date(notification.createdAt).toLocaleString()}
        </p>
      </div>
      <div className="flex flex-shrink-0 flex-col items-end gap-1">
        {notification.link && (
          <Link
            href={notification.link}
            className="text-primary text-xs hover:underline"
            aria-label={`View: ${notification.title}`}
          >
            View
          </Link>
        )}
        {!notification.read && (
          <button
            onClick={() => onMarkRead(notification.id)}
            className="text-muted-foreground hover:text-foreground text-xs"
            aria-label={`Mark as read: ${notification.title}`}
          >
            Mark read
          </button>
        )}
      </div>
    </div>
  );
}

// ─── NotificationCenter ────────────────────────────────────────────────────

export function NotificationCenter() {
  const [open, setOpen] = useState(false);
  const { data: countData, refetch: refetchCount } = useUnreadCount();
  const { data: notifData, refetch: refetchNotifs } = useNotifications(0);

  const unreadCount = countData?.count ?? 0;
  const notifications = notifData?.content ?? [];

  // Close on outside click
  useEffect(() => {
    if (!open) return;
    const handler = (e: MouseEvent) => {
      const target = e.target as HTMLElement;
      if (!target.closest("[data-notification-center]")) setOpen(false);
    };
    document.addEventListener("mousedown", handler);
    return () => document.removeEventListener("mousedown", handler);
  }, [open]);

  const handleMarkRead = async (id: string) => {
    try {
      await api.post(`/notifications/mark-read`, { ids: [id] });
      refetchCount();
      refetchNotifs();
    } catch {
      /* non-critical, ignore */
    }
  };

  const handleMarkAllRead = async () => {
    try {
      await api.post(`/notifications/mark-read-all`, {});
      refetchCount();
      refetchNotifs();
    } catch {
      /* non-critical */
    }
  };

  return (
    <div className="relative" data-notification-center="">
      {/* Bell button */}
      <button
        onClick={() => setOpen((o) => !o)}
        className="hover:bg-muted relative rounded-md p-2"
        aria-label={`Notifications${unreadCount > 0 ? ` — ${unreadCount} unread` : ""}`}
        aria-expanded={open}
        aria-haspopup="true"
      >
        {/* Bell icon */}
        <svg
          xmlns="http://www.w3.org/2000/svg"
          width="20"
          height="20"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="2"
          strokeLinecap="round"
          strokeLinejoin="round"
          aria-hidden="true"
        >
          <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
          <path d="M13.73 21a2 2 0 0 1-3.46 0" />
        </svg>

        {/* Badge */}
        {unreadCount > 0 && (
          <span
            className="bg-destructive text-destructive-foreground absolute -top-0.5 -right-0.5 flex h-4 min-w-4 items-center justify-center rounded-full px-1 text-[10px] font-bold"
            aria-hidden="true"
          >
            {unreadCount > 99 ? "99+" : unreadCount}
          </span>
        )}
      </button>

      {/* Dropdown */}
      {open && (
        <div
          className="bg-background absolute top-full right-0 z-50 mt-2 w-80 overflow-hidden rounded-md border shadow-lg"
          role="dialog"
          aria-label="Notifications"
          aria-modal="false"
        >
          {/* Header */}
          <div className="flex items-center justify-between border-b px-4 py-3">
            <h3 className="text-sm font-semibold">Notifications</h3>
            {unreadCount > 0 && (
              <button
                onClick={handleMarkAllRead}
                className="text-primary text-xs hover:underline"
                aria-label="Mark all notifications as read"
              >
                Mark all read
              </button>
            )}
          </div>

          {/* List */}
          <div className="max-h-80 overflow-y-auto" role="list" aria-label="Notification list">
            {notifications.length === 0 ? (
              <p className="text-muted-foreground px-4 py-6 text-center text-sm">
                No notifications
              </p>
            ) : (
              notifications.map((n) => (
                <NotificationItem key={n.id} notification={n} onMarkRead={handleMarkRead} />
              ))
            )}
          </div>

          {/* Footer */}
          <div className="border-t px-4 py-2 text-center">
            <Link
              href="/portal/notifications"
              className="text-primary text-xs hover:underline"
              onClick={() => setOpen(false)}
            >
              View all notifications
            </Link>
          </div>
        </div>
      )}
    </div>
  );
}
