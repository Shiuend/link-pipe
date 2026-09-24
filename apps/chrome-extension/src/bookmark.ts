export type CaptureSource = "current-page" | "page-link" | "manual";

export function shouldCloseCapture(source: CaptureSource): boolean {
  return source === "page-link";
}

export interface BookmarkRecord {
  id: string;
  url: string;
  urlHash: string;
  note: string;
  source: string;
  status: "unread" | "read";
  hidden: boolean;
  createdAt?: unknown;
  updatedAt?: unknown;
}

export function normalizeUrl(raw: string): string {
  const url = new URL(raw.trim());
  if (url.protocol !== "http:" && url.protocol !== "https:") {
    throw new Error("只支援 http 或 https 網址");
  }

  url.hash = "";
  if ((url.protocol === "http:" && url.port === "80") || (url.protocol === "https:" && url.port === "443")) {
    url.port = "";
  }
  if (url.pathname.length > 1 && url.pathname.endsWith("/")) {
    url.pathname = url.pathname.slice(0, -1);
  }

  const query = url.search
    .slice(1)
    .split("&")
    .filter(Boolean)
    .sort((left, right) => {
      const [leftKey, ...leftValue] = left.split("=");
      const [rightKey, ...rightValue] = right.split("=");
      return leftKey.localeCompare(rightKey) || leftValue.join("=").localeCompare(rightValue.join("="));
    });
  url.search = query.length ? `?${query.join("&")}` : "";
  return url.toString();
}

export async function sha256(value: string): Promise<string> {
  const bytes = await crypto.subtle.digest("SHA-256", new TextEncoder().encode(value));
  return [...new Uint8Array(bytes)].map((byte) => byte.toString(16).padStart(2, "0")).join("");
}

export function bookmarkWrite(url: string, urlHash: string, note: string, source: CaptureSource) {
  return {
    id: urlHash,
    url,
    urlHash,
    note,
    source,
    schemaVersion: 1,
    createdBy: "chrome-extension",
    lastSharedBy: "chrome-extension",
    status: "unread",
    hidden: false,
  } as const;
}
