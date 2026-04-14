/**
 * content.js — Content script injected into every page.
 *
 * Currently minimal — the selected text is already available in the background
 * service worker via info.selectionText in the contextMenus.onClicked handler.
 *
 * This file is kept in case future enhancements need direct DOM interaction
 * (e.g., highlighting translated text on the page).
 */

// Listen for messages from the side panel or background worker if needed
chrome.runtime.onMessage.addListener((message, sender, sendResponse) => {
  if (message.type === "GET_SELECTION") {
    const selection = window.getSelection()?.toString().trim() ?? "";
    sendResponse({ text: selection });
  }
  return true; // Keep the message channel open for async responses
});
