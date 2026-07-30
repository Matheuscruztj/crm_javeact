/**
 * Accessibility utilities for WCAG AA compliance.
 * Validates: P1.17 — Accessibility Baseline
 *
 * Note: Full WCAG validation requires manual testing with assistive technologies
 * and expert accessibility review beyond automated checks.
 */

/**
 * Creates an accessible ID for connecting labels to inputs.
 * Usage: const id = useId(); <Label htmlFor={id}> + <Input id={id}>
 */
export function generateId(prefix: string): string {
  return `${prefix}-${Math.random().toString(36).slice(2, 9)}`;
}

/**
 * Returns ARIA live region props for dynamic content announcements.
 * Use on containers that update without page navigation.
 */
export function liveRegionProps(
  politeness: "polite" | "assertive" = "polite"
): React.AriaAttributes {
  return {
    "aria-live": politeness,
    "aria-atomic": true,
  };
}

/**
 * Returns props for a visually hidden but screen-reader-visible element.
 * For supplemental text that should only be heard, not seen.
 */
export const srOnly: React.CSSProperties = {
  position: "absolute",
  width: "1px",
  height: "1px",
  padding: 0,
  margin: "-1px",
  overflow: "hidden",
  clip: "rect(0, 0, 0, 0)",
  whiteSpace: "nowrap",
  borderWidth: 0,
};

/**
 * Minimum contrast ratio constants per WCAG 2.1 AA.
 */
export const WCAG_AA = {
  NORMAL_TEXT: 4.5,
  LARGE_TEXT: 3.0,
  UI_COMPONENT: 3.0,
} as const;

/**
 * Focus trap for modals and dialogs.
 * Returns first and last focusable elements within container.
 */
export function getFocusableElements(container: HTMLElement): HTMLElement[] {
  const selectors = [
    "a[href]",
    "button:not([disabled])",
    "input:not([disabled])",
    "select:not([disabled])",
    "textarea:not([disabled])",
    "[tabindex]:not([tabindex='-1'])",
  ].join(",");
  return Array.from(container.querySelectorAll<HTMLElement>(selectors));
}

/**
 * Handles keyboard navigation inside a focus trap (modals).
 */
export function handleFocusTrap(e: KeyboardEvent, container: HTMLElement): void {
  if (e.key !== "Tab") return;
  const focusable = getFocusableElements(container);
  if (focusable.length === 0) return;

  const first = focusable[0];
  const last = focusable[focusable.length - 1];

  if (e.shiftKey) {
    if (document.activeElement === first) {
      e.preventDefault();
      last.focus();
    }
  } else {
    if (document.activeElement === last) {
      e.preventDefault();
      first.focus();
    }
  }
}
