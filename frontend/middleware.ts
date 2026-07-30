/**
 * Next.js Middleware for route protection.
 *
 * Redirects unauthenticated users to /login.
 * Redirects authenticated users away from /login to their role-based dashboard.
 * Validates role-based access for admin/* vs portal/* routes.
 *
 * P0.L.1 — Frontend Auth State Management (route protection middleware)
 */

import { NextRequest, NextResponse } from "next/server";

/** Routes that do not require authentication. */
const PUBLIC_PATHS = ["/login", "/api/"];

/** Routes that require ADMIN or ANALYST role. */
const ADMIN_PATHS = ["/admin"];

/** Routes that require CLIENT role (or above). */
const PORTAL_PATHS = ["/portal"];

function isPublicPath(pathname: string): boolean {
  return PUBLIC_PATHS.some((prefix) => pathname.startsWith(prefix));
}

function isAdminPath(pathname: string): boolean {
  return ADMIN_PATHS.some((prefix) => pathname.startsWith(prefix));
}

function isPortalPath(pathname: string): boolean {
  return PORTAL_PATHS.some((prefix) => pathname.startsWith(prefix));
}

export function middleware(request: NextRequest): NextResponse {
  const { pathname } = request.nextUrl;

  // Skip Next.js internals and static files
  if (pathname.startsWith("/_next") || pathname.startsWith("/favicon") || pathname.includes(".")) {
    return NextResponse.next();
  }

  // Read refresh token from cookie (set by login flow)
  // Access tokens are in-memory; we use the presence of the refresh token
  // cookie as a proxy for "authenticated" in the middleware context.
  const refreshToken =
    request.cookies.get("atlasops_refresh_token")?.value ??
    // Fallback: check localStorage via custom header set by client code
    request.headers.get("x-authenticated");

  const userRole = request.cookies.get("atlasops_user_role")?.value;

  const isAuthenticated = !!refreshToken;

  // Redirect unauthenticated users to login (except public paths)
  if (!isAuthenticated && !isPublicPath(pathname)) {
    const loginUrl = new URL("/login", request.url);
    loginUrl.searchParams.set("redirect", pathname);
    return NextResponse.redirect(loginUrl);
  }

  // Redirect authenticated users away from login page
  if (isAuthenticated && pathname === "/login") {
    const role = userRole as "ADMIN" | "ANALYST" | "CLIENT" | undefined;
    const destination = role === "CLIENT" ? "/portal/home" : "/admin/dashboard";
    return NextResponse.redirect(new URL(destination, request.url));
  }

  // Role-based path protection
  if (isAuthenticated && userRole) {
    // Admin paths require ADMIN or ANALYST role
    if (isAdminPath(pathname) && userRole === "CLIENT") {
      return NextResponse.redirect(new URL("/portal/home", request.url));
    }

    // Portal paths: any authenticated user can access
    // (resource-level authorization handled by the API)
  }

  // Redirect root to appropriate dashboard
  if (pathname === "/" && isAuthenticated && userRole) {
    const destination = userRole === "CLIENT" ? "/portal/home" : "/admin/dashboard";
    return NextResponse.redirect(new URL(destination, request.url));
  }

  return NextResponse.next();
}

export const config = {
  matcher: [
    /*
     * Match all request paths except for the ones starting with:
     * - _next/static (static files)
     * - _next/image (image optimization files)
     * - favicon.ico (favicon file)
     */
    "/((?!_next/static|_next/image|favicon.ico).*)",
  ],
};
