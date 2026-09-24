# LinkPipe Chrome Extension

[繁體中文](#繁體中文)

This Manifest V3 extension writes bookmarks to the same `/users/{uid}/bookmarks/{urlHash}` collection as the Android app. It can capture the current page, a link from the page context menu, or a manually entered URL, with an optional note.

## Requirements

- Google Chrome with Manifest V3 support.

- Node.js 22 or later.

- The same Firebase project used by the Android app, with Google Authentication and Cloud Firestore enabled.

## Firebase and Google OAuth setup

1. Register a Web app in the shared Firebase project and keep the Google sign-in provider enabled.

2. Generate a private key so the unpacked extension keeps a stable extension ID:

```bash
openssl genrsa -out key.pem 2048
chmod 600 key.pem
openssl rsa -in key.pem -pubout -outform DER | base64 | tr -d '\n'
```

   Save the final base64 value as `LINKPIPE_EXTENSION_KEY`. `key.pem` is ignored by Git; back it up securely and never commit it. The build writes only the public key to `dist/manifest.json`. Chrome derives the extension ID from that public key.

3. Build once with `LINKPIPE_EXTENSION_KEY`, load `dist` as an unpacked extension, and copy the resulting extension ID from `chrome://extensions`.

4. In Google Cloud Console, create an OAuth client of type **Chrome Extension** for that exact extension ID. Copy its client ID.

5. Copy `.env.example` to `.env.local` and fill in the Firebase Web app values, OAuth client ID, and extension public key:

```dotenv
LINKPIPE_FIREBASE_API_KEY=...
LINKPIPE_FIREBASE_AUTH_DOMAIN=your-project.firebaseapp.com
LINKPIPE_FIREBASE_PROJECT_ID=your-project-id
LINKPIPE_FIREBASE_APP_ID=...
LINKPIPE_GOOGLE_OAUTH_CLIENT_ID=...
LINKPIPE_EXTENSION_KEY=...
```

Firebase Web configuration and the OAuth client ID are identifiers embedded in the built extension, not server secrets. The private `key.pem` must remain outside version control.

## Build and load

Export the local configuration, install dependencies, and run all checks:

```bash
set -a
source .env.local
set +a
npm install
npm run check
```

Then open `chrome://extensions`, enable **Developer mode**, choose **Load unpacked**, and select this directory's `dist` folder.

When you choose the same Google account as the Android app, the extension authenticates that account with Firebase and uses the same Firebase UID and bookmark path. The clients do not share a local sign-in session.

## Usage

- Select the LinkPipe toolbar icon to save the current page or enter another URL, optionally with a note.

- Right-click a page link and select **Save link to LinkPipe** to open a capture window prefilled with that link.

- Select **View bookmarks** in the popup to open the non-hidden bookmark list and edit notes.

## Development checks

```bash
npm run check
```

`npm run check` runs TypeScript type checking, unit tests, and a production build. A build can complete with missing environment variables, but Google sign-in and Firestore operations remain disabled until configuration is supplied.

---

## 繁體中文

這個 Manifest V3 擴充功能會將書籤寫入與 Android App 相同的 `/users/{uid}/bookmarks/{urlHash}`。它支援目前頁面、頁面右鍵選單中的連結、手動輸入網址，以及選填備註。

### 系統需求

- 支援 Manifest V3 的 Google Chrome。

- Node.js 22 或更新版本。

- 與 Android App 相同、且已啟用 Google Authentication 與 Cloud Firestore 的 Firebase 專案。

### Firebase 與 Google OAuth 設定

1. 在共用 Firebase 專案註冊 Web App，並維持 Google 登入 provider 啟用。

2. 產生私鑰，使未封裝擴充功能具有固定的 Extension ID：

```bash
openssl genrsa -out key.pem 2048
chmod 600 key.pem
openssl rsa -in key.pem -pubout -outform DER | base64 | tr -d '\n'
```

   將最後的 base64 值存為 `LINKPIPE_EXTENSION_KEY`。`key.pem` 已被 Git 忽略；請安全備份且絕對不要提交。建置只會把公鑰寫入 `dist/manifest.json`，Chrome 會以該公鑰推導 Extension ID。

3. 先帶入 `LINKPIPE_EXTENSION_KEY` 建置一次，將 `dist` 載入為未封裝擴充功能，再從 `chrome://extensions` 複製 Extension ID。

4. 在 Google Cloud Console 為該 Extension ID 建立類型為 **Chrome Extension** 的 OAuth client，並複製 client ID。

5. 將 `.env.example` 複製為 `.env.local`，填入 Firebase Web App 設定、OAuth client ID 與擴充功能公鑰：

```dotenv
LINKPIPE_FIREBASE_API_KEY=...
LINKPIPE_FIREBASE_AUTH_DOMAIN=your-project.firebaseapp.com
LINKPIPE_FIREBASE_PROJECT_ID=your-project-id
LINKPIPE_FIREBASE_APP_ID=...
LINKPIPE_GOOGLE_OAUTH_CLIENT_ID=...
LINKPIPE_EXTENSION_KEY=...
```

Firebase Web 設定與 OAuth client ID 是會嵌入擴充功能的識別資訊，並非伺服器秘密；私有的 `key.pem` 必須留在版本控制之外。

### 建置與載入

匯入本機設定、安裝相依套件並執行完整檢查：

```bash
set -a
source .env.local
set +a
npm install
npm run check
```

接著開啟 `chrome://extensions`，啟用**開發人員模式**，選擇**載入未封裝項目**，並載入本目錄的 `dist` 資料夾。

選擇與 Android App 相同的 Google 帳號時，擴充功能會以該帳號登入 Firebase，因此使用相同的 Firebase UID 與書籤路徑；兩端不會共用本機登入 session。

### 使用方式

- 點擊工具列的 LinkPipe 圖示，可儲存目前頁面或輸入其他網址，並可加入備註。

- 在頁面連結按右鍵並選擇 **Save link to LinkPipe**，會開啟已預填該連結的擷取視窗。

- 在 popup 選擇 **View bookmarks**，可開啟未隱藏的書籤清單並編輯備註。

### 開發檢查

```bash
npm run check
```

`npm run check` 會執行 TypeScript 型別檢查、單元測試與 production build。未提供環境變數時仍可完成建置，但 Google 登入與 Firestore 操作會維持停用，直到設定完成。
