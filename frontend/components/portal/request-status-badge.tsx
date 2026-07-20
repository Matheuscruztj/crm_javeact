/**
 * RequestStatusBadge — reusable badge for service request status.
 * Used in portal request list and detail pages.
 * Validates: frontend-conventions.md — composable UI components
 */

interface RequestStatusBadgeProps {
  status: string;
  className?: string;
}

const STATUS_COLORS: Record<string, string> = {
  OPEN: "bg-blue-100 text-blue-800",
  IN_PROGRESS: "bg-yellow-100 text-yellow-800",
  PENDING_APPROVAL: "bg-orange-100 text-orange-800",
  RESOLVED: "bg-green-100 text-green-800",
  CLOSED: "bg-gray-100 text-gray-600",
};

export function RequestStatusBadge({ status, className = "" }: RequestStatusBadgeProps) {
  const colors = STATUS_COLORS[status] ?? "bg-muted text-muted-foreground";
  const label = status.replace(/_/g, " ");

  return (
    <span
      className={`inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium ${colors} ${className}`}
      aria-label={`Request status: ${label}`}
    >
      {label}
    </span>
  );
}
