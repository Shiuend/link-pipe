import { readFileSync } from "node:fs";
import { resolve } from "node:path";
import { describe, expect, it } from "vitest";
import { bookmarkWrite, normalizeUrl, sha256, shouldCloseCapture } from "../src/bookmark";

type Fixture = { raw: string; normalized: string; sha256: string };

describe("shared URL contract", () => {
  const fixtures = (JSON.parse(
    readFileSync(resolve(import.meta.dirname, "../../../contracts/fixtures/url-normalization-fixtures.json"), "utf8"),
  ) as { cases: Fixture[] }).cases;

  for (const fixture of fixtures) {
    it(`normalizes ${fixture.raw}`, async () => {
      const normalized = normalizeUrl(fixture.raw);
      expect(normalized).toBe(fixture.normalized);
      expect(await sha256(normalized)).toBe(fixture.sha256);
    });
  }

  it("preserves encoded query components while sorting them", () => {
    expect(normalizeUrl("https://example.com/?b=x%20y&a=%2F")).toBe("https://example.com/?a=%2F&b=x%20y");
  });

  it("rejects non-web URLs", () => {
    expect(() => normalizeUrl("javascript:alert(1)")).toThrow("http");
  });
});

describe("bookmark write contract", () => {
  it("includes the note and Chrome Extension platform metadata", () => {
    expect(bookmarkWrite("https://example.com/", "abc", "A note", "current-page")).toEqual({
      id: "abc",
      url: "https://example.com/",
      urlHash: "abc",
      note: "A note",
      source: "current-page",
      schemaVersion: 1,
      createdBy: "chrome-extension",
      lastSharedBy: "chrome-extension",
      status: "unread",
      hidden: false,
    });
  });

  it("closes only the page-link capture window after saving", () => {
    expect(shouldCloseCapture("page-link")).toBe(true);
    expect(shouldCloseCapture("current-page")).toBe(false);
    expect(shouldCloseCapture("manual")).toBe(false);
  });
});
