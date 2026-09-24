export interface TimestampLike {
  toDate(): Date;
}

export interface BookmarkOutput {
  id: string;
  url: string;
  urlHash: string;
  status: "unread" | "read";
  hidden: boolean;
  note: string;
  source: string;
  createdBy?: "android" | "chrome-extension" | "node-demo";
  lastSharedBy?: "android" | "chrome-extension" | "node-demo";
  createdAt: string | null;
  updatedAt: string | null;
}

const CLIENTS = new Set(["android", "chrome-extension", "node-demo"]);

export function bookmarksPath(uid: string): string {
  if (uid.length === 0 || uid.includes("/")) {
    throw new Error("uid must be one non-empty Firestore path segment");
  }

  return `users/${uid}/bookmarks`;
}

export function toBookmarkOutput(
  id: string,
  data: Record<string, unknown>
): BookmarkOutput {
  const url = requiredString(id, data, "url");
  const urlHash = requiredString(id, data, "urlHash");
  const status = data.status;
  const hidden = data.hidden;

  if (status !== "unread" && status !== "read") {
    throw new Error(`Bookmark ${id} has an invalid status`);
  }

  if (typeof hidden !== "boolean") {
    throw new Error(`Bookmark ${id} is missing required field hidden`);
  }

  return {
    id,
    url,
    urlHash,
    status,
    hidden,
    note: optionalString(data.note),
    source: optionalString(data.source),
    ...optionalClient("createdBy", data.createdBy),
    ...optionalClient("lastSharedBy", data.lastSharedBy),
    createdAt: timestampToIso(data.createdAt),
    updatedAt: timestampToIso(data.updatedAt)
  };
}

function requiredString(
  id: string,
  data: Record<string, unknown>,
  field: string
): string {
  const value = data[field];
  if (typeof value !== "string" || value.length === 0) {
    throw new Error(`Bookmark ${id} is missing required field ${field}`);
  }
  return value;
}

function optionalString(value: unknown): string {
  return typeof value === "string" ? value : "";
}

function optionalClient(
  field: "createdBy" | "lastSharedBy",
  value: unknown
): Partial<Pick<BookmarkOutput, "createdBy" | "lastSharedBy">> {
  return typeof value === "string" && CLIENTS.has(value)
    ? { [field]: value }
    : {};
}

function timestampToIso(value: unknown): string | null {
  if (
    typeof value !== "object" ||
    value === null ||
    !("toDate" in value) ||
    typeof value.toDate !== "function"
  ) {
    return null;
  }

  return (value as TimestampLike).toDate().toISOString();
}
