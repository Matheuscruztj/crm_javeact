/**
 * Documents list page with status badges and approval actions.
 * Task 22.4: Implement documents list page with status badges and approval actions.
 * - Create `/app/admin/documents/page.tsx` with status indicators (color-coded badges)
 * - Links to approval actions from document rows
 * Requirements: 22.5
 */

"use client";

import { useCallback, useEffect, useState } from "react";
import Link from "next/link";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Skeleton } from "@/components/ui/skeleton";
import { api, type PageResponse } from "@/lib/api-client";

type DocumentStatus =
  "UPLOADED" | "TEXT_EXTRACTED" | "ANALYZING" | "ANALYZED" | "PROCESSING_FAILED";

interface Document {
  id: string;
  filename: string;
  contentType: string;
  status: DocumentStatus;
  size: number;
  requestId: string;
  requestTitle: string;
  uploadedAt: string;
  hasApproval: boolean;
  approvalId?: string;
  approvalStatus?: "PENDING" | "APPROVED" | "REJECTED" | "CANCELLED";
}

function getStatusBadgeClass(status: DocumentStatus): string {
  switch (status) {
    case "UPLOADED":
      return "bg-gray-100 text-gray-800 border-gray-200";
    case "TEXT_EXTRACTED":
      return "bg-blue-100 text-blue-800 border-blue-200";
    case "ANALYZING":
      return "bg-yellow-100 text-yellow-800 border-yellow-200";
    case "ANALYZED":
      return "bg-green-100 text-green-800 border-green-200";
    case "PROCESSING_FAILED":
      return "bg-red-100 text-red-800 border-red-200";
    default:
      return "";
  }
}

function getApprovalBadgeClass(status: Document["approvalStatus"]): string {
  switch (status) {
    case "PENDING":
      return "bg-yellow-100 text-yellow-800 border-yellow-200";
    case "APPROVED":
      return "bg-green-100 text-green-800 border-green-200";
    case "REJECTED":
      return "bg-red-100 text-red-800 border-red-200";
    case "CANCELLED":
      return "bg-gray-100 text-gray-800 border-gray-200";
    default:
      return "";
  }
}

function formatFileSize(bytes: number): string {
  if (bytes === 0) return "0 B";
  const k = 1024;
  const sizes = ["B", "KB", "MB", "GB"];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + " " + sizes[i];
}

function getFileIcon(contentType: string): React.ReactNode {
  if (contentType.includes("pdf")) {
    return (
      <svg
        className="h-5 w-5 text-red-500"
        fill="currentColor"
        viewBox="0 0 24 24"
        aria-hidden="true"
      >
        <path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8l-6-6zM6 20V4h7v5h5v11H6z" />
      </svg>
    );
  }
  if (contentType.includes("word") || contentType.includes("document")) {
    return (
      <svg
        className="h-5 w-5 text-blue-500"
        fill="currentColor"
        viewBox="0 0 24 24"
        aria-hidden="true"
      >
        <path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8l-6-6zM6 20V4h7v5h5v11H6z" />
      </svg>
    );
  }
  if (contentType.includes("image")) {
    return (
      <svg
        className="h-5 w-5 text-green-500"
        fill="currentColor"
        viewBox="0 0 24 24"
        aria-hidden="true"
      >
        <path d="M21 19V5c0-1.1-.9-2-2-2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2zM8.5 13.5l2.5 3.01L14.5 12l4.5 6H5l3.5-4.5z" />
      </svg>
    );
  }
  return (
    <svg
      className="h-5 w-5 text-gray-500"
      fill="currentColor"
      viewBox="0 0 24 24"
      aria-hidden="true"
    >
      <path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8l-6-6zM6 20V4h7v5h5v11H6z" />
    </svg>
  );
}

function Pagination({
  page,
  totalPages,
  onPageChange,
}: {
  page: number;
  totalPages: number;
  onPageChange: (page: number) => void;
}) {
  return (
    <div className="flex items-center justify-between">
      <p className="text-muted-foreground text-sm">
        Page {page + 1} of {totalPages || 1}
      </p>
      <div className="flex gap-2">
        <Button
          variant="outline"
          size="sm"
          onClick={() => onPageChange(page - 1)}
          disabled={page === 0}
        >
          Previous
        </Button>
        <Button
          variant="outline"
          size="sm"
          onClick={() => onPageChange(page + 1)}
          disabled={page >= totalPages - 1}
        >
          Next
        </Button>
      </div>
    </div>
  );
}

