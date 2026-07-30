import { describe, expect, it, vi } from "vitest";
import { getApiErrorMessage, isApiErrorCode, mapApiErrorsToForm } from "./form-utils";

describe("form-utils", () => {
  it("maps api violations to field errors", () => {
    const setError = vi.fn();

    mapApiErrorsToForm(
      {
        violations: [
          { field: "email", message: "Invalid email" },
          { field: "password", message: "Too short" },
        ],
      },
      setError
    );

    expect(setError).toHaveBeenCalledTimes(2);
    expect(setError).toHaveBeenNthCalledWith(1, "email", {
      type: "server",
      message: "Invalid email",
    });
    expect(setError).toHaveBeenNthCalledWith(2, "password", {
      type: "server",
      message: "Too short",
    });
  });

  it("returns detail, title or fallback error messages", () => {
    expect(getApiErrorMessage({ detail: "Bad request" })).toBe("Bad request");
    expect(getApiErrorMessage({ title: "Forbidden" })).toBe("Forbidden");
    expect(getApiErrorMessage(new Error("Boom"))).toBe("Boom");
    expect(getApiErrorMessage({})).toBe("An unexpected error occurred. Please try again.");
  });

  it("detects API error codes", () => {
    expect(isApiErrorCode({ code: "RESOURCE_NOT_FOUND" }, "RESOURCE_NOT_FOUND")).toBe(true);
    expect(isApiErrorCode({ code: "OTHER" }, "RESOURCE_NOT_FOUND")).toBe(false);
  });
});
