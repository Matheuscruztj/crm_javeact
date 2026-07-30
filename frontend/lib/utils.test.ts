import { describe, expect, it } from "vitest";
import { cn } from "./utils";

describe("cn", () => {
  it("merges tailwind classes deterministically", () => {
    expect(cn("px-2", "px-4", "font-medium")).toBe("px-4 font-medium");
  });

  it("ignores falsey inputs", () => {
    expect(cn("text-sm", false, null, undefined, "leading-6")).toBe("text-sm leading-6");
  });
});
