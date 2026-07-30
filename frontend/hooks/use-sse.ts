/**
 * SSE Client Hook with reconnection and notification integration.
 * Task 25.1: Implement SSE client hook with reconnection and notification integration.
 * - Establish SSE connection after auth
 * - Display toast notification on new events
 * - Update document progress indicator on processing events
 * - Reconnect with exponential backoff (1s, 2s, 4s, max 30s) + Last-Event-ID
 * - Close connection on logout
 * Requirements: 25.1, 25.2, 25.3, 25.4, 25.5
 */

"use client";

import { useEffect, useRef, useState, useCallback } from "react";
import { getAccessToken, getTenantId } from "@/lib/api-client";

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080/api/v1";
const INITIAL_RETRY_DELAY = 1000;
const MAX_RETRY_DELAY = 30000;
const BACKOFF_MULTIPLIER = 2;

export type SSEEventType =
  | "notification"
  | "document.processing"
  | "document.analyzed"
  | "document.approved"
  | "document.rejected"
  | "request.updated"
  | "heartbeat";

export interface SSEEvent {
  id: string;
  type: SSEEventType;
  data: Record<string, unknown>;
  timestamp: string;
}

export interface SSECallbacks {
  onNotification?: (event: SSEEvent) => void;
  onDocumentProgress?: (documentId: string, status: string, progress?: number) => void;
  onRequestUpdate?: (requestId: string, status: string) => void;
  onConnected?: () => void;
  onDisconnected?: () => void;
  onError?: (error: Error) => void;
}

interface UseSSEOptions {
  enabled?: boolean;
  callbacks?: SSECallbacks;
}

interface UseSSEReturn {
  isConnected: boolean;
  lastEventId: string | null;
  reconnect: () => void;
  disconnect: () => void;
}

