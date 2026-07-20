/**
 * VisuallyHidden: renders children only for screen readers.
 * Validates: P1.17.1 — Accessibility baseline (sr-only pattern)
 */
import { srOnly } from "@/lib/accessibility";

interface VisuallyHiddenProps {
  children: React.ReactNode;
  as?: keyof React.JSX.IntrinsicElements;
}

export function VisuallyHidden({ children, as: Tag = "span" }: VisuallyHiddenProps) {
  return <Tag style={srOnly}>{children}</Tag>;
}
