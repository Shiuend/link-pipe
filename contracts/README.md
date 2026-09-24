# LinkPipe Shared Data Contract

[繁體中文](#繁體中文)

This directory defines the Firestore bookmark shape and URL-normalization behavior shared by the Android app, Chrome extension, and Node.js demo.

## Firestore location and ownership

- Bookmarks live at `/users/{uid}/bookmarks/{urlHash}`.

- Every client derives `uid` from its authenticated user. Client code must never accept another user's UID as an untrusted write target.

- The document ID is the lowercase SHA-256 digest of the normalized URL and must equal `urlHash`.

- Firestore Security Rules must restrict each user to their own `/users/{uid}` data. Firebase Admin SDK tools bypass those rules and require separate IAM controls.

## Bookmark fields

[`bookmark.schema.json`](bookmark.schema.json) is the machine-readable contract. Unknown fields are allowed so the contract can evolve without breaking older readers.

- `url`, `urlHash`, `status`, and `hidden` are the interoperable minimum required fields.

- `status` is either `unread` or `read`. `hidden: true` represents a soft-deleted bookmark.

- `note` and `source` default to empty strings when absent. `source` describes where the shared link came from, such as an Android package or website; it does not identify the LinkPipe client that wrote the document.

- `createdBy` and `lastSharedBy` identify the LinkPipe client: `android`, `chrome-extension`, or `node-demo`. They and `schemaVersion` remain optional for compatibility with existing Android documents.

- Writers use merge/upsert operations so sharing the same normalized URL updates its deterministic document rather than creating a duplicate. `updatedAt` should use a server timestamp.

- The current Android client rewrites `createdAt` when the same URL is shared again. Consumers must therefore treat it as the latest share time for legacy documents. New clients should preserve an existing `createdAt`; it cannot become a guaranteed first-creation time until Android changes and legacy data is migrated.

- Readers ignore unknown fields and supply defaults for missing optional fields. New required fields are not backward compatible until every deployed client supports them.

## URL normalization version 1

Apply these operations in order before hashing:

1. Trim surrounding whitespace. Accept only absolute `http` or `https` URLs with a host.

2. Lowercase the scheme and host while preserving path case.

3. Remove default ports (`80` for HTTP and `443` for HTTPS), the fragment, and one trailing slash when the path is longer than `/`.

4. Sort non-empty query components by key and then value while preserving their encoded representation.

5. Serialize the normalized URL as ASCII and compute its lowercase hexadecimal SHA-256 digest.

The cases in [`fixtures/url-normalization-fixtures.json`](fixtures/url-normalization-fixtures.json) are normative. A normalization change must update the fixtures and every client implementation together.

---

## 繁體中文

本目錄定義 Android App、Chrome 擴充功能與 Node.js demo 共用的 Firestore 書籤格式及 URL 正規化行為。

### Firestore 位置與擁有權

- 書籤位於 `/users/{uid}/bookmarks/{urlHash}`。

- 每個 client 都必須從已驗證的登入使用者取得 `uid`；client 程式不可接受另一名使用者的 UID 作為不受信任的寫入目標。

- 文件 ID 是正規化 URL 的小寫 SHA-256 digest，且必須等於 `urlHash`。

- Firestore Security Rules 必須限制每位使用者只能存取自己的 `/users/{uid}` 資料。Firebase Admin SDK 工具會繞過這些規則，因此需要另外使用 IAM 控制權限。

### 書籤欄位

[`bookmark.schema.json`](bookmark.schema.json) 是機器可讀的合約。為了讓合約演進而不破壞舊版 reader，它允許未知欄位。

- `url`、`urlHash`、`status` 與 `hidden` 是跨端互通所需的最小必填欄位。

- `status` 只能是 `unread` 或 `read`；`hidden: true` 表示軟刪除的書籤。

- 缺少 `note` 與 `source` 時預設為空字串。`source` 描述分享連結的來源，例如 Android package 或網站；它不代表寫入文件的 LinkPipe client。

- `createdBy` 與 `lastSharedBy` 用於識別 LinkPipe client：`android`、`chrome-extension` 或 `node-demo`。為了相容既有 Android 文件，這兩個欄位及 `schemaVersion` 仍為選填。

- Writer 使用 merge/upsert，因此再次分享相同的正規化 URL 時會更新確定性文件，而不是建立重複項目。`updatedAt` 應使用 server timestamp。

- 目前 Android client 再次分享相同 URL 時會重寫 `createdAt`，因此 consumer 必須把舊文件中的它視為最近分享時間。新的 client 應保留既有 `createdAt`；在 Android 行為調整且舊資料完成遷移前，此欄位不能保證是首次建立時間。

- Reader 會忽略未知欄位，並為缺少的選填欄位提供預設值。在所有已部署 client 都支援前，新增必填欄位並不向下相容。

### URL 正規化版本 1

雜湊前依序執行以下操作：

1. 移除前後空白，只接受具有 host 的絕對 `http` 或 `https` URL。

2. 將 scheme 與 host 轉為小寫，保留 path 的大小寫。

3. 移除預設 port（HTTP 的 `80`、HTTPS 的 `443`）、fragment，以及 path 長於 `/` 時的一個結尾斜線。

4. 依 key、再依 value 排序非空 query components，並保留其編碼表示。

5. 將正規化 URL 序列化為 ASCII，再計算小寫十六進位 SHA-256 digest。

[`fixtures/url-normalization-fixtures.json`](fixtures/url-normalization-fixtures.json) 中的案例是規範依據。任何正規化變更都必須同時更新 fixtures 與每個 client 實作。
