"use client";

import { AlertTriangle, Info, Lock } from "lucide-react";
import { cn } from "@/lib/utils";

// ─── MaintenanceBanner ────────────────────────────────────────────────────────

export interface MaintenanceBannerProps {
  visible?: boolean;
  message?: string;
  className?: string;
}

/**
 * Yellow top banner displayed when the system is in maintenance mode.
 * Only read operations are allowed while this is visible.
 */
export function MaintenanceBanner({
  visible = true,
  message = "System in maintenance mode — read only",
  className,
}: MaintenanceBannerProps) {
  if (!visible) return null;

  return (
    <div
      role="banner"
      aria-live="polite"
      className={cn(
        "flex w-full items-center gap-2 border-b border-yellow-200 bg-yellow-50 px-4 py-2 text-sm text-yellow-800",
        className
      )}
    >
      <AlertTriangle className="h-4 w-4 flex-shrink-0 text-yellow-600" aria-hidden="true" />
      <span>{message}</span>
    </div>
  );
}

// ─── DegradedBanner ───────────────────────────────────────────────────────────

export interface DegradedBannerProps {
  visible?: boolean;
  dependency?: string;
  message?: string;
  className?: string;
}

/**
 * Orange banner for a degraded dependency (e.g., AI analysis unavailable).
 */
export function DegradedBanner({
  visible = true,
  dependency,
  message,
  className,
}: DegradedBannerProps) {
  if (!visible) return null;

  const displayMessage =
    message ?? (dependency ? `${dependency} is currently unavailable` : "A dependency is degraded");

  return (
    <div
      role="status"
      aria-live="polite"
      className={cn(
        "flex w-full items-center gap-2 border-b border-orange-200 bg-orange-50 px-4 py-2 text-sm text-orange-800",
        className
      )}
    >
      <AlertTriangle className="h-4 w-4 flex-shrink-0 text-orange-600" aria-hidden="true" />
      <span>{displayMessage}</span>
    </div>
  );
}

// ─── StaleProjectionBanner ────────────────────────────────────────────────────

export interface StaleProjectionBannerProps {
  visible?: boolean;
  projectionName?: string;
  className?: string;
}

/**
 * Info banner shown when the search index or another projection may be outdated.
 */
export function StaleProjectionBanner({
  visible = true,
  projectionName = "Search index",
  className,
}: StaleProjectionBannerProps) {
  if (!visible) return null;

  return (
    <div
      role="status"
      aria-live="polite"
      className={cn(
        "flex w-full items-center gap-2 border-b border-blue-200 bg-blue-50 px-4 py-2 text-sm text-blue-800",
        className
      )}
    >
      <Info className="h-4 w-4 flex-shrink-0 text-blue-600" aria-hidden="true" />
      <span>{projectionName} may be outdated. Results might not reflect recent changes.</span>
    </div>
  );
}

// ─── PermissionDeniedState ────────────────────────────────────────────────────

export interface PermissionDeniedStateProps {
  onBack?: () => void;
  message?: string;
  className?: string;
}

/**
 * Full-page 403 permission denied state with icon and back button.
 */
export function PermissionDeniedState({
  onBack,
  message = "You do not have permission to access this resource.",
  className,
}: PermissionDeniedStateProps) {
  return (
    <div
      role="main"
      aria-label="Permission denied"
      className={cn(
        "flex min-h-[400px] flex-col items-center justify-center px-6 py-12 text-center",
        className
      )}
    >
      <div className="mb-6 flex h-16 w-16 items-center justify-center rounded-full bg-red-50">
        <Lock className="h-8 w-8 text-red-500" aria-hidden="true" />
      </div>
      <h1 className="mb-2 text-2xl font-semibold text-gray-900">Access Denied</h1>
      <p className="mb-6 max-w-sm text-sm text-gray-500">{message}</p>
      {onBack && (
        <button
          onClick={onBack}
          className="inline-flex items-center gap-2 rounded-md border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 focus:ring-2 focus:ring-gray-500 focus:ring-offset-2 focus:outline-none"
        >
          Go back
        </button>
      )}
    </div>
  );
}
