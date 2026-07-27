import fs from "node:fs";
import path from "node:path";

function readJson(filePath) {
  return JSON.parse(fs.readFileSync(filePath, "utf8"));
}

function formatNumber(value, fractionDigits = 2) {
  if (typeof value !== "number" || Number.isNaN(value)) {
    return "n/a";
  }
  return value.toFixed(fractionDigits);
}

function metricValue(summary, metricName, stat) {
  return summary?.metrics?.[metricName]?.[stat];
}

function renderMetricRow(summary, name, label, stat = "avg", unit = "ms") {
  const value = metricValue(summary, name, stat);
  return `<tr><td>${label}</td><td>${formatNumber(value)} ${unit}</td></tr>`;
}

const [, , summaryFile, jsonFile, htmlFile] = process.argv;

if (!summaryFile || !jsonFile || !htmlFile) {
  console.error(
    "Usage: node tests/load/generate-report.mjs <summary.json> <results.json> <report.html>",
  );
  process.exit(1);
}

const summary = readJson(summaryFile);
const results = readJson(jsonFile);
const scenario = summary?.root_group?.name || path.basename(summaryFile);

const totalRequests = metricValue(summary, "http_reqs", "count");
const requestRate = metricValue(summary, "http_reqs", "rate");
const failedRequests = metricValue(summary, "http_req_failed", "rate");
const durationP95 = metricValue(summary, "http_req_duration", "p(95)");
const durationP99 = metricValue(summary, "http_req_duration", "p(99)");

const html = `<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>k6 Performance Report - ${scenario}</title>
  <style>
    :root { color-scheme: light; font-family: Inter, system-ui, sans-serif; }
    body { margin: 0; padding: 32px; background: #f8fafc; color: #0f172a; }
    main { max-width: 960px; margin: 0 auto; background: white; border: 1px solid #e2e8f0; border-radius: 16px; padding: 32px; box-shadow: 0 10px 30px rgba(15, 23, 42, 0.06); }
    h1, h2 { margin-top: 0; }
    table { width: 100%; border-collapse: collapse; margin: 16px 0 24px; }
    td, th { text-align: left; border-bottom: 1px solid #e2e8f0; padding: 10px 12px; }
    code { background: #e2e8f0; padding: 2px 6px; border-radius: 6px; }
    .grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 12px; }
    .card { border: 1px solid #e2e8f0; border-radius: 12px; padding: 16px; background: #f8fafc; }
    .muted { color: #475569; }
    pre { overflow: auto; background: #0f172a; color: #e2e8f0; padding: 16px; border-radius: 12px; }
  </style>
</head>
<body>
  <main>
    <h1>k6 Performance Report</h1>
    <p class="muted">Scenario <code>${scenario}</code> generated from <code>${path.basename(jsonFile)}</code>.</p>
    <section class="grid">
      <div class="card"><strong>Total requests</strong><div>${formatNumber(totalRequests, 0)}</div></div>
      <div class="card"><strong>Request rate</strong><div>${formatNumber(requestRate)} req/s</div></div>
      <div class="card"><strong>Failed request rate</strong><div>${formatNumber(failedRequests * 100)}%</div></div>
      <div class="card"><strong>P95 duration</strong><div>${formatNumber(durationP95)} ms</div></div>
      <div class="card"><strong>P99 duration</strong><div>${formatNumber(durationP99)} ms</div></div>
    </section>
    <h2>Key metrics</h2>
    <table>
      <tbody>
        ${renderMetricRow(summary, "http_req_duration", "HTTP request duration P95", "p(95)")}
        ${renderMetricRow(summary, "http_req_duration", "HTTP request duration P99", "p(99)")}
        ${renderMetricRow(summary, "http_req_duration", "HTTP request duration avg", "avg")}
        ${renderMetricRow(summary, "http_req_failed", "HTTP request failed rate", "rate", "%")}
      </tbody>
    </table>
    <h2>Raw summary</h2>
    <pre>${JSON.stringify(results, null, 2)}</pre>
  </main>
</body>
</html>`;

fs.mkdirSync(path.dirname(htmlFile), { recursive: true });
fs.writeFileSync(htmlFile, html);
