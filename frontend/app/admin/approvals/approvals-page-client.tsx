"use client";

/**
 * Approvals page with approve/reject actions and rejection reason dialog.
 * Task 22.5: Implement approvals page with approve/reject actions and rejection reason dialog.
 * - Create `/app/admin/approvals/page.tsx` listing pending approvals
 * - Approve button + reject button with rejection reason dialog (10-1000 chars)
 * Requirements: 22.6
 */

import { useCallback, useEffect, useState } from "react";
import Link from "next/link";
import { useSearchParams } from "next/navigation";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Skeleton } from "@/components/ui/skeleton";
import { api, type PageResponse } from "@/lib/api-client";

type ApprovalStatus = "PENDING" | "APPROVED" | "REJECTED" | "CANCELLED";

interface Approval {
  id: string;
  documentId: string;
  documentFilename: string;
  requestId: string;
  requestTitle: string;
  customerName: string;
  status: ApprovalStatus;
  analysisCategory: string;
  analysisConfidence: number;
  createdAt: string;
}

function getStatusBadgeClass(status: ApprovalStatus): string {
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

function formatConfidence(confidence: number): string {
  return `${Math.round(confidence * 100)}%`;
}

function RejectDialog({
  isOpen,
  onClose,
  onConfirm,
  isSubmitting,
}: {
  isOpen: boolean;
  onClose: () => void;
  onConfirm: (reason: string) => Promise<void>;
  isSubmitting: boolean;
}) {
  const [reason, setReason] = useState("");
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    if (reason.length < 10) {
      setError("Rejection reason must be at least 10 characters");
      return;
    }
    if (reason.length > 1000) {
      setError("Rejection reason must not exceed 1000 characters");
      return;
    }
    await onConfirm(reason);
    setReason("");
  };

  const handleClose = () => {
    setReason("");
    setError(null);
    onClose();
  };

  return (
    <Dialog open={isOpen} onOpenChange={handleClose}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Reject Approval</DialogTitle>
          <DialogDescription>
            Please provide a reason for rejecting this document. The reason will be shared with the customer.
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit}>
          <div className="space-y-4">
            <div className="space-y-2">
              <label htmlFor="rejection-reason" className="text-sm font-medium leading-none">
                Rejection Reason
              </label>
              <textarea
                id="rejection-reason"
                placeholder="Enter the reason for rejection (10-1000 characters)..."
                value={reason}
                onChange={(e) => setReason(e.target.value)}
                disabled={isSubmitting}
                rows={4}
                className="flex w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
                aria-invalid={!!error}
                aria-describedby={error ? "reason-error" : "reason-hint"}
              />
              <p id="reason-hint" className="text-xs text-muted-foreground">
                {reason.length}/1000 characters
              </p>
              {error && (
                <p id="reason-error" className="text-sm text-destructive">
                  {error}
                </p>
              )}
            </div>
          </div>
          <DialogFooter className="mt-6">
            <Button type="button" variant="outline" onClick={handleClose} disabled={isSubmitting}>
              Cancel
            </Button>
            <Button type="submit" variant="destructive" disabled={isSubmitting || reason.length < 10}>
              {isSubmitting ? "Rejecting..." : "Reject"}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
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
      <p className="text-sm text-muted-foreground">
        Page {page + 1} of {totalPages || 1}
      </p>
      <div className="flex gap-2">
        <Button variant="outline" size="sm" onClick={() => onPageChange(page - 1)} disabled={page === 0}>
          Previous
        </Button>
        <Button variant="outline" size="sm" onClick={() => onPageChange(page + 1)} disabled={page >= totalPages - 1}>
          Next
        </Button>
      </div>
    </div>
  );
}

