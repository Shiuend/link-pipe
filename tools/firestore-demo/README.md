# LinkPipe Firestore Demo

[繁體中文](#繁體中文)

This read-only Node.js CLI uses the Firebase Admin SDK to fetch one user's LinkPipe bookmarks from `/users/{uid}/bookmarks`. It prints JSON to standard output and saves the same data to a local file. It never creates, updates, or deletes Firestore data.

## Requirements and security

- Node.js 22 or later.

- Application Default Credentials with read access to the target Firebase project. For local development, prefer `gcloud auth application-default login`. If a service account is required, point `GOOGLE_APPLICATION_CREDENTIALS` to an untracked JSON file in a controlled environment.

- The Firebase Authentication UID of the user to read.

The Admin SDK bypasses Firestore Security Rules. Run this tool only with trusted projects and credentials, grant the narrowest practical IAM access, and never add service-account JSON to this repository.

## Install and run

```bash
cd tools/firestore-demo
npm install
npm run build
npm start -- --uid FIREBASE_UID --project-id FIREBASE_PROJECT_ID
```

Replace the placeholders with the target user UID and Firebase project ID. Omit `--project-id` when Application Default Credentials or the local environment already resolves the correct project.

On success, the CLI prints a JSON array and writes the same content to `firestore-bookmarks.json` in the current directory. The output is sorted from newest to oldest by `updatedAt`, then `createdAt`.

## Options

```text
--uid <uid>               Firebase Authentication UID to read (required)
--project-id <project-id> Firebase project override
--include-hidden          Include soft-deleted bookmarks
--output <path>           Output file (default: firestore-bookmarks.json)
--help                    Show command help
```

The directory for `--output` must already exist. An existing output file is replaced. Hidden bookmarks are excluded unless `--include-hidden` is present.

## Verify

```bash
npm run check
```

This command runs unit tests and a TypeScript build. Unit tests do not contact Firebase. A live read requires valid credentials, project access, and a UID.

---

## 繁體中文

這個唯讀 Node.js CLI 使用 Firebase Admin SDK，從 `/users/{uid}/bookmarks` 取得指定使用者的 LinkPipe 書籤。它會將 JSON 輸出至標準輸出，並把相同資料存成本機檔案；它不會建立、更新或刪除 Firestore 資料。

### 系統需求與安全性

- Node.js 22 或更新版本。

- 可讀取目標 Firebase 專案的 Application Default Credentials。本機開發時建議使用 `gcloud auth application-default login`；若必須使用 service account，請在受控環境中以 `GOOGLE_APPLICATION_CREDENTIALS` 指向未被 Git 追蹤的 JSON 檔。

- 目標 Firebase Authentication 使用者的 UID。

Admin SDK 會繞過 Firestore Security Rules。請只對受信任的專案與憑證執行此工具、授予實務上最小的 IAM 權限，並且絕對不要將 service-account JSON 加入本 repository。

### 安裝與執行

```bash
cd tools/firestore-demo
npm install
npm run build
npm start -- --uid FIREBASE_UID --project-id FIREBASE_PROJECT_ID
```

請將 placeholder 替換成目標使用者 UID 與 Firebase 專案 ID。若 Application Default Credentials 或本機環境已能解析正確專案，可省略 `--project-id`。

成功時，CLI 會輸出 JSON array，並將相同內容寫入目前目錄的 `firestore-bookmarks.json`。輸出會先依 `updatedAt`、再依 `createdAt`，由新到舊排序。

### 選項

```text
--uid <uid>               要讀取的 Firebase Authentication UID（必填）
--project-id <project-id> 覆寫 Firebase 專案
--include-hidden          包含軟刪除的書籤
--output <path>           輸出檔案（預設：firestore-bookmarks.json）
--help                    顯示指令說明
```

`--output` 的目標資料夾必須已存在，既有輸出檔會被取代。除非加入 `--include-hidden`，否則隱藏書籤不會出現在結果中。

### 驗證

```bash
npm run check
```

此指令會執行單元測試與 TypeScript build。單元測試不會連線至 Firebase；實際讀取需要有效憑證、專案權限與 UID。
