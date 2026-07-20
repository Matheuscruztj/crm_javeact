/**
 * Developers page: embedded Swagger, AsyncAPI, SSE/webhook examples.
 * Validates: P3.9.3 — /developers
 */

const API_BASE = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080/api/v1";
const SWAGGER_URL = API_BASE.replace("/api/v1", "") + "/swagger-ui.html";
const ASYNCAPI_URL = "/docs/asyncapi.yaml";

const examples = [
  {
    title: "SSE Connection",
    lang: "JavaScript",
    code: `const token = localStorage.getItem("atlasops_access_token");
const tenantId = localStorage.getItem("atlasops_tenant_id");

const source = new EventSource(
  \`/api/v1/events/stream?token=\${token}\`,
  { withCredentials: true }
);

source.addEventListener("notification", (e) => {
  const data = JSON.parse(e.data);
  console.log("Notification:", data.title);
});

source.addEventListener("document.analyzed", (e) => {
  const data = JSON.parse(e.data);
  console.log("Document analyzed:", data.documentId);
});`,
  },
  {
    title: "Webhook Dispatch",
    lang: "cURL",
    code: `curl -X POST https://api.atlasops.io/api/v1/integrations/webhooks/dispatch \\
  -H "Authorization: Bearer \${TOKEN}" \\
  -H "X-Tenant-ID: \${TENANT_ID}" \\
  -H "Content-Type: application/json" \\
  -d '{
    "targetUrl": "https://your-endpoint.com/hook",
    "eventType": "document.analyzed",
    "payload": "{\\"documentId\\": \\"doc-123\\"}"
  }'`,
  },
  {
    title: "Paginated List",
    lang: "TypeScript",
    code: `import { api } from "@/lib/api-client";

interface Customer { id: string; name: string; status: string; }

const result = await api.get<{
  content: Customer[];
  page: { totalElements: number; totalPages: number };
}>("/customers?page=0&size=20");

console.log(result.page.totalElements, "total customers");`,
  },
];

export default function DevelopersPage() {
  return (
    <div className="p-6 space-y-8 max-w-4xl">
      <div>
        <h1 className="text-2xl font-bold">Developer Documentation</h1>
        <p className="text-muted-foreground">API docs, async events, and integration examples.</p>
      </div>

      {/* API Documentation Links */}
      <section aria-labelledby="api-docs-heading">
        <h2 id="api-docs-heading" className="mb-4 text-lg font-semibold border-b pb-2">
          API Documentation
        </h2>
        <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
          <a
            href={SWAGGER_URL}
            target="_blank"
            rel="noopener noreferrer"
            className="flex items-center gap-3 rounded-md border p-4 hover:bg-muted transition-colors"
            aria-label="Open Swagger UI in new tab"
          >
            <div className="rounded bg-green-100 p-2 text-green-700 text-xs font-bold">OAS</div>
            <div>
              <p className="font-medium text-sm">Swagger UI</p>
              <p className="text-xs text-muted-foreground">Interactive REST API explorer</p>
            </div>
          </a>
          <a
            href={ASYNCAPI_URL}
            target="_blank"
            rel="noopener noreferrer"
            className="flex items-center gap-3 rounded-md border p-4 hover:bg-muted transition-colors"
            aria-label="Open AsyncAPI spec in new tab"
          >
            <div className="rounded bg-purple-100 p-2 text-purple-700 text-xs font-bold">ASY</div>
            <div>
              <p className="font-medium text-sm">AsyncAPI Spec</p>
              <p className="text-xs text-muted-foreground">SSE channels and domain events</p>
            </div>
          </a>
        </div>
      </section>

      {/* Code Examples */}
      <section aria-labelledby="examples-heading">
        <h2 id="examples-heading" className="mb-4 text-lg font-semibold border-b pb-2">
          Integration Examples
        </h2>
        <div className="space-y-4">
          {examples.map((ex) => (
            <div key={ex.title} className="rounded-md border overflow-hidden">
              <div className="flex items-center justify-between bg-muted/50 px-4 py-2">
                <span className="font-medium text-sm">{ex.title}</span>
                <span className="text-xs text-muted-foreground">{ex.lang}</span>
              </div>
              <pre className="overflow-x-auto p-4 text-xs">
                <code>{ex.code}</code>
              </pre>
            </div>
          ))}
        </div>
      </section>

      {/* Base URL info */}
      <section aria-labelledby="base-url-heading">
        <h2 id="base-url-heading" className="mb-3 text-lg font-semibold border-b pb-2">
          Base URL
        </h2>
        <div className="rounded-md bg-muted px-4 py-3 font-mono text-sm">
          {API_BASE}
        </div>
        <p className="mt-2 text-xs text-muted-foreground">
          Set via <code>NEXT_PUBLIC_API_URL</code> environment variable.
        </p>
      </section>
    </div>
  );
}
