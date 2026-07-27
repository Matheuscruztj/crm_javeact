import { chromium } from "@playwright/test";
import fs from "node:fs";
import path from "node:path";

const BASE_URL = process.env.FRONTEND_BASE_URL || "http://localhost:3000";
const OUTPUT_FILE = process.env.FRONTEND_PERF_OUTPUT || "frontend/perf-results/critical-path.json";
const ROUTES = [
  "/login",
  "/portal/home",
  "/admin/dashboard",
  "/admin/search",
];

const browser = await chromium.launch({ headless: true });
const page = await browser.newPage();
const results = [];

for (const route of ROUTES) {
  const start = performance.now();
  const response = await page.goto(`${BASE_URL}${route}`, { waitUntil: "networkidle" });
  const durationMs = performance.now() - start;
  results.push({
    route,
    status: response?.status() ?? null,
    durationMs: Number(durationMs.toFixed(2)),
  });
}

await browser.close();

fs.mkdirSync(path.dirname(OUTPUT_FILE), { recursive: true });
fs.writeFileSync(
  OUTPUT_FILE,
  JSON.stringify(
    {
      baseUrl: BASE_URL,
      generatedAt: new Date().toISOString(),
      results,
    },
    null,
    2,
  ),
);

console.log(`Frontend performance evidence written to ${OUTPUT_FILE}`);
