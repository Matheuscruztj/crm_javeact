/**
 * AsyncAPI documentation viewer page.
 * Renders the asyncapi.yaml specification using an embedded iframe viewer.
 * Accessible at /asyncapi (per P0.G.3 acceptance criteria).
 *
 * Validates: P0.G.3 — AsyncAPI Documentation
 */
export default function AsyncApiPage() {
  return (
    <main className="flex min-h-screen flex-col">
      <div className="border-b px-6 py-4">
        <h1 className="text-xl font-semibold">AsyncAPI — Event Channels</h1>
        <p className="text-muted-foreground text-sm">
          Server-Sent Events (SSE) channels and domain events for AtlasOps AI.
        </p>
      </div>

      {/* Embedded AsyncAPI viewer via asyncapi-react CDN */}
      <div className="flex-1">
        <iframe
          src="https://studio.asyncapi.com/?url=/docs/asyncapi.yaml&readOnly=true"
          className="h-full min-h-screen w-full border-0"
          title="AsyncAPI documentation"
          aria-label="AsyncAPI documentation viewer"
          sandbox="allow-scripts allow-same-origin allow-forms allow-popups"
        />
      </div>

      {/* Fallback: direct download link */}
      <div className="text-muted-foreground border-t px-6 py-3 text-sm">
        <a
          href="/docs/asyncapi.yaml"
          download="asyncapi.yaml"
          className="text-primary underline hover:no-underline"
        >
          Download asyncapi.yaml
        </a>
        {" · "}
        <a
          href="/docs/asyncapi.yaml"
          target="_blank"
          rel="noopener noreferrer"
          className="text-primary underline hover:no-underline"
        >
          View raw
        </a>
      </div>
    </main>
  );
}
