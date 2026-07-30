import { describe, expect, it, vi } from "vitest";
import { generateId, getFocusableElements, handleFocusTrap, liveRegionProps, WCAG_AA } from "./accessibility";

describe("accessibility", () => {
  it("generates ids with prefix", () => {
    const id = generateId("field");
    expect(id.startsWith("field-")).toBe(true);
    expect(id.length).toBeGreaterThan("field-".length);
  });

  it("returns live region props", () => {
    expect(liveRegionProps()).toEqual({ "aria-live": "polite", "aria-atomic": true });
    expect(liveRegionProps("assertive")).toEqual({ "aria-live": "assertive", "aria-atomic": true });
  });

  it("finds focusable elements", () => {
    document.body.innerHTML = `
      <div>
        <button>One</button>
        <a href="/test">Two</a>
        <input />
      </div>
    `;
    const container = document.body.firstElementChild as HTMLElement;
    expect(getFocusableElements(container)).toHaveLength(3);
    expect(WCAG_AA.NORMAL_TEXT).toBe(4.5);
  });

  it("handles focus trapping at the boundaries and ignores non-tab keys", () => {
    document.body.innerHTML = `
      <div>
        <button id="first">One</button>
        <button id="second">Two</button>
      </div>
    `;
    const container = document.body.firstElementChild as HTMLElement;
    const [first, last] = getFocusableElements(container);

    const nonTabEvent = {
      key: "Enter",
      shiftKey: false,
      preventDefault: vi.fn(),
    } as unknown as KeyboardEvent;
    handleFocusTrap(nonTabEvent, container);
    expect(nonTabEvent.preventDefault).not.toHaveBeenCalled();

    const emptyContainer = document.createElement("div");
    const emptyEvent = {
      key: "Tab",
      shiftKey: false,
      preventDefault: vi.fn(),
    } as unknown as KeyboardEvent;
    handleFocusTrap(emptyEvent, emptyContainer);
    expect(emptyEvent.preventDefault).not.toHaveBeenCalled();

    first.focus();
    const backwardEvent = {
      key: "Tab",
      shiftKey: true,
      preventDefault: vi.fn(),
    } as unknown as KeyboardEvent;
    handleFocusTrap(backwardEvent, container);
    expect(backwardEvent.preventDefault).toHaveBeenCalledTimes(1);
    expect(document.activeElement).toBe(last);

    last.focus();
    const forwardEvent = {
      key: "Tab",
      shiftKey: false,
      preventDefault: vi.fn(),
    } as unknown as KeyboardEvent;
    handleFocusTrap(forwardEvent, container);
    expect(forwardEvent.preventDefault).toHaveBeenCalledTimes(1);
    expect(document.activeElement).toBe(first);
  });
});
