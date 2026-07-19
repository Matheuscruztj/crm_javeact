/**
 * Activities page with global feed and infinite scroll.
 * Task 22.7: Implement activities page with global feed and infinite scroll.
 * - Create `/app/admin/activities/page.tsx` showing global activity feed
 * - Infinite scroll pagination
 * Requirements: 22.8
 */

"use client";

import { useCallback, useEffect, useRef, useState } from "react";
import Link from "next/link";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import { api, type PageResponse } from "@/lib/api-client";

type ActivityType =
  | "CUSTOMER_CREATED"
  | "CUSTOMER_UPDATED"
  | "REQUEST_CREATED"
  | "REQUEST_STATUS_CHANGED"
  | "DOCUMENT_UPLOADED"
  | "DOCUMENT_ANALYZED"
  | "APPROVAL_DECIDED"
  | "USER_LOGIN"
  | "COMMENT_ADDED";

interface Activity {
  id: string;
  eventId: string;
  actorId: string;
  actorName: string;
  activityType: ActivityType;
  entityType: string;
  entityId: string;
  entityName: string;
  description: string;
  metadata: Record<string, unknown>;
  occurredAt: string;
}

function getActivityIcon(activityType: ActivityType): React.ReactNode {
  switch (activityType) {
    case "CUSTOMER_CREATED":
    case "CUSTOMER_UPDATED":
      return (
        <div className="flex h-8 w-8 items-center justify-center rounded-full bg-blue-100">
          <svg
            className="h-4 w-4 text-blue-600"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"
            />
          </svg>
        </div>
      );
    case "REQUEST_CREATED":
    case "REQUEST_STATUS_CHANGED":
      return (
        <div className="flex h-8 w-8 items-center justify-center rounded-full bg-purple-100">
          <svg
            className="h-4 w-4 text-purple-600"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"
            />
          </svg>
        </div>
      );
    case "DOCUMENT_UPLOADED":
    case "DOCUMENT_ANALYZED":
      return (
        <div className="flex h-8 w-8 items-center justify-center rounded-full bg-green-100">
          <svg
            className="h-4 w-4 text-green-600"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
            />
          </svg>
        </div>
      );
    case "APPROVAL_DECIDED":
      return (
        <div className="flex h-8 w-8 items-center justify-center rounded-full bg-yellow-100">
          <svg
            className="h-4 w-4 text-yellow-600"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
            />
          </svg>
        </div>
      );
    case "USER_LOGIN":
      return (
        <div className="flex h-8 w-8 items-center justify-center rounded-full bg-gray-100">
          <svg
            className="h-4 w-4 text-gray-600"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M11 16l-4-4m0 0l4-4m-4 4h14m-5 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h7a3 3 0 013 3v1"
            />
          </svg>
        </div>
      );
    case "COMMENT_ADDED":
      return (
        <div className="flex h-8 w-8 items-center justify-center rounded-full bg-indigo-100">
          <svg
            className="h-4 w-4 text-indigo-600"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"
            />
          </svg>
        </div>
      );
    default:
      return (
        <div className="flex h-8 w-8 items-center justify-center rounded-full bg-gray-100">
          <svg
            className="h-4 w-4 text-gray-600"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
            />
          </svg>
        </div>
      );
  }
}

function getEntityLink(entityType: string, entityId: string): string | null {
  switch (entityType.toLowerCase()) {
    case "customer":
      return `/admin/customers/${entityId}`;
    case "request":
      return `/admin/requests/${entityId}`;
    case "document":
      return `/admin/documents/${entityId}`;
    case "approval":
      return `/admin/approvals?documentId=${entityId}`;
    default:
      return null;
  }
}

function formatRelativeTime(dateString: string): string {
  const date = new Date(dateString);
  const now = new Date();
  const diffInSeconds = Math.floor((now.getTime() - date.getTime()) / 1000);

  if (diffInSeconds < 60) {
    return "just now";
  }

  const diffInMinutes = Math.floor(diffInSeconds / 60);
  if (diffInMinutes < 60) {
    return `${diffInMinutes}m ago`;
  }

  const diffInHours = Math.floor(diffInMinutes / 60);
  if (diffInHours < 24) {
    return `${diffInHours}h ago`;
  }

  const diffInDays = Math.floor(diffInHours / 24);
  if (diffInDays < 7) {
    return `${diffInDays}d ago`;
  }

  return date.toLocaleDateString();
}

