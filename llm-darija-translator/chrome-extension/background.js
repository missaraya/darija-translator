/**
 * background.js — Service Worker for the Darija Translator extension.
 *
 * Responsibilities:
 *  1. Create the "Translate to Darija" context menu item.
 *  2. When the user clicks it: open the side panel and forward the selected text.
 *  3. Store selected text in chrome.storage.session so the side panel can read it.
 */

// --------------------------------------------------------------------------
// Context menu setup — runs once when the extension installs / reloads
// --------------------------------------------------------------------------
chrome.runtime.onInstalled.addListener(() => {
  chrome.contextMenus.create({
    id: "translate-to-darija",
    title: "Translate to Darija",
    contexts: ["selection"]   // Only show when text is selected
  });
  console.log("[BG] Darija Translator context menu created.");
});

// --------------------------------------------------------------------------
// Handle context menu click
// --------------------------------------------------------------------------
chrome.contextMenus.onClicked.addListener(async (info, tab) => {
  if (info.menuItemId !== "translate-to-darija") return;

  const selectedText = info.selectionText?.trim() ?? "";
  console.log("[BG] Context menu clicked, selected text:", selectedText.substring(0, 80));

  // Store selected text so the side panel can pick it up after opening
  await chrome.storage.session.set({ pendingText: selectedText });

  // Open the side panel for the current window
  await chrome.sidePanel.open({ windowId: tab.windowId });
});

// --------------------------------------------------------------------------
// Allow toolbar button click to open the side panel too
// --------------------------------------------------------------------------
chrome.action.onClicked.addListener(async (tab) => {
  await chrome.sidePanel.open({ windowId: tab.windowId });
});
