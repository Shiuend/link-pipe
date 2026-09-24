import assert from "node:assert/strict";
import test from "node:test";

import { bookmarksPath, toBookmarkOutput } from "../src/bookmark.js";

test("builds the contract Firestore path for one authenticated user", () => {
  assert.equal(bookmarksPath("user-123"), "users/user-123/bookmarks");
  assert.throws(() => bookmarksPath("other/user"), /uid/i);
});

test("maps a complete Firestore document to JSON-safe output", () => {
  const output = toBookmarkOutput("abc", {
    url: "https://example.com/article",
    urlHash: "hash",
    status: "read",
    hidden: false,
    note: "Saved note",
    source: "Chrome",
    createdBy: "android",
    lastSharedBy: "node-demo",
    createdAt: { toDate: () => new Date("2026-09-20T12:00:00.000Z") },
    updatedAt: { toDate: () => new Date("2026-09-20T13:00:00.000Z") },
    futureField: "ignored"
  });

  assert.deepEqual(output, {
    id: "abc",
    url: "https://example.com/article",
    urlHash: "hash",
    status: "read",
    hidden: false,
    note: "Saved note",
    source: "Chrome",
    createdBy: "android",
    lastSharedBy: "node-demo",
    createdAt: "2026-09-20T12:00:00.000Z",
    updatedAt: "2026-09-20T13:00:00.000Z"
  });
});

test("applies contract defaults to legacy documents", () => {
  assert.deepEqual(
    toBookmarkOutput("legacy", {
      url: "https://example.com",
      urlHash: "hash",
      status: "unread",
      hidden: false
    }),
    {
      id: "legacy",
      url: "https://example.com",
      urlHash: "hash",
      status: "unread",
      hidden: false,
      note: "",
      source: "",
      createdAt: null,
      updatedAt: null
    }
  );
});

test("rejects a document missing an interoperable field", () => {
  assert.throws(
    () => toBookmarkOutput("broken", { url: "https://example.com" }),
    /broken.*urlHash/i
  );
});
