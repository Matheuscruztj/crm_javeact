/**
 * Audit page with filterable log table.
 * Task 22.6: Implement audit page with filterable log table.
 * - Create `/app/admin/audit/page.tsx` with action type filter, date range picker, actor filter
 * - Paginated data table
 * Requirements: 22.7
 */

"use client";

import { useCallback, useEffect, useState } from "react";
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

type ActionType =
  | "CREATE"
  | "UPDATE"
  | "DELETE"
  | "LOGIN"
  | "LOGOUT"
  | "APPROVE"
  | "REJECT"
  | "UPLOAD"
  | "DOWNLOAD";

interface AuditEntry {
  id: string;
  actorId: string;
  actorName: string;
  actorRole: string;
  actionType: ActionType;
  entityType: string;
  entityId: string;
  description: string;
  ipAddress: string;
  timestamp: string;
}

function getActionTypeBadgeClass(actionType: ActionType): string {
  switch (actionType) {
    case "CREATE":
      return "bg-green-100 text-green-800 border-green-200";
    case "UPDATE":
      return "bg-blue-100 text-blue-800 border-blue-200";
    case "DELETE":
      return "bg-red-100 text-red-800 border-red-200";
    case "LOGIN":
    case "LOGOUT":
      return "bg-purple-100 text-purple-800 border-purple-200";
    case "APPROVE":
      return "bg-emerald-100 text-emerald-800 border-emerald-200";
    case "REJECT":
      return "bg-orange-100 text-orange-800 border-orange-200";
    case "UPLOAD":
    case "DOWNLOAD":
      return "bg-cyan-100 text-cyan-800 border-cyan-200";
    default:
      return "bg-gray-100 text-gray-800 border-gray-200";
  }
}

