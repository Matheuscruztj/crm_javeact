import { describe, expect, it } from "vitest";

import { CONTRACT_ENDPOINTS, CONTRACT_ENDPOINT_GROUPS } from "./api-contract";

describe("api-contract", () => {
  it("lists the critical frontend-visible contract endpoints without duplicates", () => {
    expect(new Set(CONTRACT_ENDPOINTS).size).toBe(CONTRACT_ENDPOINTS.length);
    expect(CONTRACT_ENDPOINTS).toContain("/auth/login");
    expect(CONTRACT_ENDPOINTS).toContain("/customers");
    expect(CONTRACT_ENDPOINTS).toContain("/documents");
    expect(CONTRACT_ENDPOINTS).toContain("/events/stream");
  });

  it("groups endpoints by frontend usage surface", () => {
    expect(CONTRACT_ENDPOINT_GROUPS.auth).toEqual(["/auth/login", "/auth/refresh", "/auth/logout"]);
    expect(CONTRACT_ENDPOINT_GROUPS.core).toContain("/requests");
    expect(CONTRACT_ENDPOINT_GROUPS.realtime).toContain("/analytics/dashboard");
  });
});