export function useSSE(options: UseSSEOptions = {}): UseSSEReturn {
  const { enabled = true, callbacks = {} } = options;

  const [isConnected, setIsConnected] = useState(false);
  const [lastEventId, setLastEventId] = useState<string | null>(null);

  const eventSourceRef = useRef<EventSource | null>(null);
  const retryDelayRef = useRef(INITIAL_RETRY_DELAY);
  const retryTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const lastEventIdRef = useRef<string | null>(null);

  // Update ref when state changes
  useEffect(() => {
    lastEventIdRef.current = lastEventId;
  }, [lastEventId]);

  const disconnect = useCallback(() => {
    if (retryTimeoutRef.current) {
      clearTimeout(retryTimeoutRef.current);
      retryTimeoutRef.current = null;
    }

    if (eventSourceRef.current) {
      eventSourceRef.current.close();
      eventSourceRef.current = null;
    }

    setIsConnected(false);
    callbacks.onDisconnected?.();
  }, [callbacks]);

  const connect = useCallback(() => {
    const token = getAccessToken();
    const tenantId = getTenantId();

    if (!token || !tenantId) {
      return;
    }

    // Close existing connection
    if (eventSourceRef.current) {
      eventSourceRef.current.close();
    }

    // Build URL with auth params (EventSource doesn't support custom headers)
    const url = new URL(`${API_BASE_URL}/events/stream`);
    url.searchParams.set("token", token);
    url.searchParams.set("tenantId", tenantId);

    // Include Last-Event-ID for reconnection
    if (lastEventIdRef.current) {
      url.searchParams.set("lastEventId", lastEventIdRef.current);
    }

    const eventSource = new EventSource(url.toString());
    eventSourceRef.current = eventSource;

    eventSource.onopen = () => {
      setIsConnected(true);
      retryDelayRef.current = INITIAL_RETRY_DELAY; // Reset backoff on successful connection
      callbacks.onConnected?.();
    };

    eventSource.onerror = () => {
      setIsConnected(false);
      eventSource.close();
      eventSourceRef.current = null;

      // Schedule reconnection with exponential backoff
      const delay = retryDelayRef.current;
      retryDelayRef.current = Math.min(retryDelayRef.current * BACKOFF_MULTIPLIER, MAX_RETRY_DELAY);

      retryTimeoutRef.current = setTimeout(() => {
        connect();
      }, delay);

      callbacks.onError?.(new Error("SSE connection error"));
    };

    // Generic message handler
    eventSource.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data) as SSEEvent;

        // Update last event ID for reconnection
        if (event.lastEventId) {
          setLastEventId(event.lastEventId);
        } else if (data.id) {
          setLastEventId(data.id);
        }

        handleEvent(data);
      } catch {
        // Ignore parse errors
      }
    };

    // Typed event handlers
    eventSource.addEventListener("notification", (event) => {
      try {
        const data = JSON.parse(event.data) as SSEEvent;
        if (event.lastEventId) setLastEventId(event.lastEventId);
        callbacks.onNotification?.(data);
      } catch {
        // Ignore
      }
    });

    eventSource.addEventListener("document.processing", (event) => {
      try {
        const data = JSON.parse(event.data) as {
          documentId: string;
          status: string;
          progress?: number;
        };
        callbacks.onDocumentProgress?.(data.documentId, data.status, data.progress);
      } catch {
        // Ignore
      }
    });

    eventSource.addEventListener("document.analyzed", (event) => {
      try {
        const data = JSON.parse(event.data) as {
          documentId: string;
          status: string;
        };
        callbacks.onDocumentProgress?.(data.documentId, "ANALYZED", 100);
      } catch {
        // Ignore
      }
    });

    eventSource.addEventListener("document.approved", (event) => {
      try {
        const data = JSON.parse(event.data) as { documentId: string };
        callbacks.onDocumentProgress?.(data.documentId, "APPROVED", 100);
      } catch {
        // Ignore
      }
    });

    eventSource.addEventListener("document.rejected", (event) => {
      try {
        const data = JSON.parse(event.data) as { documentId: string };
        callbacks.onDocumentProgress?.(data.documentId, "REJECTED", 100);
      } catch {
        // Ignore
      }
    });

    eventSource.addEventListener("request.updated", (event) => {
      try {
        const data = JSON.parse(event.data) as {
          requestId: string;
          status: string;
        };
        callbacks.onRequestUpdate?.(data.requestId, data.status);
      } catch {
        // Ignore
      }
    });

    // Heartbeat to keep connection alive
    eventSource.addEventListener("heartbeat", () => {
      // Connection is alive
    });
  }, [callbacks]);

  const handleEvent = useCallback(
    (event: SSEEvent) => {
      switch (event.type) {
        case "notification":
          callbacks.onNotification?.(event);
          break;
        case "document.processing":
        case "document.analyzed":
        case "document.approved":
        case "document.rejected":
          callbacks.onDocumentProgress?.(
            event.data.documentId as string,
            event.data.status as string,
            event.data.progress as number | undefined
          );
          break;
        case "request.updated":
          callbacks.onRequestUpdate?.(event.data.requestId as string, event.data.status as string);
          break;
        case "heartbeat":
          // Ignore heartbeat
          break;
      }
    },
    [callbacks]
  );

  const reconnect = useCallback(() => {
    disconnect();
    retryDelayRef.current = INITIAL_RETRY_DELAY;
    connect();
  }, [disconnect, connect]);

  // Connect on mount, disconnect on unmount
  useEffect(() => {
    if (enabled) {
      connect();
    }

    return () => {
      disconnect();
    };
  }, [enabled, connect, disconnect]);

  // Disconnect when auth token changes (logout)
  useEffect(() => {
    const handleStorageChange = (event: StorageEvent) => {
      if (event.key === "atlasops_refresh_token" && !event.newValue) {
        // Logged out
        disconnect();
      }
    };

    window.addEventListener("storage", handleStorageChange);
    return () => window.removeEventListener("storage", handleStorageChange);
  }, [disconnect]);

  return {
    isConnected,
    lastEventId,
    reconnect,
    disconnect,
  };
}

/**
 * Hook for SSE-powered notification badge updates.
 */
export function useSSENotifications() {
  const [unreadCount, setUnreadCount] = useState(0);
  const [latestNotification, setLatestNotification] = useState<SSEEvent | null>(null);

  const { isConnected, reconnect, disconnect } = useSSE({
    callbacks: {
      onNotification: (event) => {
        setLatestNotification(event);
        setUnreadCount((prev) => prev + 1);
      },
    },
  });

  const clearLatest = useCallback(() => {
    setLatestNotification(null);
  }, []);

  const decrementUnread = useCallback(() => {
    setUnreadCount((prev) => Math.max(0, prev - 1));
  }, []);

  const setCount = useCallback((count: number) => {
    setUnreadCount(count);
  }, []);

  return {
    isConnected,
    unreadCount,
    latestNotification,
    clearLatest,
    decrementUnread,
    setCount,
    reconnect,
    disconnect,
  };
}

/**
 * Hook for SSE-powered document progress updates.
 */
export function useSSEDocumentProgress() {
  const [documentStatuses, setDocumentStatuses] = useState<
    Map<string, { status: string; progress?: number }>
  >(new Map());

  const { isConnected } = useSSE({
    callbacks: {
      onDocumentProgress: (documentId, status, progress) => {
        setDocumentStatuses((prev) => {
          const next = new Map(prev);
          next.set(documentId, { status, progress });
          return next;
        });
      },
    },
  });

  const getStatus = useCallback(
    (documentId: string) => {
      return documentStatuses.get(documentId);
    },
    [documentStatuses]
  );

  return {
    isConnected,
    documentStatuses,
    getStatus,
  };
}
