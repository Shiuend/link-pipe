#!/usr/bin/env node

import { applicationDefault, initializeApp } from "firebase-admin/app";
import { getFirestore } from "firebase-admin/firestore";

import { bookmarksPath, toBookmarkOutput } from "./bookmark.js";
import { saveLocalCopy } from "./local-output.js";

const usage = `Usage: npm start -- --uid <firebase-uid> [--project-id <project-id>] [--include-hidden] [--output <path>]

Reads LinkPipe bookmarks, prints JSON, and saves the same data locally.

Options:
  --uid <uid>               Firebase Authentication user UID to read (required)
  --project-id <project-id> Firebase project override; otherwise use ADC/default config
  --include-hidden          Include soft-deleted bookmarks
  --output <path>           Local JSON path (default: firestore-bookmarks.json)
  --help                    Show this help`;

interface Options {
  uid: string;
  projectId?: string;
  includeHidden: boolean;
  outputPath: string;
}

function parseArguments(args: string[]): Options | null {
  if (args.includes("--help")) {
    return null;
  }

  let uid: string | undefined;
  let projectId: string | undefined;
  let includeHidden = false;
  let outputPath = "firestore-bookmarks.json";

  for (let index = 0; index < args.length; index += 1) {
    const argument = args[index];
    if (argument === "--include-hidden") {
      includeHidden = true;
    } else if (
      argument === "--uid" ||
      argument === "--project-id" ||
      argument === "--output"
    ) {
      const value = args[index + 1];
      if (value === undefined || value.startsWith("--")) {
        throw new Error(`${argument} requires a value`);
      }
      if (argument === "--uid") uid = value;
      else if (argument === "--project-id") projectId = value;
      else outputPath = value;
      index += 1;
    } else {
      throw new Error(`Unknown argument: ${argument}`);
    }
  }

  if (uid === undefined) {
    throw new Error("--uid is required");
  }

  return { uid, projectId, includeHidden, outputPath };
}

async function main(): Promise<void> {
  const options = parseArguments(process.argv.slice(2));
  if (options === null) {
    console.log(usage);
    return;
  }

  const app = initializeApp({
    credential: applicationDefault(),
    ...(options.projectId === undefined ? {} : { projectId: options.projectId })
  });
  const snapshot = await getFirestore(app).collection(bookmarksPath(options.uid)).get();
  const bookmarks = snapshot.docs
    .map((document) => toBookmarkOutput(document.id, document.data()))
    .filter((bookmark) => options.includeHidden || !bookmark.hidden)
    .sort((left, right) =>
      (right.updatedAt ?? right.createdAt ?? "").localeCompare(
        left.updatedAt ?? left.createdAt ?? ""
      )
    );

  const json = await saveLocalCopy(options.outputPath, bookmarks);
  process.stdout.write(json);
}

main().catch((error: unknown) => {
  const message = error instanceof Error ? error.message : String(error);
  console.error(`Firestore demo failed: ${message}`);
  console.error(usage);
  process.exitCode = 1;
});
