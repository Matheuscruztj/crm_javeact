/**
 * Portal Notifications Page.
 * Task 23.4: Implement portal notifications page.
 * - Read/unread indicators
 * - Mark-as-read functionality (single and bulk)
 * Requirements: 23.7
 */

"use client";

import { useCallback, useEffect, useState } from "react";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
} from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { api, type PageResponse } from "@/lib/api-client";
import { cn } from "@/lib/utils";

interface Notification {
  id: string;
  title: string;
  message: string;
  type: "INFO" | "SUCCESS" | "WARNING" | "ERROR";
  read: boolean;
  createdAt: string;
  entityType?: string;
  entityId?: string;
}

function getNotificationIcon(type: Notification["type"]): React.ReactNode {
  switch (type) {
    case "SUCCESS":
      return (
        <div className="flex h-10 w-10 items-center justify-center rounded-full bg-green-100">
          <svg
            className="h-5 w-5 text-green-600"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
            aria-hidden="true"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M5 13l4 4L19 7"
            />
          </svg>
        </div>
      );
    case "WARNING":
      return (
        <div className="flex h-10 w-10 items-center justify-center rounded-full bg-yellow-100">
          <svg
            className="h-5 w-5 text-yellow-600"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
            aria-hidden="true"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
            />
          </svg>
        </div>
      );
    case "ERROR":
      return (
        <div className="flex h-10 w-10 items-center justify-center rounded-full bg-red-100">
          <svg
            className="h-5 w-5 text-red-600"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
            aria-hidden="true"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
            />
          </svg>
        </div>
      );
    default:
      return (
        <div className="flex h-10 w-10 items-center justify-center rounded-full bg-blue-100">
          <svg
            className="h-5 w-5 text-blue-600"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
            aria-hidden="true"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
            />
          </svg>
        </div>
      );
  }
}

function NotificationItem({
  notification,
  onMarkRead,
  selected,
  onSelect,
}: {
  notification: Notification;
  onMarkRead: (id: string) => void;
  selected: boolean;
  onSelect: (id: string) => void;
}) {
  return (
    <div
      className={cn(
        "flex items-start gap-4 rounded-lg border p-4 transition-colors",
        !notification.read && "bg-primary/5 border-primary/20",
        selected && "ring-2 ring-primary",
      )}
    >
      <input
        type="checkbox"
        checked={selected}
        onChange={() => onSelect(notification.id)}
        className="mt-1 h-4 w-4 rounded border-gray-300"
        aria-label={`Selecionar notificação: ${notification.title}`}
      />

      <div className="flex-shrink-0">
        {getNotificationIcon(notification.type)}
      </div>

      <div className="flex-1 min-w-0">
        <div className="flex items-start justify-between gap-2">
          <div>
            <h3
              className={cn(
                "font-medium",
                !notification.read && "font-semibold",
              )}
            >
              {notification.title}
              {!notification.read && (
                <span className="ml-2 inline-flex h-2 w-2 rounded-full bg-primary" />
              )}
            </h3>
            <p className="text-sm text-muted-foreground mt-1">
              {notification.message}
            </p>
          </div>
          <span className="text-xs text-muted-foreground whitespace-nowrap">
            {new Date(notification.createdAt).toLocaleDateString("pt-BR", {
              day: "2-digit",
              month: "short",
              hour: "2-digit",
              minute: "2-digit",
            })}
          </span>
        </div>
      </div>

      {!notification.read && (
        <Button
          variant="ghost"
          size="sm"
          onClick={() => onMarkRead(notification.id)}
          className="flex-shrink-0"
        >
          Marcar como lida
        </Button>
      )}
    </div>
  );
}

function Pagination({
  page,
  totalPages,
  onPageChange,
}: {
  page: number;
  totalPages: number;
  onPageChange: (page: number) => void;
}) {
  return (
    <div className="flex items-center justify-between">
      <p className="text-sm text-muted-foreground">
        Página {page + 1} de {totalPages || 1}
      </p>
      <div className="flex gap-2">
        <Button
          variant="outline"
          size="sm"
          onClick={() => onPageChange(page - 1)}
          disabled={page === 0}
        >
          Anterior
        </Button>
        <Button
          variant="outline"
          size="sm"
          onClick={() => onPageChange(page + 1)}
          disabled={page >= totalPages - 1}
        >
          Próxima
        </Button>
      </div>
    </div>
  );
}

