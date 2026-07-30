/**
 * CustomerStatusBadge — reusable badge for customer active/inactive status.
 * Used in admin customer list and detail pages.
 * Validates: frontend-conventions.md — component composition over inheritance
 */
interface CustomerStatusBadgeProps {
  status: "ACTIVE" | "INACTIVE";
  className?: string;
}

export function CustomerStatusBadge({ status, className = "" }: CustomerStatusBadgeProps) {
  const colors = status === "ACTIVE" ? "bg-green-100 text-green-800" : "bg-gray-100 text-gray-600";

  return (
    <span
      className={`inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium ${colors} ${className}`}
      aria-label={`Status: ${status}`}
    >
      {status}
    </span>
  );
}
