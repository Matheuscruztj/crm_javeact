/**
 * Authentication hook for managing user session state.
 * Task 21.1: Login page with credential form and token management.
 */

"use client";

import { useCallback, useEffect, useState } from "react";
import {
  login as apiLogin,
  logout as apiLogout,
  getAccessToken,
  clearTokens,
  type LoginRequest,
  type LoginResponse,
} from "@/lib/api-client";

interface User {
  id: string;
  email: string;
  name: string;
  role: "ADMIN" | "ANALYST" | "CLIENT";
  tenantId: string;
}

interface AuthState {
  user: User | null;
  isLoading: boolean;
  isAuthenticated: boolean;
}

interface UseAuthReturn extends AuthState {
  login: (credentials: LoginRequest) => Promise<void>;
  logout: () => Promise<void>;
}

const USER_STORAGE_KEY = "atlasops_user";

function getStoredUser(): User | null {
  if (typeof window === "undefined") return null;
  const stored = localStorage.getItem(USER_STORAGE_KEY);
  if (!stored) return null;
  try {
    return JSON.parse(stored) as User;
  } catch {
    return null;
  }
}

function setStoredUser(user: User | null): void {
  if (typeof window === "undefined") return;
  if (user) {
    localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(user));
    // Set cookies for middleware to read (role-based route protection)
    document.cookie = `atlasops_user_role=${user.role}; path=/; SameSite=Lax`;
  } else {
    localStorage.removeItem(USER_STORAGE_KEY);
    // Clear cookies
    document.cookie =
      "atlasops_user_role=; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT";
    document.cookie =
      "atlasops_refresh_token=; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT";
  }
}

export function useAuth(): UseAuthReturn {
  const [state, setState] = useState<AuthState>({
    user: null,
    isLoading: true,
    isAuthenticated: false,
  });

  // Check for existing session on mount
  useEffect(() => {
    const storedUser = getStoredUser();
    const hasToken = !!getAccessToken();

    if (storedUser && hasToken) {
      setState({
        user: storedUser,
        isLoading: false,
        isAuthenticated: true,
      });
    } else {
      // Clear any stale data
      if (!hasToken) {
        setStoredUser(null);
        clearTokens();
      }
      setState({
        user: null,
        isLoading: false,
        isAuthenticated: false,
      });
    }
  }, []);

  const login = useCallback(
    async (credentials: LoginRequest): Promise<void> => {
      setState((prev) => ({ ...prev, isLoading: true }));

      try {
        const response: LoginResponse = await apiLogin(credentials);
        const user: User = response.user;

        setStoredUser(user);
        setState({
          user,
          isLoading: false,
          isAuthenticated: true,
        });
      } catch (error) {
        setState({
          user: null,
          isLoading: false,
          isAuthenticated: false,
        });
        throw error;
      }
    },
    [],
  );

  const logout = useCallback(async (): Promise<void> => {
    setState((prev) => ({ ...prev, isLoading: true }));

    try {
      await apiLogout();
    } finally {
      setStoredUser(null);
      setState({
        user: null,
        isLoading: false,
        isAuthenticated: false,
      });
    }
  }, []);

  return {
    ...state,
    login,
    logout,
  };
}

/**
 * Get redirect path based on user role.
 */
export function getRedirectPath(role: User["role"]): string {
  switch (role) {
    case "ADMIN":
    case "ANALYST":
      return "/admin/dashboard";
    case "CLIENT":
      return "/portal/home";
    default:
      return "/";
  }
}
