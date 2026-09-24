# LinkPipe

[繁體中文](#繁體中文)

LinkPipe is a multi-client bookmark project backed by a shared Firestore data model. It includes a Jetpack Compose Android app, a Manifest V3 Chrome extension, and a read-only Node.js command-line demo.

## Components

- `apps/android`: Android app for saving shared links, browsing bookmarks, editing notes, changing read status, and soft-deleting entries.

- `apps/chrome-extension`: Chrome extension for saving the current page, a page link, or a manually entered URL, with optional notes.

- `contracts`: Shared Firestore fields, URL-normalization rules, and cross-client fixtures.

- `tools/firestore-demo`: Read-only TypeScript CLI that exports one user's bookmarks through the Firebase Admin SDK.

## Requirements

- Android Studio or JDK 17 for the Android project.

- Node.js 22 or later for the Chrome extension and Firestore demo.

- A Firebase project with Google Authentication and Cloud Firestore enabled for live use.

## Firebase setup

1. Register an Android app in Firebase with package name `com.linkpipe.app`.

2. Save its `google-services.json` as `apps/android/app/google-services.json`. The file is ignored by Git because it contains project-specific configuration.

3. Enable the Google sign-in provider. Set `default_web_client_id` in `apps/android/app/src/main/res/values/strings.xml` to the Firebase project's Web OAuth client ID.

4. Create Cloud Firestore. Bookmarks are stored at `/users/{uid}/bookmarks/{urlHash}`; deploy Security Rules that allow users to access only their own documents.

The Android JVM tests and Kotlin compilation work without `google-services.json`. Live sign-in and Firestore access require the Firebase setup above.

## Build and verify

Android:

```bash
cd apps/android
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

Chrome extension:

```bash
cd apps/chrome-extension
npm install
npm run check
```

See [`apps/chrome-extension/README.md`](apps/chrome-extension/README.md) for Firebase, OAuth, and unpacked-extension setup.

Firestore demo:

```bash
cd tools/firestore-demo
npm install
npm run check
npm start -- --help
```

See [`tools/firestore-demo/README.md`](tools/firestore-demo/README.md) for credentials, permissions, and usage.

## Shared data contract

All clients use the same deterministic document path and URL-normalization fixtures. Read [`contracts/README.md`](contracts/README.md) before changing stored fields or normalization behavior.

## Public release snapshots

Private releases use `vMAJOR.MINOR.PATCH` tags. Android, the Chrome extension, and the Firestore demo share one version. Each published snapshot in [`Shiuend/link-pipe`](https://github.com/Shiuend/link-pipe) records the corresponding private source revision as a full `Source-Commit` SHA in its commit message; the public repository does not create new release tags.

Public snapshots contain only approved product paths. They exclude internal specifications, release tooling, Agentflow records, credentials, and the private repository's Git history.

## License

LinkPipe is available under the [MIT License](LICENSE).

---

## 繁體中文

LinkPipe 是以共用 Firestore 資料模型為基礎的多端書籤專案，包含 Jetpack Compose Android App、Manifest V3 Chrome 擴充功能，以及唯讀的 Node.js 命令列示範工具。

### 元件

- `apps/android`：用於儲存分享連結、瀏覽書籤、編輯備註、變更閱讀狀態及軟刪除項目的 Android App。

- `apps/chrome-extension`：可儲存目前頁面、頁面連結或手動輸入網址，並可加入備註的 Chrome 擴充功能。

- `contracts`：共用的 Firestore 欄位、URL 正規化規則與跨端測試 fixtures。

- `tools/firestore-demo`：透過 Firebase Admin SDK 匯出指定使用者書籤的唯讀 TypeScript CLI。

### 系統需求

- Android 專案需要 Android Studio 或 JDK 17。

- Chrome 擴充功能與 Firestore demo 需要 Node.js 22 或更新版本。

- 實際使用時需要已啟用 Google Authentication 與 Cloud Firestore 的 Firebase 專案。

### Firebase 設定

1. 在 Firebase 註冊 Android App，package name 使用 `com.linkpipe.app`。

2. 將 `google-services.json` 存為 `apps/android/app/google-services.json`。此檔案包含專案專屬設定，已被 Git 忽略。

3. 啟用 Google 登入 provider，並將 Firebase 專案的 Web OAuth client ID 填入 `apps/android/app/src/main/res/values/strings.xml` 的 `default_web_client_id`。

4. 建立 Cloud Firestore。書籤位於 `/users/{uid}/bookmarks/{urlHash}`；請部署 Security Rules，限制使用者只能存取自己的文件。

未放入 `google-services.json` 時仍可執行 Android JVM 測試與 Kotlin 編譯；實際登入與 Firestore 存取需要完成以上設定。

### 建置與驗證

Android：

```bash
cd apps/android
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

Chrome 擴充功能：

```bash
cd apps/chrome-extension
npm install
npm run check
```

Firebase、OAuth 與載入未封裝擴充功能的設定請見 [`apps/chrome-extension/README.md`](apps/chrome-extension/README.md)。

Firestore demo：

```bash
cd tools/firestore-demo
npm install
npm run check
npm start -- --help
```

憑證、權限與使用方式請見 [`tools/firestore-demo/README.md`](tools/firestore-demo/README.md)。

### 共用資料合約

所有 client 使用相同的確定性文件路徑與 URL 正規化 fixtures。變更儲存欄位或正規化行為前，請先閱讀 [`contracts/README.md`](contracts/README.md)。

### 公開 release 快照

私人來源專案使用 `vMAJOR.MINOR.PATCH` tag 發布正式版本，Android、Chrome 擴充功能與 Firestore demo 共用同一版本。[`Shiuend/link-pipe`](https://github.com/Shiuend/link-pipe) 中每個公開快照的 commit message 都會以完整 `Source-Commit` SHA 記錄對應的私人來源 revision；公開 repository 不再建立新的 release tag。

公開快照只包含核准的產品路徑，不包含內部規格、發布工具、Agentflow 紀錄、憑證或私人 repository 的 Git history。

### 授權

LinkPipe 採用 [MIT License](LICENSE) 授權。
