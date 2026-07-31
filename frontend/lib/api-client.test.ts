import { beforeEach, describe, expect, it, vi } from "vitest";

const fetchMock = vi.fn();

vi.stubGlobal("fetch", fetchMock);
vi.stubGlobal("crypto", {
  randomUUID: vi.fn(() => "trace-123"),
});

async function loadModule() {
  vi.resetModules();
  return await import("./api-client");
}

describe("api-client", () => {
  beforeEach(() => {
    fetchMock.mockReset();
    localStorage.clear();
    document.cookie = "";
  });

  it("stores and clears tokens and tenant context", async () => {
    const { setTokens, clearTokens, getAccessToken, getRefreshToken, setTenantId, getTenantId } =
      await loadModule();

    setTokens({ accessToken: "access-1", refreshToken: "refresh-1", expiresIn: 120 });
    setTenantId("tenant-1");

    expect(getAccessToken()).toBe("access-1");
    expect(getRefreshToken()).toBe("refresh-1");
    expect(getTenantId()).toBe("tenant-1");
    expect(localStorage.getItem("atlasops_refresh_token")).toBe("refresh-1");

    clearTokens();

    expect(getAccessToken()).toBeNull();
    expect(getRefreshToken()).toBeNull();
    expect(getTenantId()).toBeNull();
  });

  it("logs in and persists tokens and tenant", async () => {
    const { login, getAccessToken, getTenantId } = await loadModule();

    fetchMock.mockResolvedValueOnce({
      ok: true,
      json: async () => ({
        accessToken: "access-2",
        refreshToken: "refresh-2",
        expiresIn: 60,
        user: {
          id: "user-1",
          email: "admin@example.com",
          name: "Admin",
          role: "ADMIN",
          tenantId: "tenant-2",
        },
      }),
    });

    const response = await login({ email: "admin@example.com", password: "secret" });

    expect(response.user.tenantId).toBe("tenant-2");
    expect(getAccessToken()).toBe("access-2");
    expect(getTenantId()).toBe("tenant-2");
    expect(fetchMock).toHaveBeenCalledWith(
      "http://localhost:8080/api/v1/auth/login",
      expect.objectContaining({
        method: "POST",
      })
    );
  });

  it("logout clears tokens even when api call fails", async () => {
    const { logout, setTokens, setTenantId, getAccessToken, getTenantId } = await loadModule();

    setTokens({ accessToken: "access-3", refreshToken: "refresh-3", expiresIn: 60 });
    setTenantId("tenant-3");
    fetchMock.mockResolvedValueOnce({
      ok: true,
      status: 204,
      json: async () => ({}),
    });

    await logout();

    expect(getAccessToken()).toBeNull();
    expect(getTenantId()).toBeNull();
  });

  it("sends auth, tenant and correlation headers on request", async () => {
    const { api, setTokens, setTenantId } = await loadModule();

    setTokens({ accessToken: "old-access", refreshToken: "refresh-4", expiresIn: 120 });
    setTenantId("tenant-4");

    fetchMock.mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: async () => ({ ok: true }),
    });

    await api.get("/customers");

    expect(fetchMock).toHaveBeenCalledWith(
      "http://localhost:8080/api/v1/customers",
      expect.objectContaining({
        headers: expect.objectContaining({
          Authorization: "Bearer old-access",
          "X-Tenant-ID": "tenant-4",
          "X-Correlation-ID": "trace-123",
        }),
      })
    );
  });

  it("refreshes expired tokens before a request and uses the refreshed access token", async () => {
    const { api, setTokens, setTenantId } = await loadModule();

    setTokens({
      accessToken: "expired-access",
      refreshToken: "refresh-expired",
      expiresIn: 1,
    });
    setTenantId("tenant-expired");
    localStorage.setItem("atlasops_token_expires", String(Date.now() - 1000));

    fetchMock
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({
          accessToken: "fresh-access",
          refreshToken: "fresh-refresh",
          expiresIn: 120,
        }),
      })
      .mockResolvedValueOnce({
        ok: true,
        status: 200,
        json: async () => ({ id: "customer-1" }),
      });

    await expect(api.get<{ id: string }>("/customers/1")).resolves.toEqual({ id: "customer-1" });

    expect(fetchMock).toHaveBeenNthCalledWith(
      1,
      "http://localhost:8080/api/v1/auth/refresh",
      expect.objectContaining({
        method: "POST",
      })
    );
    expect(fetchMock).toHaveBeenNthCalledWith(
      2,
      "http://localhost:8080/api/v1/customers/1",
      expect.objectContaining({
        headers: expect.objectContaining({
          Authorization: "Bearer fresh-access",
          "X-Tenant-ID": "tenant-expired",
        }),
      })
    );
  });

  it("retries once after a 401 response when refresh succeeds", async () => {
    const { api, setTokens, setTenantId, getAccessToken } = await loadModule();

    setTokens({ accessToken: "stale-access", refreshToken: "refresh-401", expiresIn: 120 });
    setTenantId("tenant-401");

    fetchMock
      .mockResolvedValueOnce({
        ok: false,
        status: 401,
        json: async () => ({
          status: 401,
          title: "Unauthorized",
          code: "UNAUTHORIZED",
          detail: "",
          traceId: "",
          type: "",
        }),
      })
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({
          accessToken: "rotated-access",
          refreshToken: "rotated-refresh",
          expiresIn: 120,
        }),
      })
      .mockResolvedValueOnce({
        ok: true,
        status: 200,
        json: async () => ({ id: "customer-2" }),
      });

    await expect(api.get<{ id: string }>("/customers/2")).resolves.toEqual({ id: "customer-2" });

    expect(fetchMock.mock.calls[0]?.[0]).toBe("http://localhost:8080/api/v1/customers/2");
    expect(fetchMock.mock.calls[0]?.[1]).toEqual(
      expect.objectContaining({
        method: "GET",
        headers: expect.objectContaining({
          "Content-Type": "application/json",
          "X-Tenant-ID": "tenant-401",
        }),
      })
    );
    expect(fetchMock).toHaveBeenNthCalledWith(
      2,
      "http://localhost:8080/api/v1/auth/refresh",
      expect.objectContaining({
        method: "POST",
      })
    );
    expect(fetchMock).toHaveBeenNthCalledWith(
      3,
      "http://localhost:8080/api/v1/customers/2",
      expect.objectContaining({
        headers: expect.objectContaining({
          Authorization: "Bearer rotated-access",
          "X-Tenant-ID": "tenant-401",
        }),
      })
    );
    expect(getAccessToken()).toBe("rotated-access");
  });

  it("returns undefined for no-content responses and clears tokens when refresh fails", async () => {
    const { api, setTokens, getAccessToken, getTenantId } = await loadModule();

    setTokens({ accessToken: "soon-expired", refreshToken: "refresh-bad", expiresIn: 1 });
    localStorage.setItem("atlasops_token_expires", String(Date.now() - 1000));

    fetchMock.mockResolvedValueOnce({
      ok: false,
      status: 500,
      json: async () => ({}),
    });

    await expect(api.get("/needs-refresh")).rejects.toThrow(
      "Session expired. Please log in again."
    );
    expect(getAccessToken()).toBeNull();
    expect(getTenantId()).toBeNull();

    fetchMock.mockResolvedValueOnce({
      ok: true,
      status: 204,
      json: async () => ({}),
    });

    await expect(api.delete("/customers/3")).resolves.toBeUndefined();
  });
});