function formatTimestamp(timestamp: string): string {
  const date = new Date(timestamp);
  return date.toLocaleString();
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

export default function AuditPage() {
  const [entries, setEntries] = useState<AuditEntry[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [actionTypeFilter, setActionTypeFilter] = useState<string>("ALL");
  const [actorFilter, setActorFilter] = useState("");
  const [fromDate, setFromDate] = useState("");
  const [toDate, setToDate] = useState("");
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  const fetchAuditEntries = useCallback(async () => {
    setIsLoading(true);
    setError(null);

    try {
      const params = new URLSearchParams({
        page: String(page),
        size: "20",
        sort: "timestamp,desc",
      });

      if (actionTypeFilter !== "ALL") {
        params.append("actionType", actionTypeFilter);
      }

      if (actorFilter.trim()) {
        params.append("actorId", actorFilter.trim());
      }

      if (fromDate) {
        params.append("from", new Date(fromDate).toISOString());
      }

      if (toDate) {
        // Set to end of day
        const endDate = new Date(toDate);
        endDate.setHours(23, 59, 59, 999);
        params.append("to", endDate.toISOString());
      }

      const response = await api.get<PageResponse<AuditEntry>>(`/audit?${params.toString()}`);

      setEntries(response.content);
      setTotalPages(response.page.totalPages);
      setTotalElements(response.page.totalElements);
    } catch {
      setError("Failed to load audit entries");
    } finally {
      setIsLoading(false);
    }
  }, [page, actionTypeFilter, actorFilter, fromDate, toDate]);

  useEffect(() => {
    fetchAuditEntries();
  }, [fetchAuditEntries]);

  const handleClearFilters = () => {
    setActionTypeFilter("ALL");
    setActorFilter("");
    setFromDate("");
    setToDate("");
    setPage(0);
  };

  const hasFilters =
    actionTypeFilter !== "ALL" || actorFilter.trim() !== "" || fromDate !== "" || toDate !== "";

  return (
    <div className="p-6">
      <div className="mb-6">
        <h1 className="text-2xl font-bold">Audit Log</h1>
        <p className="text-muted-foreground">
          View system activity and audit trail. {totalElements > 0 && `(${totalElements} total)`}
        </p>
      </div>

      {/* Filters */}
      <div className="mb-4 space-y-4">
        <div className="flex flex-col gap-4 sm:flex-row sm:flex-wrap">
          <select
            value={actionTypeFilter}
            onChange={(e) => {
              setActionTypeFilter(e.target.value);
              setPage(0);
            }}
            className="border-input bg-background ring-offset-background focus:ring-ring rounded-md border px-3 py-2 text-sm focus:ring-2 focus:outline-none"
            aria-label="Filter by action type"
          >
            <option value="ALL">All Actions</option>
            <option value="CREATE">Create</option>
            <option value="UPDATE">Update</option>
            <option value="DELETE">Delete</option>
            <option value="LOGIN">Login</option>
            <option value="LOGOUT">Logout</option>
            <option value="APPROVE">Approve</option>
            <option value="REJECT">Reject</option>
            <option value="UPLOAD">Upload</option>
            <option value="DOWNLOAD">Download</option>
          </select>

          <Input
            type="text"
            placeholder="Filter by actor ID..."
            value={actorFilter}
            onChange={(e) => {
              setActorFilter(e.target.value);
              setPage(0);
            }}
            className="max-w-[200px]"
            aria-label="Filter by actor ID"
          />

          <div className="flex items-center gap-2">
            <label htmlFor="from-date" className="text-muted-foreground text-sm">
              From:
            </label>
            <Input
              id="from-date"
              type="date"
              value={fromDate}
              onChange={(e) => {
                setFromDate(e.target.value);
                setPage(0);
              }}
              className="w-auto"
            />
          </div>

          <div className="flex items-center gap-2">
            <label htmlFor="to-date" className="text-muted-foreground text-sm">
              To:
            </label>
            <Input
              id="to-date"
              type="date"
              value={toDate}
              onChange={(e) => {
                setToDate(e.target.value);
                setPage(0);
              }}
              className="w-auto"
            />
          </div>

          {hasFilters && (
            <Button variant="ghost" size="sm" onClick={handleClearFilters}>
              Clear filters
            </Button>
          )}
        </div>
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
              <TableHead>Timestamp</TableHead>
              <TableHead>Actor</TableHead>
              <TableHead>Action</TableHead>
              <TableHead>Entity</TableHead>
              <TableHead>Description</TableHead>
              <TableHead>IP Address</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {isLoading ? (
              Array.from({ length: 5 }).map((_, i) => (
                <TableRow key={i}>
                  <TableCell>
                    <Skeleton className="h-4 w-32" />
                  </TableCell>
                  <TableCell>
                    <Skeleton className="h-4 w-24" />
                  </TableCell>
                  <TableCell>
                    <Skeleton className="h-6 w-20" />
                  </TableCell>
                  <TableCell>
                    <Skeleton className="h-4 w-32" />
                  </TableCell>
                  <TableCell>
                    <Skeleton className="h-4 w-48" />
                  </TableCell>
                  <TableCell>
                    <Skeleton className="h-4 w-24" />
                  </TableCell>
                </TableRow>
              ))
            ) : entries.length === 0 ? (
              <TableRow>
                <TableCell colSpan={6} className="py-8 text-center">
                  <p className="text-muted-foreground">No audit entries found</p>
                </TableCell>
              </TableRow>
            ) : (
              entries.map((entry) => (
                <TableRow key={entry.id}>
                  <TableCell className="font-mono text-sm">
                    {formatTimestamp(entry.timestamp)}
                  </TableCell>
                  <TableCell>
                    <div>
                      <p className="font-medium">{entry.actorName}</p>
                      <p className="text-muted-foreground text-xs">{entry.actorRole}</p>
                    </div>
                  </TableCell>
                  <TableCell>
                    <Badge className={getActionTypeBadgeClass(entry.actionType)}>
                      {entry.actionType}
                    </Badge>
                  </TableCell>
                  <TableCell>
                    <div>
                      <p className="font-medium">{entry.entityType}</p>
                      <p className="text-muted-foreground max-w-[150px] truncate text-xs">
                        {entry.entityId}
                      </p>
                    </div>
                  </TableCell>
                  <TableCell className="max-w-[250px] truncate" title={entry.description}>
                    {entry.description}
                  </TableCell>
                  <TableCell className="text-muted-foreground font-mono text-sm">
                    {entry.ipAddress}
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
              <div className="flex justify-between">
                <Skeleton className="h-6 w-20" />
                <Skeleton className="h-4 w-32" />
              </div>
              <Skeleton className="h-4 w-40" />
              <Skeleton className="h-4 w-full" />
            </div>
          ))
        ) : entries.length === 0 ? (
          <div className="py-8 text-center">
            <p className="text-muted-foreground">No audit entries found</p>
          </div>
        ) : (
          entries.map((entry) => (
            <div key={entry.id} className="space-y-3 rounded-lg border p-4">
              <div className="flex items-center justify-between">
                <Badge className={getActionTypeBadgeClass(entry.actionType)}>
                  {entry.actionType}
                </Badge>
                <span className="text-muted-foreground font-mono text-sm">
                  {formatTimestamp(entry.timestamp)}
                </span>
              </div>
              <div>
                <p className="font-medium">
                  {entry.actorName}{" "}
                  <span className="text-muted-foreground font-normal">({entry.actorRole})</span>
                </p>
                <p className="text-muted-foreground text-sm">
                  {entry.entityType}: {entry.entityId}
                </p>
              </div>
              <p className="text-sm">{entry.description}</p>
              <p className="text-muted-foreground font-mono text-xs">IP: {entry.ipAddress}</p>
            </div>
          ))
        )}
      </div>

      {/* Pagination */}
      {!isLoading && entries.length > 0 && (
        <div className="mt-4">
          <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
        </div>
      )}
    </div>
  );
}