export default function DocumentsPage() {
  const [documents, setDocuments] = useState<Document[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState<string>("ALL");
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  const fetchDocuments = useCallback(async () => {
    setIsLoading(true);
    setError(null);

    try {
      const params = new URLSearchParams({
        page: String(page),
        size: "20",
        sort: "uploadedAt,desc",
      });

      if (search.trim()) {
        params.append("search", search.trim());
      }

      if (statusFilter !== "ALL") {
        params.append("status", statusFilter);
      }

      const response = await api.get<PageResponse<Document>>(`/documents?${params.toString()}`);

      setDocuments(response.content);
      setTotalPages(response.page.totalPages);
      setTotalElements(response.page.totalElements);
    } catch {
      setError("Failed to load documents");
    } finally {
      setIsLoading(false);
    }
  }, [page, search, statusFilter]);

  useEffect(() => {
    fetchDocuments();
  }, [fetchDocuments]);

  // Debounced search
  useEffect(() => {
    const timer = setTimeout(() => {
      setPage(0);
    }, 300);
    return () => clearTimeout(timer);
  }, [search]);

  return (
    <div className="p-6">
      <div className="mb-6">
        <h1 className="text-2xl font-bold">Documents</h1>
        <p className="text-muted-foreground">
          Browse and manage uploaded documents. {totalElements > 0 && `(${totalElements} total)`}
        </p>
      </div>

      {/* Filters */}
      <div className="mb-4 flex flex-col gap-4 sm:flex-row">
        <div className="relative max-w-md flex-1">
          <svg
            className="text-muted-foreground absolute top-1/2 left-3 h-4 w-4 -translate-y-1/2"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
            aria-hidden="true"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
            />
          </svg>
          <Input
            type="search"
            placeholder="Search documents..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="pl-10"
            aria-label="Search documents"
          />
        </div>

        <select
          value={statusFilter}
          onChange={(e) => {
            setStatusFilter(e.target.value);
            setPage(0);
          }}
          className="border-input bg-background ring-offset-background focus:ring-ring rounded-md border px-3 py-2 text-sm focus:ring-2 focus:outline-none"
          aria-label="Filter by status"
        >
          <option value="ALL">All Statuses</option>
          <option value="UPLOADED">Uploaded</option>
          <option value="TEXT_EXTRACTED">Text Extracted</option>
          <option value="ANALYZING">Analyzing</option>
          <option value="ANALYZED">Analyzed</option>
          <option value="PROCESSING_FAILED">Processing Failed</option>
        </select>
      </div>

      {error && (
        <div
          className="bg-destructive/10 text-destructive mb-4 rounded-md p-4 text-sm"
          role="alert"
        >
          {error}
        </div>
      )}

      {/* Table for desktop */}
      <div className="hidden rounded-md border lg:block">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Document</TableHead>
              <TableHead>Request</TableHead>
              <TableHead>Status</TableHead>
              <TableHead>Approval</TableHead>
              <TableHead>Size</TableHead>
              <TableHead>Uploaded</TableHead>
              <TableHead className="w-[100px]">Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {isLoading ? (
              Array.from({ length: 5 }).map((_, i) => (
                <TableRow key={i}>
                  <TableCell>
                    <div className="flex items-center gap-2">
                      <Skeleton className="h-5 w-5" />
                      <Skeleton className="h-4 w-40" />
                    </div>
                  </TableCell>
                  <TableCell>
                    <Skeleton className="h-4 w-32" />
                  </TableCell>
                  <TableCell>
                    <Skeleton className="h-6 w-24" />
                  </TableCell>
                  <TableCell>
                    <Skeleton className="h-6 w-20" />
                  </TableCell>
                  <TableCell>
                    <Skeleton className="h-4 w-16" />
                  </TableCell>
                  <TableCell>
                    <Skeleton className="h-4 w-20" />
                  </TableCell>
                  <TableCell>
                    <Skeleton className="h-8 w-16" />
                  </TableCell>
                </TableRow>
              ))
            ) : documents.length === 0 ? (
              <TableRow>
                <TableCell colSpan={7} className="py-8 text-center">
                  <p className="text-muted-foreground">No documents found</p>
                </TableCell>
              </TableRow>
            ) : (
              documents.map((doc) => (
                <TableRow key={doc.id}>
                  <TableCell>
                    <div className="flex items-center gap-2">
                      {getFileIcon(doc.contentType)}
                      <Link
                        href={`/admin/documents/${doc.id}`}
                        className="max-w-[200px] truncate font-medium hover:underline"
                        title={doc.filename}
                      >
                        {doc.filename}
                      </Link>
                    </div>
                  </TableCell>
                  <TableCell>
                    <Link
                      href={`/admin/requests/${doc.requestId}`}
                      className="text-muted-foreground block max-w-[150px] truncate hover:underline"
                      title={doc.requestTitle}
                    >
                      {doc.requestTitle}
                    </Link>
                  </TableCell>
                  <TableCell>
                    <Badge className={getStatusBadgeClass(doc.status)}>
                      {doc.status.replace(/_/g, " ")}
                    </Badge>
                  </TableCell>
                  <TableCell>
                    {doc.hasApproval && doc.approvalStatus ? (
                      <Badge className={getApprovalBadgeClass(doc.approvalStatus)}>
                        {doc.approvalStatus}
                      </Badge>
                    ) : (
                      <span className="text-muted-foreground text-sm">—</span>
                    )}
                  </TableCell>
                  <TableCell className="text-muted-foreground">
                    {formatFileSize(doc.size)}
                  </TableCell>
                  <TableCell className="text-muted-foreground">
                    {new Date(doc.uploadedAt).toLocaleDateString()}
                  </TableCell>
                  <TableCell>
                    {doc.hasApproval && doc.approvalStatus === "PENDING" && (
                      <Link href={`/admin/approvals?documentId=${doc.id}`}>
                        <Button variant="outline" size="sm">
                          Review
                        </Button>
                      </Link>
                    )}
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>

      {/* Cards for mobile/tablet */}
      <div className="space-y-4 lg:hidden">
        {isLoading ? (
          Array.from({ length: 3 }).map((_, i) => (
            <div key={i} className="space-y-3 rounded-lg border p-4">
              <div className="flex items-center gap-2">
                <Skeleton className="h-5 w-5" />
                <Skeleton className="h-5 w-48" />
              </div>
              <Skeleton className="h-4 w-32" />
              <div className="flex gap-2">
                <Skeleton className="h-6 w-24" />
                <Skeleton className="h-6 w-20" />
              </div>
              <div className="flex justify-between">
                <Skeleton className="h-4 w-16" />
                <Skeleton className="h-4 w-20" />
              </div>
            </div>
          ))
        ) : documents.length === 0 ? (
          <div className="py-8 text-center">
            <p className="text-muted-foreground">No documents found</p>
          </div>
        ) : (
          documents.map((doc) => (
            <div key={doc.id} className="space-y-3 rounded-lg border p-4">
              <div className="flex items-center gap-2">
                {getFileIcon(doc.contentType)}
                <Link
                  href={`/admin/documents/${doc.id}`}
                  className="truncate font-medium hover:underline"
                >
                  {doc.filename}
                </Link>
              </div>
              <Link
                href={`/admin/requests/${doc.requestId}`}
                className="text-muted-foreground block text-sm hover:underline"
              >
                {doc.requestTitle}
              </Link>
              <div className="flex flex-wrap gap-2">
                <Badge className={getStatusBadgeClass(doc.status)}>
                  {doc.status.replace(/_/g, " ")}
                </Badge>
                {doc.hasApproval && doc.approvalStatus && (
                  <Badge className={getApprovalBadgeClass(doc.approvalStatus)}>
                    {doc.approvalStatus}
                  </Badge>
                )}
              </div>
              <div className="flex items-center justify-between border-t pt-2">
                <span className="text-muted-foreground text-sm">{formatFileSize(doc.size)}</span>
                <span className="text-muted-foreground text-sm">
                  {new Date(doc.uploadedAt).toLocaleDateString()}
                </span>
              </div>
              {doc.hasApproval && doc.approvalStatus === "PENDING" && (
                <Link href={`/admin/approvals?documentId=${doc.id}`}>
                  <Button variant="outline" size="sm" className="w-full">
                    Review Approval
                  </Button>
                </Link>
              )}
            </div>
          ))
        )}
      </div>

      {/* Pagination */}
      {!isLoading && documents.length > 0 && (
        <div className="mt-4">
          <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
        </div>
      )}
    </div>
  );
}
