import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "AtlasOps AI",
  description: "Intelligent CRM with AI-powered document analysis",
};

/**
 * Root layout required by Next.js App Router.
 * All routes inherit this layout. Section-specific layouts
 * (admin, portal) layer their own chrome on top of this.
 */
export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en" suppressHydrationWarning>
      <body className="bg-background min-h-screen font-sans antialiased">{children}</body>
    </html>
  );
}