function ActivityItem({ activity }: { activity: Activity }) {
  const entityLink = getEntityLink(activity.entityType, activity.entityId);

  return (
    <div className="flex gap-3 p-4 hover:bg-muted/50 transition-colors">
      {getActivityIcon(activity.activityType)}
      <div className="flex-1 min-w-0">
        <div className="flex items-start justify-between gap-2">
          <p className="text-sm">
            <span className="font-medium">{activity.actorName}</span>{" "}
            <span className="text-muted-foreground">
              {activity.description}
            </span>
          </p>
          <span className="text-xs text-muted-foreground whitespace-nowrap">
            {formatRelativeTime(activity.occurredAt)}
          </span>
        </div>
        {entityLink ? (
          <Link
            href={entityLink}
            className="text-xs text-primary hover:underline"
          >
            {activity.entityType}: {activity.entityName || activity.entityId}
          </Link>
        ) : (
          <p className="text-xs text-muted-foreground">
            {activity.entityType}: {activity.entityName || activity.entityId}
          </p>
        )}
      </div>
    </div>
  );
}

function ActivitySkeleton() {
  return (
    <div className="flex gap-3 p-4">
      <Skeleton className="h-8 w-8 rounded-full shrink-0" />
      <div className="flex-1 space-y-2">
        <Skeleton className="h-4 w-3/4" />
        <Skeleton className="h-3 w-1/2" />
      </div>
    </div>
  );
}

export default function ActivitiesPage() {
  const [activities, setActivities] = useState<Activity[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isLoadingMore, setIsLoadingMore] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [entityTypeFilter, setEntityTypeFilter] = useState<string>("ALL");

  const observerTarget = useRef<HTMLDivElement>(null);

  const fetchActivities = useCallback(
    async (pageNum: number, append: boolean = false) => {
      if (append) {
        setIsLoadingMore(true);
      } else {
        setIsLoading(true);
      }
      setError(null);

      try {
        const params = new URLSearchParams({
          page: String(pageNum),
          size: "20",
          sort: "occurredAt,desc",
        });

        if (entityTypeFilter !== "ALL") {
          params.append("entityType", entityTypeFilter);
        }

        const response = await api.get<PageResponse<Activity>>(
          `/activities?${params.toString()}`,
        );

        if (append) {
          setActivities((prev) => [...prev, ...response.content]);
        } else {
          setActivities(response.content);
        }

        setHasMore(pageNum < response.page.totalPages - 1);
      } catch {
        setError("Failed to load activities");
      } finally {
        setIsLoading(false);
        setIsLoadingMore(false);
      }
    },
    [entityTypeFilter],
  );

  // Initial load and filter changes
  useEffect(() => {
    setPage(0);
    setActivities([]);
    setHasMore(true);
    fetchActivities(0, false);
  }, [entityTypeFilter, fetchActivities]);

  // Infinite scroll
  useEffect(() => {
    const observer = new IntersectionObserver(
      (entries) => {
        if (
          entries[0].isIntersecting &&
          hasMore &&
          !isLoading &&
          !isLoadingMore
        ) {
          const nextPage = page + 1;
          setPage(nextPage);
          fetchActivities(nextPage, true);
        }
      },
      { threshold: 0.1 },
    );

    const currentTarget = observerTarget.current;
    if (currentTarget) {
      observer.observe(currentTarget);
    }

    return () => {
      if (currentTarget) {
        observer.unobserve(currentTarget);
      }
    };
  }, [hasMore, isLoading, isLoadingMore, page, fetchActivities]);

  return (
    <div className="p-6">
      <div className="mb-6">
        <h1 className="text-2xl font-bold">Activity Feed</h1>
        <p className="text-muted-foreground">
          Recent activity across the system.
        </p>
      </div>

      {/* Filters */}
      <div className="mb-4">
        <select
          value={entityTypeFilter}
          onChange={(e) => setEntityTypeFilter(e.target.value)}
          className="rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background focus:outline-none focus:ring-2 focus:ring-ring"
          aria-label="Filter by entity type"
        >
          <option value="ALL">All Activities</option>
          <option value="Customer">Customers</option>
          <option value="Request">Requests</option>
          <option value="Document">Documents</option>
          <option value="Approval">Approvals</option>
          <option value="User">Users</option>
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

      {/* Activity Feed */}
      <div className="rounded-md border divide-y">
        {isLoading ? (
          Array.from({ length: 10 }).map((_, i) => <ActivitySkeleton key={i} />)
        ) : activities.length === 0 ? (
          <div className="text-center py-12">
            <svg
              className="mx-auto h-12 w-12 text-muted-foreground"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={1}
                d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"
              />
            </svg>
            <p className="mt-4 text-muted-foreground">No activities found</p>
          </div>
        ) : (
          <>
            {activities.map((activity) => (
              <ActivityItem key={activity.id} activity={activity} />
            ))}

            {/* Loading more indicator */}
            {isLoadingMore && (
              <>
                <ActivitySkeleton />
                <ActivitySkeleton />
                <ActivitySkeleton />
              </>
            )}
          </>
        )}
      </div>

      {/* Infinite scroll trigger */}
      <div ref={observerTarget} className="h-10" aria-hidden="true" />

      {/* End of feed indicator */}
      {!isLoading && !hasMore && activities.length > 0 && (
        <p className="text-center text-sm text-muted-foreground py-4">
          You&apos;ve reached the end of the activity feed
        </p>
      )}
    </div>
  );
}
