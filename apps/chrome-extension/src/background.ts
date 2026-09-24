const MENU_ID = "linkpipe-save-link";

chrome.runtime.onInstalled.addListener(() => {
  chrome.contextMenus.removeAll(() => {
    chrome.contextMenus.create({ id: MENU_ID, title: "儲存連結到 LinkPipe", contexts: ["link"] });
  });
});

chrome.contextMenus.onClicked.addListener((info) => {
  if (info.menuItemId !== MENU_ID || !info.linkUrl) return;
  const query = new URLSearchParams({ url: info.linkUrl, source: "page-link" });
  void chrome.windows.create({
    url: chrome.runtime.getURL(`capture.html?${query}`),
    type: "popup",
    width: 440,
    height: 580,
  });
});
