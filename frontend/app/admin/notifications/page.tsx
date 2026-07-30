"use client";

import { useEffect, useState } from "react";
import { api } from "@/lib/api-client";
import { getApiErrorMessage } from "@/lib/form-utils";

interface Notification {
  id: string;
  title: string;
  message: string;
  read: boolean;
  link?: string;
  createdAt: string;
}

/**
 * Admin notifications management page.
 * Lists all notifications with mark-read and unread count.
 * Validates: Requirements 15.4, 15.5, 15.7, 15.8 (task 15)
 */
export default function AdminNotificationsPage() {
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = async () => {
    try {
      setLoading(true);
      const [list, count] = await Promise.all([
        api.get<{ content: Notification[] }>("/notifications?size=50"),
        api.get<{ count: number }>("/notifications/unread-count"),
      ]);
      setNotifications(list.content ?? []);
      setUnreadCount(count.count ?? 0);
      setError(null);
    } catch (err) {
      setError(getApiErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void load();
  }, []);

  const markAllRead = async () => {
    const unreadIds = notifications.filter((n) => !n.read).map((n) => n.id);
    if (unreadIds.length === 0) return;
    try {
      await api.patch("/notifications/mark-read", { ids: unreadIds });
      setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
      setUnreadCount(0);
    } catch (err) {
      setError(getApiErrorMessage(err));
    }
  };

  if (loading)
    return (
      <div className="text-muted-foreground p-6" aria-busy="true">
        Loading…
      </div>
    );
  if (error)
    return (
      <div className="text-destructive p-6" role="alert">
        {error}
      </div>
    );

  return (
    <div className="p-6">
      <div className="mb-6 flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">Notifications</h1>
          {unreadCount > 0 && <p className="text-muted-foreground text-sm">{unreadCount} unread</p>}
        </div>
        {unreadCount > 0 && (
          <button onClick={markAllRead} className="hover:bg-muted rounded border px-4 py-2 text-sm">
            Mark all read
          </button>
        )}
      </div>

      {notifications.length === 0 ? (
        <div className="text-muted-foreground rounded-lg border p-8 text-center">
          No notifications.
        </div>
      ) : (
        <ul className="divide-y rounded-lg border">
          {notifications.map((n) => (
            <li key={n.id} className={`flex gap-3 px-4 py-3 ${n.read ? "" : "bg-blue-50/50"}`}>
              {!n.read && (
                <span
                  className="mt-1.5 h-2 w-2 shrink-0 rounded-full bg-blue-500"
                  aria-label="Unread"
                />
              )}
              {n.read && <span className="mt-1.5 h-2 w-2 shrink-0" />}
              <div className="min-w-0 flex-1">
                <p className="text-sm font-medium">{n.title}</p>
                <p className="text-muted-foreground text-xs">{n.message}</p>
                <p className="text-muted-foreground mt-1 text-xs">
                  {new Date(n.createdAt).toLocaleString()}
                </p>
              </div>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
