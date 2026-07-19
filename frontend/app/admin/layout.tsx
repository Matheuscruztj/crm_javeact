"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useState } from "react";
import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";

const navItems = [
  { label: "Dashboard", href: "/admin/dashboard" },
  { label: "Customers", href: "/admin/customers" },
  { label: "Requests", href: "/admin/requests" },
  { label: "Documents", href: "/admin/documents" },
  { label: "Approvals", href: "/admin/approvals" },
  { label: "Search", href: "/admin/search" },
  { label: "Activities", href: "/admin/activities" },
  { label: "Operations", href: "/admin/operations" },
  { label: "Integrations", href: "/admin/integrations" },
  { label: "Imports", href: "/admin/imports" },
  { label: "Audit", href: "/admin/audit" },
  { label: "Settings", href: "/admin/settings" },
];

function Sidebar({
  className,
  onNavigate,
}: {
  className?: string;
  onNavigate?: () => void;
}) {
  const pathname = usePathname();

  return (
    <aside className={cn("flex h-full flex-col bg-card border-r", className)}>
      <div className="flex h-14 items-center px-4 font-semibold text-lg">
        AtlasOps Admin
      </div>
      <Separator />
      <nav className="flex-1 overflow-y-auto p-2" aria-label="Admin navigation">
        <ul className="space-y-1">
          {navItems.map((item) => (
            <li key={item.href}>
              <Link
                href={item.href}
                onClick={onNavigate}
                className={cn(
                  "block rounded-md px-3 py-2 text-sm font-medium transition-colors",
                  "hover:bg-accent hover:text-accent-foreground",
                  pathname?.startsWith(item.href)
                    ? "bg-accent text-accent-foreground"
                    : "text-muted-foreground",
                )}
              >
                {item.label}
              </Link>
            </li>
          ))}
        </ul>
      </nav>
    </aside>
  );
}

function Breadcrumbs() {
  const pathname = usePathname();
  const segments = pathname?.split("/").filter(Boolean) ?? [];

  return (
    <nav aria-label="Breadcrumb" className="text-sm text-muted-foreground">
      <ol className="flex items-center gap-1.5">
        {segments.map((segment, index) => {
          const href = "/" + segments.slice(0, index + 1).join("/");
          const isLast = index === segments.length - 1;
          const label =
            segment.charAt(0).toUpperCase() +
            segment.slice(1).replace(/[-_]/g, " ");

          return (
            <li key={href} className="flex items-center gap-1.5">
              {index > 0 && <span aria-hidden="true">/</span>}
              {isLast ? (
                <span className="font-medium text-foreground">{label}</span>
              ) : (
                <Link
                  href={href}
                  className="hover:text-foreground transition-colors"
                >
                  {label}
                </Link>
              )}
            </li>
          );
        })}
      </ol>
    </nav>
  );
}

export default function AdminLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const [sidebarOpen, setSidebarOpen] = useState(false);

  return (
    <div className="flex h-screen overflow-hidden">
      {/* Desktop sidebar: persistent ≥1024px */}
      <Sidebar className="hidden lg:flex w-64 shrink-0" />

      {/* Tablet sidebar: collapsible 768–1023px */}
      <div className="hidden md:flex lg:hidden">
        {sidebarOpen && (
          <Sidebar
            className="w-64 shrink-0"
            onNavigate={() => setSidebarOpen(false)}
          />
        )}
      </div>

      {/* Mobile overlay: <768px */}
      {sidebarOpen && (
        <div className="fixed inset-0 z-50 md:hidden">
          <div
            className="absolute inset-0 bg-black/50"
            onClick={() => setSidebarOpen(false)}
            aria-hidden="true"
          />
          <Sidebar
            className="relative z-10 w-64 h-full"
            onNavigate={() => setSidebarOpen(false)}
          />
        </div>
      )}

      {/* Main content */}
      <div className="flex flex-1 flex-col overflow-hidden">
        {/* Header */}
        <header className="flex h-14 items-center gap-4 border-b bg-card px-4 lg:px-6">
          <Button
            variant="ghost"
            size="sm"
            className="lg:hidden"
            onClick={() => setSidebarOpen(!sidebarOpen)}
            aria-label={sidebarOpen ? "Close navigation" : "Open navigation"}
          >
            <svg
              className="h-5 w-5"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
              aria-hidden="true"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M4 6h16M4 12h16M4 18h16"
              />
            </svg>
          </Button>
          <Breadcrumbs />
        </header>

        {/* Page content */}
        <main className="flex-1 overflow-y-auto">{children}</main>
      </div>
    </div>
  );
}
