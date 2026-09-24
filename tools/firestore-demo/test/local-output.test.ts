import assert from "node:assert/strict";
import { readFile } from "node:fs/promises";
import { join } from "node:path";
import test from "node:test";
import { tmpdir } from "node:os";
import { mkdtemp } from "node:fs/promises";

import { saveLocalCopy } from "../src/local-output.js";

test("saves the same formatted JSON emitted by the CLI", async () => {
  const directory = await mkdtemp(join(tmpdir(), "linkpipe-firestore-demo-"));
  const outputPath = join(directory, "bookmarks.json");
  const bookmarks = [{ id: "bookmark-1", url: "https://example.com" }];

  const json = await saveLocalCopy(outputPath, bookmarks);

  assert.equal(json, `${JSON.stringify(bookmarks, null, 2)}\n`);
  assert.equal(await readFile(outputPath, "utf8"), json);
});
