export const CONTRACT_ENDPOINTS = [
  "/auth/login",
  "/auth/refresh",
  "/auth/logout",
  "/tenants",
  "/customers",
  "/requests",
  "/documents",
  "/approvals",
  "/analytics/dashboard",
  "/events/stream",
  "/integrations/webhooks/dispatch",
] as const;

export type ContractEndpoint = (typeof CONTRACT_ENDPOINTS)[number];

export const CONTRACT_ENDPOINT_GROUPS = {
  auth: ["/auth/login", "/auth/refresh", "/auth/logout"],
  core: ["/tenants", "/customers", "/requests", "/documents", "/approvals"],
  realtime: ["/analytics/dashboard", "/events/stream"],
  integrations: ["/integrations/webhooks/dispatch"],
} as const;
