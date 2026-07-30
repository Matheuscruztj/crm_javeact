/**
 * SkipNav: provides keyboard users a way to skip repetitive navigation.
 * Place at the top of every layout, before any nav content.
 * Validates: P1.17.3 — Keyboard navigation
 */
export function SkipNav({ targetId = "main-content" }: { targetId?: string }) {
  return (
    <a
      href={`#${targetId}`}
      className="focus:bg-primary focus:text-primary-foreground sr-only focus:not-sr-only focus:absolute focus:top-4 focus:left-4 focus:z-50 focus:rounded-md focus:px-4 focus:py-2 focus:text-sm focus:font-medium focus:outline-none"
    >
      Skip to main content
    </a>
  );
}
