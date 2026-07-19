/**
 * Requests list page with filters and analyst assignment.
 * Task 22.3: Implement requests list page with filters and analyst assignment.
 * - Create `/app/admin/requests/page.tsx` with paginated data table, status filter, priority filter
 * - Analyst assignment dropdown on request rows
 * Requirements: 22.4
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
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Skeleton } from "@/components/ui/skeleton";
import { api, type PageResponse } from "@/lib/api-client";

interface Request {
  id: string;
  title: string;
  status: "OPEN" | "IN_PROGRESS" | "RESOLVED" | "CLOSED";
  priority: "LOW" | "MEDIUM" | "HIGH" | "URGENT";
  customerId: string;
  customerName: string;
  assignedAnalystId: string | null;
  assignedAnalystName: string | null;
  createdAt: string;
}

interface Analyst {
  id: string;
  name: string;
}

function getStatusBadgeClass(status: Request["status"]): string {
  switch (status) {
    case "OPEN":
      return "bg-blue-100 text-blue-800 border-blue-200";
    case "IN_PROGRESS":
      return "bg-yellow-100 text-yellow-800 border-yellow-200";
    case "RESOLVED":
      return "bg-green-100 text-green-800 border-green-200";
    case "CLOSED":
      return "bg-gray-100 text-gray-800 border-gray-200";
    default:
      return "";
  }
}

function getPriorityBadgeClass(priority: Request["priority"]): string {
  switch (priority) {
    case "LOW":
      return "bg-slate-100 text-slate-800 border-slate-200";
    case "MEDIUM":
      return "bg-blue-100 text-blue-800 border-blue-200";
    case "HIGH":
      return "bg-orange-100 text-orange-800 border-orange-200";
    case "URGENT":
      return "bg-red-100 text-red-800 border-red-200";
    default:
      return "";
  }
}

function AnalystAssignment({
  request,
  analysts,
  onAssign,
}: {
  request: Request;
  analysts: Analyst[];
  onAssign: (requestId: string, analystId: string | null) => Promise<void>;
}) {
  const [isAssigning, setIsAssigning] = useState(false);

  const handleAssign = async (analystId: string | null) => {
    setIsAssigning(true);
    try {
      await onAssign(request.id, analystId);
    } finally {
      setIsAssigning(false);
    }
  };

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button
          variant="ghost"
          size="sm"
          disabled={isAssigning}
          className="h-auto py-1 px-2 text-left justify-start"
        >
          {isAssigning ? (
            "Assigning..."
          ) : request.assignedAnalystName ? (
            <span className="truncate max-w-[120px]">
              {request.assignedAnalystName}
            </span>
          ) : (
            <span className="text-muted-foreground">Unassigned</span>
          )}
          <svg
            className="ml-1 h-4 w-4 shrink-0"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
            aria-hidden="true"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M19 9l-7 7-7-7"
            />
          </svg>
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align="start">
        <DropdownMenuItem onClick={() => handleAssign(null)}>
          <span className="text-muted-foreground">Unassigned</span>
        </DropdownMenuItem>
        {analysts.map((analyst) => (
          <DropdownMenuItem
            key={analyst.id}
            onClick={() => handleAssign(analyst.id)}
          >
            {analyst.name}
          </DropdownMenuItem>
        ))}
      </DropdownMenuContent>
    </DropdownMenu>
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

export default function RequestsPage() {
  const [requests, setRequests] = useState<Request[]>([]);
  const [analysts, setAnalysts] = useState<Analyst[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState<string>("ALL");
  const [priorityFilter, setPriorityFilter] = useState<string>("ALL");
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  const fetchRequests = useCallback(async () => {
    setIsLoading(true);
    setError(null);

    try {
      const params = new URLSearchParams({
        page: String(page),
        size: "20",
        sort: "createdAt,desc",
      });

      if (search.trim()) {
        params.append("search", search.trim());
      }

      if (statusFilter !== "ALL") {
        params.append("status", statusFilter);
      }

      if (priorityFilter !== "ALL") {
        params.append("priority", priorityFilter);
      }

      const response = await api.get<PageResponse<Request>>(
        `/requests?${params.toString()}`,
      );

      setRequests(response.content);
      setTotalPages(response.page.totalPages);
      setTotalElements(response.page.totalElements);
    } catch {
      setError("Failed to load requests");
    } finally {
      setIsLoading(false);
    }
  }, [page, search, statusFilter, priorityFilter]);

  // Fetch analysts for assignment dropdown
  useEffect(() => {
    async function fetchAnalysts() {
      try {
        const response = await api.get<Analyst[]>("/users/analysts");
        setAnalysts(response);
      } catch {
        // Non-critical error, just log it
        console.error("Failed to load analysts");
      }
    }

    fetchAnalysts();
  }, []);

  useEffect(() => {
    fetchRequests();
  }, [fetchRequests]);

  // Debounced search
  useEffect(() => {
    const timer = setTimeout(() => {
      setPage(0);
    }, 300);
    return () => clearTimeout(timer);
  }, [search]);

  const handleAssignAnalyst = async (
    requestId: string,
    analystId: string | null,
  ) => {
    try {
      await api.patch(`/requests/${requestId}/assign`, { analystId });
      // Update local state
      setRequests((prev) =>
        prev.map((req) => {
          if (req.id === requestId) {
            const analyst = analysts.find((a) => a.id === analystId);
            return {
              ...req,
              assignedAnalystId: analystId,
              assignedAnalystName: analyst?.name ?? null,
            };
          }
          return req;
        }),
      );
    } catch {
      setError("Failed to assign analyst");
    }
  };

  return (
    <div className="p-6">
      <div className="mb-6">
        <h1 className="text-2xl font-bold">Requests</h1>
        <p className="text-muted-foreground">
          Manage service requests and their workflows.{" "}
          {totalElements > 0 && `(${totalElements} total)`}
        </p>
      </div>

      {/* Filters */}
      <div className="mb-4 flex flex-col gap-4 sm:flex-row">
        <div className="relative flex-1 max-w-md">
          <svg
            className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground"
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
            placeholder="Search requests..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="pl-10"
            aria-label="Search requests"
          />
        </div>

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
          <option value="OPEN">Open</option>
          <option value="IN_PROGRESS">In Progress</option>
          <option value="RESOLVED">Resolved</option>
          <option value="CLOSED">Closed</option>
        </select>

        <select
          value={priorityFilter}
          onChange={(e) => {
            setPriorityFilter(e.target.value);
            setPage(0);
          }}
          className="rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background focus:outline-none focus:ring-2 focus:ring-ring"
          aria-label="Filter by priority"
        >
          <option value="ALL">All Priorities</option>
          <option value="LOW">Low</option>
          <option value="MEDIUM">Medium</option>
          <option value="HIGH">High</option>
          <option value="URGENT">Urgent</option>
        </select>
      </div>

      {error && (
        <div
          className="mb-4 rounded-md bg-destructive/10 p-4 text-sm text-destructive"
          role="alert"
        >
          {error}
        </div>
      )}

      {/* Table for desktop */}
      <div className="hidden lg:block rounded-md border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Title</TableHead>
              <TableHead>Customer</TableHead>
              <TableHead>Status</TableHead>
              <TableHead>Priority</TableHead>
              <TableHead>Assigned To</TableHead>
              <TableHead>Created</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {isLoading ? (
              Array.from({ length: 5 }).map((_, i) => (
                <TableRow key={i}>
                  <TableCell>
                    <Skeleton className="h-4 w-48" />
                  </TableCell>
                  <TableCell>
                    <Skeleton className="h-4 w-32" />
                  </TableCell>
                  <TableCell>
                    <Skeleton className="h-6 w-20" />
                  </TableCell>
                  <TableCell>
                    <Skeleton className="h-6 w-16" />
                  </TableCell>
                  <TableCell>
                    <Skeleton className="h-4 w-24" />
                  </TableCell>
                  <TableCell>
                    <Skeleton className="h-4 w-20" />
                  </TableCell>
                </TableRow>
              ))
            ) : requests.length === 0 ? (
              <TableRow>
                <TableCell colSpan={6} className="text-center py-8">
                  <p className="text-muted-foreground">No requests found</p>
                </TableCell>
              </TableRow>
            ) : (
              requests.map((request) => (
                <TableRow key={request.id}>
                  <TableCell>
                    <Link
                      href={`/admin/requests/${request.id}`}
                      className="font-medium hover:underline"
                    >
                      {request.title}
                    </Link>
                  </TableCell>
                  <TableCell className="text-muted-foreground">
                    {request.customerName}
                  </TableCell>
                  <TableCell>
                    <Badge className={getStatusBadgeClass(request.status)}>
                      {request.status.replace("_", " ")}
                    </Badge>
                  </TableCell>
                  <TableCell>
                    <Badge className={getPriorityBadgeClass(request.priority)}>
                      {request.priority}
                    </Badge>
                  </TableCell>
                  <TableCell>
                    <AnalystAssignment
                      request={request}
                      analysts={analysts}
                      onAssign={handleAssignAnalyst}
                    />
                  </TableCell>
                  <TableCell className="text-muted-foreground">
                    {new Date(request.createdAt).toLocaleDateString()}
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>

      {/* Cards for mobile/tablet */}
      <div className="lg:hidden space-y-4">
        {isLoading ? (
          Array.from({ length: 3 }).map((_, i) => (
            <div key={i} className="rounded-lg border p-4 space-y-3">
              <Skeleton className="h-5 w-48" />
              <Skeleton className="h-4 w-32" />
              <div className="flex gap-2">
                <Skeleton className="h-6 w-20" />
                <Skeleton className="h-6 w-16" />
              </div>
              <div className="flex justify-between">
                <Skeleton className="h-4 w-28" />
                <Skeleton className="h-4 w-20" />
              </div>
            </div>
          ))
        ) : requests.length === 0 ? (
          <div className="text-center py-8">
            <p className="text-muted-foreground">No requests found</p>
          </div>
        ) : (
          requests.map((request) => (
            <div key={request.id} className="rounded-lg border p-4 space-y-3">
              <Link
                href={`/admin/requests/${request.id}`}
                className="font-medium hover:underline block"
              >
                {request.title}
              </Link>
              <p className="text-sm text-muted-foreground">
                {request.customerName}
              </p>
              <div className="flex gap-2">
                <Badge className={getStatusBadgeClass(request.status)}>
                  {request.status.replace("_", " ")}
                </Badge>
                <Badge className={getPriorityBadgeClass(request.priority)}>
                  {request.priority}
                </Badge>
              </div>
              <div className="flex items-center justify-between pt-2 border-t">
                <div className="flex items-center gap-2">
                  <span className="text-sm text-muted-foreground">
                    Analyst:
                  </span>
                  <AnalystAssignment
                    request={request}
                    analysts={analysts}
                    onAssign={handleAssignAnalyst}
                  />
                </div>
                <span className="text-sm text-muted-foreground">
                  {new Date(request.createdAt).toLocaleDateString()}
                </span>
              </div>
            </div>
          ))
        )}
      </div>

      {/* Pagination */}
      {!isLoading && requests.length > 0 && (
        <div className="mt-4">
          <Pagination
            page={page}
            totalPages={totalPages}
            onPageChange={setPage}
          />
        </div>
      )}
    </div>
  );
}
