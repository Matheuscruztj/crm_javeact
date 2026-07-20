/**
 * DocumentStatusBadge — reusable badge for document processing status.
 * Used in portal document list and detail pages.
 * Validates: frontend-conventions.md — composable UI components
 */

interface DocumentStatusBadgeProps {
  status: string;
  className?: string;
}

const STATUS_COLORS: Record<string, string> = {
  PENDING_UPLOAD: "bg-gray-100 text-gray-600",
  UPLOADED: "bg-blue-100 text-blue-800",
  TEXT_EXTRACTED: "bg-indigo-100 text-indigo-800",
  ANALYZED: "bg-green-100 text-green-800",
  PROCESSING_FAILED: "bg-red-100 text-red-800",
  UPLOAD_FAILED: "bg-red-50 text-red-700",
};

export function DocumentStatusBadge({ status, className = "" }: DocumentStatusBadgeProps) {
  const colors = STATUS_COLORS[status] ?? "bg-muted text-muted-foreground";
  const label = status.replace(/_/g, " ");

  return (
    <span
      className={`inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium ${colors} ${className}`}
      aria-label={`Document status: ${label}`}
    >
      {label}
    </span>
  );
}
