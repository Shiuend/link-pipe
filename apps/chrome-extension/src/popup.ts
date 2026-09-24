import "./capture";

const libraryButton = document.querySelector<HTMLButtonElement>("#open-library");
libraryButton?.addEventListener("click", () => void chrome.tabs.create({ url: chrome.runtime.getURL("library.html") }));

const urlInput = document.querySelector<HTMLInputElement>("#url");
const sourceInput = document.querySelector<HTMLInputElement>("#source");
document.querySelector<HTMLButtonElement>("#use-current")?.addEventListener("click", async () => {
  const [tab] = await chrome.tabs.query({ active: true, currentWindow: true });
  if (tab?.url?.startsWith("http") && urlInput && sourceInput) {
    urlInput.value = tab.url;
    sourceInput.value = "current-page";
  }
});
document.querySelector<HTMLButtonElement>("#use-manual")?.addEventListener("click", () => {
  if (urlInput && sourceInput) {
    urlInput.value = "";
    sourceInput.value = "manual";
    urlInput.focus();
  }
});
document.querySelector<HTMLButtonElement>("#use-current")?.click();
