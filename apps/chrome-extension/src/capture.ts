import { currentUser, saveBookmark, signInWithGoogle } from "./firebase";
import { shouldCloseCapture, type CaptureSource } from "./bookmark";
import { errorMessage, requiredElement, showMessage } from "./ui";

const form = requiredElement<HTMLFormElement>("#capture-form");
const urlInput = requiredElement<HTMLInputElement>("#url");
const noteInput = requiredElement<HTMLTextAreaElement>("#note");
const saveButton = requiredElement<HTMLButtonElement>("#save");
const signInButton = requiredElement<HTMLButtonElement>("#sign-in");
const authLabel = requiredElement<HTMLElement>("#auth-label");
const message = requiredElement<HTMLElement>("#message");
const params = new URLSearchParams(location.search);
const sourceInput = requiredElement<HTMLInputElement>("#source");

urlInput.value = params.get("url") || "";
sourceInput.value = params.get("source") || sourceInput.value || "manual";

async function refreshAuth() {
  const user = await currentUser();
  authLabel.textContent = user?.email || "尚未登入";
  signInButton.hidden = Boolean(user);
}

signInButton.addEventListener("click", async () => {
  signInButton.disabled = true;
  try {
    await signInWithGoogle();
    await refreshAuth();
    showMessage(message, "登入成功，可以儲存連結。", "success");
  } catch (error) {
    showMessage(message, errorMessage(error), "error");
  } finally {
    signInButton.disabled = false;
  }
});

form.addEventListener("submit", async (event) => {
  event.preventDefault();
  saveButton.disabled = true;
  showMessage(message, "正在儲存…");
  try {
    await saveBookmark(urlInput.value, noteInput.value, sourceInput.value as CaptureSource);
    showMessage(message, "已儲存連結。", "success");
    noteInput.value = "";
    if (shouldCloseCapture(sourceInput.value as CaptureSource)) {
      window.close();
    }
  } catch (error) {
    showMessage(message, errorMessage(error), "error");
  } finally {
    saveButton.disabled = false;
  }
});

void refreshAuth();
