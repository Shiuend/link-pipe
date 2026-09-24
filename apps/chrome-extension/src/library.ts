import { currentUser, listBookmarks, signInWithGoogle, signOutEverywhere, updateNote } from "./firebase";
import type { BookmarkRecord } from "./bookmark";
import { errorMessage, requiredElement, showMessage } from "./ui";

const authLabel = requiredElement<HTMLElement>("#auth-label");
const signInButton = requiredElement<HTMLButtonElement>("#sign-in");
const signOutButton = requiredElement<HTMLButtonElement>("#sign-out");
const message = requiredElement<HTMLElement>("#message");
const list = requiredElement<HTMLElement>("#bookmark-list");

function bookmarkItem(bookmark: BookmarkRecord): HTMLElement {
  const article = document.createElement("article");
  article.className = "bookmark";

  const link = document.createElement("a");
  link.href = bookmark.url;
  link.target = "_blank";
  link.rel = "noreferrer";
  link.textContent = bookmark.url;

  const note = document.createElement("textarea");
  note.value = bookmark.note || "";
  note.placeholder = "加入備註";
  note.setAttribute("aria-label", `編輯 ${bookmark.url} 的備註`);

  const save = document.createElement("button");
  save.textContent = "儲存備註";
  save.addEventListener("click", async () => {
    save.disabled = true;
    try {
      await updateNote(bookmark.urlHash || bookmark.id, note.value);
      showMessage(message, "備註已更新。", "success");
    } catch (error) {
      showMessage(message, errorMessage(error), "error");
    } finally {
      save.disabled = false;
    }
  });

  article.append(link, note, save);
  return article;
}

async function render() {
  const user = await currentUser();
  authLabel.textContent = user?.email || "尚未登入";
  signInButton.hidden = Boolean(user);
  signOutButton.hidden = !user;
  list.replaceChildren();
  if (!user) {
    showMessage(message, "登入後即可查看 Android 與 Extension 共用的書籤。", "neutral");
    return;
  }

  showMessage(message, "正在載入…");
  try {
    const bookmarks = await listBookmarks();
    list.replaceChildren(...bookmarks.map(bookmarkItem));
    showMessage(message, bookmarks.length ? `共 ${bookmarks.length} 筆書籤。` : "目前沒有書籤。", "neutral");
  } catch (error) {
    showMessage(message, errorMessage(error), "error");
  }
}

signInButton.addEventListener("click", async () => {
  try {
    await signInWithGoogle();
    await render();
  } catch (error) {
    showMessage(message, errorMessage(error), "error");
  }
});

signOutButton.addEventListener("click", async () => {
  await signOutEverywhere();
  await render();
});

void render();
