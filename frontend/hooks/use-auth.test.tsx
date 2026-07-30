import { renderHook, waitFor, act } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vitest";

const apiLogin = vi.fn();
const apiLogout = vi.fn();
const getAccessToken = vi.fn();
const clearTokens = vi.fn();

vi.mock("@/lib/api-client", () => ({
  login: (...args: unknown[]) => apiLogin(...args),
  logout: (...args: unknown[]) => apiLogout(...args),
  getAccessToken: (...args: unknown[]) => getAccessToken(...args),
  clearTokens: (...args: unknown[]) => clearTokens(...args),
}));

import { getRedirectPath, useAuth } from "./use-auth";

describe("useAuth", () => {
  beforeEach(() => {
    apiLogin.mockReset();
    apiLogout.mockReset();
    getAccessToken.mockReset();
    clearTokens.mockReset();
    localStorage.clear();
    document.cookie = "";
  });

  it("returns unauthenticated state when there is no session", async () => {
    getAccessToken.mockReturnValue(null);

    const { result } = renderHook(() => useAuth());

    await waitFor(() => expect(result.current.isLoading).toBe(false));
    expect(result.current.isAuthenticated).toBe(false);
    expect(result.current.user).toBeNull();
    expect(clearTokens).toHaveBeenCalled();
  });

  it("hydrates existing session from storage and token", async () => {
    const user = {
      id: "user-1",
      email: "admin@example.com",
      name: "Admin",
      role: "ADMIN" as const,
      tenantId: "tenant-1",
    };
    localStorage.setItem("atlasops_user", JSON.stringify(user));
    getAccessToken.mockReturnValue("access");

    const { result } = renderHook(() => useAuth());

    await waitFor(() => expect(result.current.isLoading).toBe(false));
    expect(result.current.isAuthenticated).toBe(true);
    expect(result.current.user).toEqual(user);
  });

  it("logs in and stores user state", async () => {
    apiLogin.mockResolvedValueOnce({
      user: {
        id: "user-2",
        email: "analyst@example.com",
        name: "Analyst",
        role: "ANALYST",
        tenantId: "tenant-2",
      },
    });
    getAccessToken.mockReturnValue(null);

    const { result } = renderHook(() => useAuth());
    await waitFor(() => expect(result.current.isLoading).toBe(false));

    await act(async () => {
      await result.current.login({ email: "analyst@example.com", password: "secret123" });
    });

    expect(result.current.isAuthenticated).toBe(true);
    expect(result.current.user?.tenantId).toBe("tenant-2");
  });

  it("logs out and clears stored session", async () => {
    apiLogout.mockResolvedValueOnce(undefined);
    getAccessToken.mockReturnValue("access");

    const { result } = renderHook(() => useAuth());
    await waitFor(() => expect(result.current.isLoading).toBe(false));

    await act(async () => {
      await result.current.logout();
    });

    expect(result.current.isAuthenticated).toBe(false);
    expect(apiLogout).toHaveBeenCalled();
  });

  it("maps redirect paths by role", () => {
    expect(getRedirectPath("ADMIN")).toBe("/admin/dashboard");
    expect(getRedirectPath("ANALYST")).toBe("/admin/dashboard");
    expect(getRedirectPath("CLIENT")).toBe("/portal/home");
  });
});
