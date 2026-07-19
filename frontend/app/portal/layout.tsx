"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { cn } from "@/lib/utils";
import { Separator } from "@/components/ui/separator";

const portalNavItems = [
  { href: "/portal/home", label: "Início", icon: "🏠" },
  { href: "/portal/requests", label: "Solicitações", icon: "📋" },
  { href: "/portal/documents", label: "Documentos", icon: "📄" },
  { href: "/portal/notifications", label: "Notificações", icon: "🔔" },
];

export default function PortalLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const pathname = usePathname();

  return (
    <div className="min-h-screen bg-background">
      {/* Desktop sidebar - hidden below lg */}
      <aside className="fixed inset-y-0 left-0 z-30 hidden w-64 border-r bg-card lg:block">
        <div className="flex h-14 items-center border-b px-4">
          <span className="text-lg font-semibold">AtlasOps Portal</span>
        </div>
        <nav className="flex flex-col gap-1 p-4">
          {portalNavItems.map((item) => (
            <Link
              key={item.href}
              href={item.href}
              className={cn(
                "flex items-center gap-3 rounded-md px-3 py-2 text-sm font-medium transition-colors hover:bg-accent hover:text-accent-foreground",
                pathname.startsWith(item.href)
                  ? "bg-accent text-accent-foreground"
                  : "text-muted-foreground",
              )}
            >
              <span>{item.icon}</span>
              {item.label}
            </Link>
          ))}
        </nav>
        <Separator className="mx-4" />
        <div className="p-4">
          <Link
            href="/portal/documents/upload"
            className="flex w-full items-center justify-center rounded-md bg-primary px-4 py-2 text-sm font-medium text-primary-foreground hover:bg-primary/90"
          >
            Enviar Documento
          </Link>
        </div>
      </aside>

      {/* Main content area */}
      <div className="lg:pl-64">
        {/* Mobile header */}
        <header className="sticky top-0 z-20 flex h-14 items-center border-b bg-card px-4 lg:hidden">
          <span className="text-lg font-semibold">AtlasOps Portal</span>
        </header>

        {/* Page content */}
        <main className="pb-20 lg:pb-0">{children}</main>
      </div>

      {/* Mobile bottom navigation - visible below lg */}
      <nav className="fixed inset-x-0 bottom-0 z-30 border-t bg-card lg:hidden">
        <div className="flex items-center justify-around py-2">
          {portalNavItems.map((item) => (
            <Link
              key={item.href}
              href={item.href}
              className={cn(
                "flex flex-col items-center gap-1 px-3 py-1 text-xs font-medium transition-colors",
                pathname.startsWith(item.href)
                  ? "text-primary"
                  : "text-muted-foreground",
              )}
            >
              <span className="text-lg">{item.icon}</span>
              {item.label}
            </Link>
          ))}
        </div>
      </nav>
    </div>
  );
}