export default function PortalNotificationsPage() {
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [unreadCount, setUnreadCount] = useState(0);
  const [selectedIds, setSelectedIds] = useState<Set<string>>(new Set());
  const [filter, setFilter] = useState<"ALL" | "UNREAD">("ALL");

  const fetchNotifications = useCallback(async () => {
    setIsLoading(true);
    setError(null);

    try {
      const params = new URLSearchParams({
        page: String(page),
        size: "20",
        sort: "createdAt,desc",
      });

      if (filter === "UNREAD") {
        params.append("unreadOnly", "true");
      }

      const [notifsRes, countRes] = await Promise.all([
        api.get<PageResponse<Notification>>(
          `/notifications?${params.toString()}`,
        ),
        api.get<{ unreadCount: number }>("/notifications/unread-count"),
      ]);

      setNotifications(notifsRes.content);
      setTotalPages(notifsRes.page.totalPages);
      setUnreadCount(countRes.unreadCount);
      setSelectedIds(new Set());
    } catch {
      setError("Falha ao carregar notificações");
    } finally {
      setIsLoading(false);
    }
  }, [page, filter]);

  useEffect(() => {
    fetchNotifications();
  }, [fetchNotifications]);

  const markAsRead = useCallback(async (id: string) => {
    try {
      await api.patch("/notifications/mark-read", { ids: [id] });
      setNotifications((prev) =>
        prev.map((n) => (n.id === id ? { ...n, read: true } : n)),
      );
      setUnreadCount((prev) => Math.max(0, prev - 1));
    } catch {
      // Silently fail
    }
  }, []);

  const markSelectedAsRead = useCallback(async () => {
    if (selectedIds.size === 0) return;

    try {
      const ids = Array.from(selectedIds);
      await api.patch("/notifications/mark-read", { ids });
      setNotifications((prev) =>
        prev.map((n) => (selectedIds.has(n.id) ? { ...n, read: true } : n)),
      );
      const unreadSelected = notifications.filter(
        (n) => selectedIds.has(n.id) && !n.read,
      ).length;
      setUnreadCount((prev) => Math.max(0, prev - unreadSelected));
      setSelectedIds(new Set());
    } catch {
      // Silently fail
    }
  }, [selectedIds, notifications]);

  const markAllAsRead = useCallback(async () => {
    try {
      await api.patch("/notifications/mark-all-read", {});
      setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
      setUnreadCount(0);
    } catch {
      // Silently fail
    }
  }, []);

  const toggleSelect = useCallback((id: string) => {
    setSelectedIds((prev) => {
      const newSet = new Set(prev);
      if (newSet.has(id)) {
        newSet.delete(id);
      } else {
        newSet.add(id);
      }
      return newSet;
    });
  }, []);

  const selectAll = useCallback(() => {
    if (selectedIds.size === notifications.length) {
      setSelectedIds(new Set());
    } else {
      setSelectedIds(new Set(notifications.map((n) => n.id)));
    }
  }, [notifications, selectedIds]);

  return (
    <div className="p-6">
      <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-bold">Notificações</h1>
          <p className="text-muted-foreground">
            Acompanhe atualizações e alertas sobre suas solicitações.
            {unreadCount > 0 && (
              <span className="ml-2 inline-flex items-center rounded-full bg-primary px-2 py-0.5 text-xs text-primary-foreground">
                {unreadCount} não lida{unreadCount > 1 ? "s" : ""}
              </span>
            )}
          </p>
        </div>

        {unreadCount > 0 && (
          <Button variant="outline" onClick={markAllAsRead}>
            Marcar todas como lidas
          </Button>
        )}
      </div>

      {/* Filters and Bulk Actions */}
      <div className="mb-4 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div className="flex items-center gap-4">
          <select
            value={filter}
            onChange={(e) => {
              setFilter(e.target.value as "ALL" | "UNREAD");
              setPage(0);
            }}
            className="rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background focus:outline-none focus:ring-2 focus:ring-ring"
            aria-label="Filtrar notificações"
          >
            <option value="ALL">Todas</option>
            <option value="UNREAD">Não lidas</option>
          </select>

          {notifications.length > 0 && (
            <label className="flex items-center gap-2 text-sm">
              <input
                type="checkbox"
                checked={selectedIds.size === notifications.length}
                onChange={selectAll}
                className="h-4 w-4 rounded border-gray-300"
              />
              Selecionar todas
            </label>
          )}
        </div>

        {selectedIds.size > 0 && (
          <Button variant="outline" size="sm" onClick={markSelectedAsRead}>
            Marcar {selectedIds.size} como lida{selectedIds.size > 1 ? "s" : ""}
          </Button>
        )}
      </div>

      {error && (
        <div
          className="mb-4 rounded-md bg-destructive/10 p-4 text-sm text-destructive"
          role="alert"
        >
          {error}
        </div>
      )}

      {/* Notifications List */}
      <div className="space-y-3">
        {isLoading ? (
          Array.from({ length: 5 }).map((_, i) => (
            <Card key={i}>
              <CardContent className="p-4">
                <div className="flex items-start gap-4">
                  <Skeleton className="h-10 w-10 rounded-full" />
                  <div className="flex-1">
                    <Skeleton className="h-5 w-48 mb-2" />
                    <Skeleton className="h-4 w-full" />
                  </div>
                </div>
              </CardContent>
            </Card>
          ))
        ) : notifications.length === 0 ? (
          <Card>
            <CardContent className="py-8 text-center">
              <svg
                className="mx-auto h-12 w-12 text-muted-foreground mb-4"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
                aria-hidden="true"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"
                />
              </svg>
              <p className="text-muted-foreground">
                {filter === "UNREAD"
                  ? "Nenhuma notificação não lida."
                  : "Nenhuma notificação encontrada."}
              </p>
            </CardContent>
          </Card>
        ) : (
          notifications.map((notification) => (
            <NotificationItem
              key={notification.id}
              notification={notification}
              onMarkRead={markAsRead}
              selected={selectedIds.has(notification.id)}
              onSelect={toggleSelect}
            />
          ))
        )}
      </div>

      {/* Pagination */}
      {!isLoading && notifications.length > 0 && (
        <div className="mt-4">
          <Pagination
            page={page}
            totalPages={totalPages}
            onPageChange={setPage}
          />
        </div>
      )}
    </div>
  );
}
