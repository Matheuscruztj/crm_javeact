import { renderHook, act } from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

const mockApiClient = vi.hoisted(() => ({
  getAccessToken: vi.fn(),
  getTenantId: vi.fn(),
}));

vi.mock("@/lib/api-client", () => mockApiClient);

import { useSSE, useSSENotifications, useSSEDocumentProgress } from "./use-sse";

type Listener = (event: { data: string; lastEventId?: string }) => void;

class FakeEventSource {
  static instances: FakeEventSource[] = [];

  url: string;
  closed = false;
  onopen: null | (() => void) = null;
  onerror: null | (() => void) = null;
  onmessage: null | ((event: { data: string; lastEventId?: string }) => void) = null;
  listeners = new Map<string, Listener[]>();

  constructor(url: string) {
    this.url = url;
    FakeEventSource.instances.push(this);
  }

  addEventListener(type: string, handler: Listener) {
    const handlers = this.listeners.get(type) ?? [];
    handlers.push(handler);
    this.listeners.set(type, handlers);
  }

  close() {
    this.closed = true;
  }

  emitOpen() {
    this.onopen?.();
  }

  emitError() {
    this.onerror?.();
  }

  emitMessage(payload: unknown, lastEventId?: string) {
    this.onmessage?.({ data: JSON.stringify(payload), lastEventId });
  }

  emit(type: string, payload: unknown, lastEventId?: string) {
    for (const handler of this.listeners.get(type) ?? []) {
      handler({ data: JSON.stringify(payload), lastEventId });
    }
  }
}

describe("useSSE", () => {
  beforeEach(() => {
    FakeEventSource.instances = [];
    mockApiClient.getAccessToken.mockReturnValue("token-1");
    mockApiClient.getTenantId.mockReturnValue("tenant-1");
    vi.stubGlobal("EventSource", FakeEventSource);
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("connects, handles events and reconnects with last event id", async () => {
    const onNotification = vi.fn();
    const onDocumentProgress = vi.fn();
    const onRequestUpdate = vi.fn();
    const onConnected = vi.fn();
    const onDisconnected = vi.fn();
    const onError = vi.fn();
    const callbacks = {
      onNotification,
      onDocumentProgress,
      onRequestUpdate,
      onConnected,
      onDisconnected,
      onError,
    };

    const { result } = renderHook(() => useSSE({ callbacks }));

    expect(FakeEventSource.instances).toHaveLength(1);
    const first = FakeEventSource.instances[0];
    expect(new URL(first.url).searchParams.get("token")).toBe("token-1");
    expect(new URL(first.url).searchParams.get("tenantId")).toBe("tenant-1");

    act(() => {
      first.emitOpen();
    });
    expect(onConnected).toHaveBeenCalledTimes(1);

    act(() => {
      first.emitMessage(
        {
          id: "event-1",
          type: "notification",
          data: { message: "Ping" },
          timestamp: "2026-07-27T00:00:00Z",
        },
        "event-1"
      );
    });

    expect(onNotification).toHaveBeenCalledWith(
      expect.objectContaining({ id: "event-1", type: "notification" })
    );

    act(() => {
      first.emit("document.processing", {
        documentId: "doc-1",
        status: "PROCESSING",
        progress: 25,
      });
      first.emit("document.analyzed", { documentId: "doc-1", status: "ANALYZED" });
      first.emit("document.approved", { documentId: "doc-1" });
      first.emit("document.rejected", { documentId: "doc-2" });
      first.emit("request.updated", { requestId: "req-1", status: "DONE" });
      first.emit("heartbeat", {});
    });

    expect(onDocumentProgress).toHaveBeenCalledWith("doc-1", "PROCESSING", 25);
    expect(onDocumentProgress).toHaveBeenCalledWith("doc-1", "ANALYZED", 100);
    expect(onDocumentProgress).toHaveBeenCalledWith("doc-1", "APPROVED", 100);
    expect(onDocumentProgress).toHaveBeenCalledWith("doc-2", "REJECTED", 100);
    expect(onRequestUpdate).toHaveBeenCalledWith("req-1", "DONE");

    act(() => {
      result.current.reconnect();
    });

    expect(first.closed).toBe(true);
    expect(onError).not.toHaveBeenCalled();
    expect(FakeEventSource.instances).toHaveLength(2);
    const second = FakeEventSource.instances[1];
    expect(new URL(second.url).searchParams.get("lastEventId")).toBe("event-1");

    act(() => {
      result.current.disconnect();
    });
    expect(onDisconnected).toHaveBeenCalled();
    expect(second.closed).toBe(true);
  });

  it("does not connect when disabled and disconnects on logout", async () => {
    const onDisconnected = vi.fn();
    const callbacks = { onDisconnected };
    const { rerender, unmount } = renderHook(
      ({ enabled }) =>
        useSSE({
          enabled,
          callbacks,
        }),
      { initialProps: { enabled: false } }
    );

    expect(FakeEventSource.instances).toHaveLength(0);

    rerender({ enabled: true });
    expect(FakeEventSource.instances).toHaveLength(1);

    act(() => {
      window.dispatchEvent(
        new StorageEvent("storage", {
          key: "atlasops_refresh_token",
          newValue: null,
        })
      );
    });

    expect(onDisconnected).toHaveBeenCalled();
    expect(FakeEventSource.instances[0].closed).toBe(true);

    unmount();
  });
});

describe("useSSE wrappers", () => {
  beforeEach(() => {
    FakeEventSource.instances = [];
    mockApiClient.getAccessToken.mockReturnValue("token-2");
    mockApiClient.getTenantId.mockReturnValue("tenant-2");
    vi.stubGlobal("EventSource", FakeEventSource);
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("tracks unread notifications and document status snapshots", async () => {
    const notifications = renderHook(() => useSSENotifications());
    const documents = renderHook(() => useSSEDocumentProgress());

    expect(FakeEventSource.instances.length).toBeGreaterThanOrEqual(2);

    const notificationSource = FakeEventSource.instances[0];
    const documentSource = FakeEventSource.instances[1];

    act(() => {
      notificationSource.emit("notification", {
        id: "n-1",
        type: "notification",
        data: { message: "New" },
        timestamp: "2026-07-27T00:00:00Z",
      });
      documentSource.emit("document.processing", {
        documentId: "doc-99",
        status: "PROCESSING",
        progress: 50,
      });
    });

    expect(notifications.result.current.unreadCount).toBe(1);
    expect(notifications.result.current.latestNotification).toMatchObject({ id: "n-1" });
    expect(documents.result.current.getStatus("doc-99")).toEqual({
      status: "PROCESSING",
      progress: 50,
    });

    act(() => {
      notifications.result.current.setCount(3);
      notifications.result.current.decrementUnread();
      notifications.result.current.clearLatest();
    });

    expect(notifications.result.current.unreadCount).toBe(2);
    expect(notifications.result.current.latestNotification).toBeNull();
  });
});