export default function ApprovalsPageClient() {
  const searchParams = useSearchParams();
  const documentIdFilter = searchParams.get("documentId");

  const [approvals, setApprovals] = useState<Approval[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [statusFilter, setStatusFilter] = useState<string>(documentIdFilter ? "ALL" : "PENDING");
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [actionLoading, setActionLoading] = useState<string | null>(null);
  const [rejectDialogApproval, setRejectDialogApproval] = useState<Approval | null>(null);

  const fetchApprovals = useCallback(async () => {
    setIsLoading(true);
    setError(null);

    try {
      const params = new URLSearchParams({
        page: String(page),
        size: "20",
        sort: "createdAt,desc",
      });

      if (statusFilter !== "ALL") params.append("status", statusFilter);
      if (documentIdFilter) params.append("documentId", documentIdFilter);

      const response = await api.get<PageResponse<Approval>>(`/approvals?${params.toString()}`);
      setApprovals(response.content);
      setTotalPages(response.page.totalPages);
      setTotalElements(response.page.totalElements);
    } catch {
      setError("Failed to load approvals");
    } finally {
      setIsLoading(false);
    }
  }, [page, statusFilter, documentIdFilter]);

  useEffect(() => {
    fetchApprovals();
  }, [fetchApprovals]);

  const handleApprove = async (approvalId: string) => {
    setActionLoading(approvalId);
    setError(null);
    try {
      await api.post(`/approvals/${approvalId}/approve`);
      setApprovals((prev) =>
        prev.map((a) => (a.id === approvalId ? { ...a, status: "APPROVED" as ApprovalStatus } : a)),
      );
    } catch {
      setError("Failed to approve document");
    } finally {
      setActionLoading(null);
    }
  };

  const handleReject = async (reason: string) => {
    if (!rejectDialogApproval) return;
    setActionLoading(rejectDialogApproval.id);
    setError(null);
    try {
      await api.post(`/approvals/${rejectDialogApproval.id}/reject`, { reason });
      setApprovals((prev) =>
        prev.map((a) => (a.id === rejectDialogApproval.id ? { ...a, status: "REJECTED" as ApprovalStatus } : a)),
      );
      setRejectDialogApproval(null);
    } catch {
      setError("Failed to reject document");
    } finally {
      setActionLoading(null);
    }
  };

  return (
    <div className="p-6">
      <div className="mb-6">
        <h1 className="text-2xl font-bold">Approvals</h1>
        <p className="text-muted-foreground">
          Review and approve or reject analyzed documents. {totalElements > 0 && `(${totalElements} total)`}
        </p>
      </div>

      <div className="mb-4 flex flex-col gap-4 sm:flex-row">
        <select
          value={statusFilter}
          onChange={(e) => {
            setStatusFilter(e.target.value);
            setPage(0);
          }}
          className="rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background focus:outline-none focus:ring-2 focus:ring-ring"
          aria-label="Filter by status"
        >
          <option value="ALL">All Statuses</option>
          <option value="PENDING">Pending</option>
          <option value="APPROVED">Approved</option>
          <option value="REJECTED">Rejected</option>
          <option value="CANCELLED">Cancelled</option>
        </select>

        {documentIdFilter && (
          <div className="flex items-center gap-2 text-sm text-muted-foreground">
            <span>Filtered by document</span>
            <Link href="/admin/approvals" className="text-primary hover:underline">
              Clear filter
            </Link>
          </div>
        )}
      </div>

      {error && (
        <div className="mb-4 rounded-md bg-destructive/10 p-4 text-sm text-destructive" role="alert">
          {error}
        </div>
      )}

      <div className="hidden rounded-md border lg:block">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Document</TableHead>
              <TableHead>Request</TableHead>
              <TableHead>Customer</TableHead>
              <TableHead>Category</TableHead>
              <TableHead>Confidence</TableHead>
              <TableHead>Status</TableHead>
              <TableHead>Created</TableHead>
              <TableHead className="w-[180px]">Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {isLoading ? (
              Array.from({ length: 5 }).map((_, i) => (
                <TableRow key={i}>
                  <TableCell><Skeleton className="h-4 w-32" /></TableCell>
                  <TableCell><Skeleton className="h-4 w-40" /></TableCell>
                  <TableCell><Skeleton className="h-4 w-28" /></TableCell>
                  <TableCell><Skeleton className="h-4 w-20" /></TableCell>
                  <TableCell><Skeleton className="h-4 w-12" /></TableCell>
                  <TableCell><Skeleton className="h-6 w-20" /></TableCell>
                  <TableCell><Skeleton className="h-4 w-20" /></TableCell>
                  <TableCell>
                    <div className="flex gap-2">
                      <Skeleton className="h-8 w-16" />
                      <Skeleton className="h-8 w-16" />
                    </div>
                  </TableCell>
                </TableRow>
              ))
            ) : approvals.length === 0 ? (
              <TableRow>
                <TableCell colSpan={8} className="py-8 text-center">
                  <p className="text-muted-foreground">
                    {statusFilter === "PENDING" ? "No pending approvals" : "No approvals found"}
                  </p>
                </TableCell>
              </TableRow>
            ) : (
              approvals.map((approval) => (
                <TableRow key={approval.id}>
                  <TableCell className="font-medium">{approval.documentFilename}</TableCell>
                  <TableCell>{approval.requestTitle}</TableCell>
                  <TableCell>{approval.customerName}</TableCell>
                  <TableCell>
                    <Badge variant="outline">{approval.analysisCategory}</Badge>
                  </TableCell>
                  <TableCell>{formatConfidence(approval.analysisConfidence)}</TableCell>
                  <TableCell>
                    <Badge variant="outline" className={getStatusBadgeClass(approval.status)}>
                      {approval.status}
                    </Badge>
                  </TableCell>
                  <TableCell>{new Date(approval.createdAt).toLocaleString()}</TableCell>
                  <TableCell>
                    <div className="flex gap-2">
                      <Button
                        size="sm"
                        onClick={() => handleApprove(approval.id)}
                        disabled={actionLoading === approval.id || approval.status !== "PENDING"}
                      >
                        Approve
                      </Button>
                      <Button
                        size="sm"
                        variant="destructive"
                        onClick={() => setRejectDialogApproval(approval)}
                        disabled={actionLoading === approval.id || approval.status !== "PENDING"}
                      >
                        Reject
                      </Button>
                    </div>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>

      <div className="mt-6">
        <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
      </div>

      <RejectDialog
        isOpen={!!rejectDialogApproval}
        onClose={() => setRejectDialogApproval(null)}
        onConfirm={handleReject}
        isSubmitting={!!actionLoading}
      />
    </div>
  );
}
