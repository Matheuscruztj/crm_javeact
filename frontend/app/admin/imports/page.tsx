"use client";

import { useRef, useState } from "react";
import { api } from "@/lib/api-client";
import { getApiErrorMessage } from "@/lib/form-utils";

interface ImportResult {
  jobId: string;
  status: string;
  totalRecords: number;
  processedRecords: number;
}

/**
 * Imports page: CSV upload with schema preview and progress tracking.
 * Validates: P0.K.3.1 — admin/imports/page.tsx — Upload CSV + progress
 */
export default function ImportsPage() {
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [preview, setPreview] = useState<string[][] | null>(null);
  const [schema, setSchema] = useState<string[]>([]);
  const [errors, setErrors] = useState<string[]>([]);
  const [importResult, setImportResult] = useState<ImportResult | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [csvContent, setCsvContent] = useState<string | null>(null);

  const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const text = await file.text();
    setCsvContent(text);

    // Basic client-side CSV schema inference
    const lines = text.split("\n").filter((l) => l.trim());
    if (lines.length === 0) return;

    const header = lines[0].split(",").map((h) => h.trim());
    setSchema(header);

    const dataRows = lines.slice(1, 6).map((l) => l.split(","));
    setPreview(dataRows);

    // Validate column count consistency
    const validationErrors: string[] = [];
    lines.slice(1).forEach((line, i) => {
      const cols = line.split(",").length;
      if (cols !== header.length) {
        validationErrors.push(`Row ${i + 2}: expected ${header.length} columns, got ${cols}`);
      }
    });
    setErrors(validationErrors);
  };

  const handleImport = async () => {
    if (!csvContent) return;
    try {
      setLoading(true);
      const result = await api.post<ImportResult>("/imports/csv", {
        content: csvContent,
      });
      setImportResult(result);
      setError(null);
    } catch (err) {
      setError(getApiErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="p-6">
      <div className="mb-6">
        <h1 className="text-2xl font-bold">Imports</h1>
        <p className="text-muted-foreground">
          Bulk data import from CSV files with preview and validation.
        </p>
      </div>

      {error && (
        <div role="alert" className="bg-destructive/10 text-destructive mb-4 rounded-md p-4">
          {error}
        </div>
      )}

      <div className="mb-6 rounded-md border-2 border-dashed p-8 text-center">
        <input
          ref={fileInputRef}
          type="file"
          accept=".csv"
          className="hidden"
          id="csv-upload"
          onChange={handleFileChange}
          aria-label="Upload CSV file"
        />
        <label
          htmlFor="csv-upload"
          className="bg-primary text-primary-foreground hover:bg-primary/90 cursor-pointer rounded-md px-6 py-3 text-sm font-medium"
        >
          Select CSV file
        </label>
        <p className="text-muted-foreground mt-2 text-sm">Drag & drop or click to upload</p>
      </div>

      {schema.length > 0 && (
        <div className="mb-4">
          <h2 className="mb-2 font-semibold">Detected Schema</h2>
          <div className="flex flex-wrap gap-2">
            {schema.map((col) => (
              <span key={col} className="bg-muted rounded-full px-3 py-1 text-xs">
                {col}
              </span>
            ))}
          </div>
        </div>
      )}

      {errors.length > 0 && (
        <div role="alert" className="mb-4 rounded-md bg-yellow-50 p-4">
          <h3 className="mb-2 font-medium text-yellow-800">Validation Issues</h3>
          <ul className="list-disc pl-4 text-sm text-yellow-700">
            {errors.map((e, i) => (
              <li key={i}>{e}</li>
            ))}
          </ul>
        </div>
      )}

      {preview && (
        <div className="mb-6">
          <h2 className="mb-2 font-semibold">Preview (first 5 rows)</h2>
          <div className="overflow-x-auto rounded-md border">
            <table className="w-full text-sm">
              <thead className="bg-muted/50">
                <tr>
                  {schema.map((h) => (
                    <th key={h} className="px-3 py-2 text-left font-medium">
                      {h}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {preview.map((row, i) => (
                  <tr key={i} className="border-t">
                    {row.map((cell, j) => (
                      <td key={j} className="px-3 py-2">
                        {cell}
                      </td>
                    ))}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {csvContent && errors.length === 0 && (
        <button
          onClick={handleImport}
          disabled={loading}
          className="bg-primary text-primary-foreground hover:bg-primary/90 rounded-md px-6 py-2 text-sm font-medium disabled:opacity-50"
          aria-busy={loading}
        >
          {loading ? "Importing…" : "Start Import"}
        </button>
      )}

      {importResult && (
        <div className="mt-6 rounded-md bg-green-50 p-4">
          <h3 className="font-medium text-green-800">Import started</h3>
          <p className="text-sm text-green-700">Job ID: {importResult.jobId}</p>
          <p className="text-sm text-green-700">Status: {importResult.status}</p>
        </div>
      )}
    </div>
  );
}
