/**
 * API Client with authentication and tenant context.
 * Provides a typed HTTP client for communicating with the backend API.
 * Task 21.3: API client hook with auth headers and tenant context.
 */

const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080/api/v1";

// Token storage (in-memory for access token, localStorage for refresh token)
let accessToken: string | null = null;

interface TokenPair {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
}

interface ApiError {
  type: string;
  title: string;
  status: number;
  code: string;
  detail: string;
  traceId: string;
  violations?: Array<{ field: string; message: string }>;
}

interface PageResponse<T> {
  content: T[];
  page: {
    number: number;
    size: number;
    totalElements: number;
    totalPages: number;
  };
}

// Auth token management
export function setTokens(tokens: TokenPair): void {
  accessToken = tokens.accessToken;
  if (typeof window !== "undefined") {
    localStorage.setItem("atlasops_refresh_token", tokens.refreshToken);
    localStorage.setItem(
      "atlasops_token_expires",
      String(Date.now() + tokens.expiresIn * 1000),
    );
    // Set cookie for middleware route protection (P0.L.1)
    document.cookie = `atlasops_refresh_token=${tokens.refreshToken}; path=/; SameSite=Lax`;
  }
}

export function clearTokens(): void {
  accessToken = null;
  if (typeof window !== "undefined") {
    localStorage.removeItem("atlasops_refresh_token");
    localStorage.removeItem("atlasops_token_expires");
    localStorage.removeItem("atlasops_tenant_id");
    // Clear cookies used by middleware (P0.L.1)
    document.cookie =
      "atlasops_refresh_token=; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT";
  }
}

export function getAccessToken(): string | null {
  return accessToken;
}

export function getRefreshToken(): string | null {
  if (typeof window === "undefined") return null;
  return localStorage.getItem("atlasops_refresh_token");
}

export function isTokenExpired(): boolean {
  if (typeof window === "undefined") return true;
  const expiresAt = localStorage.getItem("atlasops_token_expires");
  if (!expiresAt) return true;
  // Consider expired 30 seconds before actual expiry for buffer
  return Date.now() > Number(expiresAt) - 30000;
}

// Tenant management
export function setTenantId(tenantId: string): void {
  if (typeof window !== "undefined") {
    localStorage.setItem("atlasops_tenant_id", tenantId);
  }
}

export function getTenantId(): string | null {
  if (typeof window === "undefined") return null;
  return localStorage.getItem("atlasops_tenant_id");
}

// Refresh token logic
async function refreshAccessToken(): Promise<boolean> {
  const refreshToken = getRefreshToken();
  if (!refreshToken) return false;

  try {
    const response = await fetch(`${API_BASE_URL}/auth/refresh`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ refreshToken }),
    });

    if (!response.ok) {
      clearTokens();
      return false;
    }

    const tokens: TokenPair = await response.json();
    setTokens(tokens);
    return true;
  } catch {
    clearTokens();
    return false;
  }
}

// API request function with auth and tenant headers
async function apiRequest<T>(
  endpoint: string,
  options: RequestInit = {},
): Promise<T> {
  // Auto-refresh expired tokens before making request
  if (isTokenExpired() && getRefreshToken()) {
    const refreshed = await refreshAccessToken();
    if (!refreshed) {
      throw new Error("Session expired. Please log in again.");
    }
  }

  const headers: HeadersInit = {
    "Content-Type": "application/json",
    ...options.headers,
  };

  // Add Authorization header if we have a token
  if (accessToken) {
    (headers as Record<string, string>)["Authorization"] =
      `Bearer ${accessToken}`;
  }

  // Add X-Tenant-ID header if we have a tenant
  const tenantId = getTenantId();
  if (tenantId) {
    (headers as Record<string, string>)["X-Tenant-ID"] = tenantId;
  }

  const response = await fetch(`${API_BASE_URL}${endpoint}`, {
    ...options,
    headers,
  });

  // Handle 401 - try to refresh token once
  if (response.status === 401 && getRefreshToken()) {
    const refreshed = await refreshAccessToken();
    if (refreshed) {
      // Retry the request with new token
      (headers as Record<string, string>)["Authorization"] =
        `Bearer ${accessToken}`;
      const retryResponse = await fetch(`${API_BASE_URL}${endpoint}`, {
        ...options,
        headers,
      });
      if (!retryResponse.ok) {
        const error: ApiError = await retryResponse.json();
        throw error;
      }
      return retryResponse.json();
    }
    throw new Error("Session expired. Please log in again.");
  }

  if (!response.ok) {
    const error: ApiError = await response.json();
    throw error;
  }

  // Handle 204 No Content
  if (response.status === 204) {
    return undefined as T;
  }

  return response.json();
}

// HTTP method helpers
export const api = {
  get: <T>(endpoint: string) => apiRequest<T>(endpoint, { method: "GET" }),

  post: <T>(endpoint: string, body?: unknown) =>
    apiRequest<T>(endpoint, {
      method: "POST",
      body: body ? JSON.stringify(body) : undefined,
    }),

  put: <T>(endpoint: string, body: unknown) =>
    apiRequest<T>(endpoint, {
      method: "PUT",
      body: JSON.stringify(body),
    }),

  patch: <T>(endpoint: string, body: unknown) =>
    apiRequest<T>(endpoint, {
      method: "PATCH",
      body: JSON.stringify(body),
    }),

  delete: <T>(endpoint: string) =>
    apiRequest<T>(endpoint, { method: "DELETE" }),
};

// Auth API
export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
  user: {
    id: string;
    email: string;
    name: string;
    role: "ADMIN" | "ANALYST" | "CLIENT";
    tenantId: string;
  };
}

export async function login(credentials: LoginRequest): Promise<LoginResponse> {
  const response = await fetch(`${API_BASE_URL}/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(credentials),
  });

  if (!response.ok) {
    const error: ApiError = await response.json();
    throw error;
  }

  const data: LoginResponse = await response.json();
  setTokens({
    accessToken: data.accessToken,
    refreshToken: data.refreshToken,
    expiresIn: data.expiresIn,
  });
  setTenantId(data.user.tenantId);

  return data;
}

export async function logout(): Promise<void> {
  try {
    await api.post("/auth/logout");
  } finally {
    clearTokens();
  }
}

// Type exports
export type { ApiError, PageResponse, TokenPair };
export { API_BASE_URL };
