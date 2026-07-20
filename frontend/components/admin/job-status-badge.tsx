/**
 * JobStatusBadge — reusable badge for background job status.
 * Used in admin operations page.
 * Validates: frontend-conventions.md — component composition
 */

type JobStatus = "QUEUED" | "RUNNING" | "COMPLETED" | "FAILED" | "CANCELLED";

interface JobStatusBadgeProps {
  status: JobStatus;
  className?: string;
}

const STATUS_COLORS: Record<JobStatus, string> = {
  QUEUED: "bg-yellow-100 text-yellow-800",
  RUNNING: "bg-blue-100 text-blue-800",
  COMPLETED: "bg-green-100 text-green-800",
  FAILED: "bg-red-100 text-red-800",
  CANCELLED: "bg-gray-100 text-gray-600",
};

export function JobStatusBadge({ status, className = "" }: JobStatusBadgeProps) {
  return (
    <span
      className={`inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium ${STATUS_COLORS[status]} ${className}`}
      aria-label={`Job status: ${status}`}
    >
      {status}
    </span>
  );
}
