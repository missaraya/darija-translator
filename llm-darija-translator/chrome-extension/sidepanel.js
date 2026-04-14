/**
 * sidepanel.js — Logic for the Darija Translator side panel.
 *
 * Features:
 *  - Loads settings (URL, credentials) from chrome.storage.local
 *  - Polls chrome.storage.session for text forwarded from the context menu
 *  - Calls the backend REST endpoint with Basic Auth
 *  - Displays translation with copy and text-to-speech buttons
 *  - Settings panel for configuring backend URL and credentials
 */

"use strict";

// --------------------------------------------------------------------------
// Default configuration
// --------------------------------------------------------------------------
const DEFAULTS = {
  backendUrl: "http://localhost:8080",
  username:   "student",
  password:   "student123"
};

// --------------------------------------------------------------------------
// DOM references
// --------------------------------------------------------------------------
const sourceTextEl    = document.getElementById("source-text");
const btnTranslate    = document.getElementById("btn-translate");
const btnClear        = document.getElementById("btn-clear");
const btnCopy         = document.getElementById("btn-copy");
const btnSpeak        = document.getElementById("btn-speak");
const btnSettings     = document.getElementById("btn-settings");
const btnSaveSettings = document.getElementById("btn-save-settings");
const settingsPanel   = document.getElementById("settings-panel");
const mainPanel       = document.getElementById("main-panel");
const loadingEl       = document.getElementById("loading");
const errorBoxEl      = document.getElementById("error-box");
const resultSectionEl = document.getElementById("result-section");
const resultTextEl    = document.getElementById("result-text");
const statusBar       = document.getElementById("status-bar");
const settingUrl      = document.getElementById("setting-url");
const settingUsername = document.getElementById("setting-username");
const settingPassword = document.getElementById("setting-password");
const settingsStatus  = document.getElementById("settings-status");

// --------------------------------------------------------------------------
// State
// --------------------------------------------------------------------------
let settings = { ...DEFAULTS };

// --------------------------------------------------------------------------
// Initialise
// --------------------------------------------------------------------------
async function init() {
  await loadSettings();
  await checkForPendingText();
}

async function loadSettings() {
  const stored = await chrome.storage.local.get(["backendUrl", "username", "password"]);
  settings = {
    backendUrl: stored.backendUrl || DEFAULTS.backendUrl,
    username:   stored.username   || DEFAULTS.username,
    password:   stored.password   || DEFAULTS.password
  };
  // Populate settings fields
  settingUrl.value      = settings.backendUrl;
  settingUsername.value = settings.username;
  settingPassword.value = settings.password;
}

/**
 * Check if text was forwarded from the context menu (background service worker
 * stores it in chrome.storage.session as "pendingText").
 */
async function checkForPendingText() {
  const data = await chrome.storage.session.get("pendingText");
  if (data.pendingText) {
    sourceTextEl.value = data.pendingText;
    // Clear pending text so it's not loaded again if panel is reopened
    await chrome.storage.session.remove("pendingText");
    setStatus("Text loaded from selection. Click Translate.");
  }
}

// --------------------------------------------------------------------------
// Translation
// --------------------------------------------------------------------------
async function translate() {
  const text = sourceTextEl.value.trim();
  if (!text) {
    showError("Please enter some text to translate.");
    return;
  }

  setLoading(true);
  hideError();
  hideResult();

  try {
    const response = await callTranslateAPI(text);
    showResult(response.translatedText);
    setStatus("Translation complete ✓");
  } catch (err) {
    showError(err.message);
    setStatus("Error — see message above");
  } finally {
    setLoading(false);
  }
}

async function callTranslateAPI(text) {
  const url      = `${settings.backendUrl}/api/translator/translate`;
  const authHeader = "Basic " + btoa(`${settings.username}:${settings.password}`);

  const response = await fetch(url, {
    method:  "POST",
    headers: {
      "Content-Type":  "application/json",
      "Authorization": authHeader
    },
    body: JSON.stringify({
      text:           text,
      sourceLanguage: "English",
      targetLanguage: "Moroccan Darija"
    })
  });

  const data = await response.json();

  if (response.status === 401) {
    throw new Error("Authentication failed. Check username and password in Settings.");
  }
  if (!response.ok) {
    throw new Error(data.message || `Server error (HTTP ${response.status})`);
  }

  return data;
}

// --------------------------------------------------------------------------
// Copy and Text-to-Speech
// --------------------------------------------------------------------------
function copyTranslation() {
  const text = resultTextEl.textContent;
  if (!text) return;
  navigator.clipboard.writeText(text).then(() => {
    btnCopy.textContent = "✅ Copied!";
    setTimeout(() => (btnCopy.textContent = "📋 Copy"), 2000);
  });
}

function speakTranslation() {
  const text = resultTextEl.textContent;
  if (!text) return;

  // Cancel any ongoing speech
  window.speechSynthesis.cancel();

  const utterance = new SpeechSynthesisUtterance(text);
  utterance.lang  = "ar-MA"; // Moroccan Arabic locale
  utterance.rate  = 0.85;

  // Try to find an Arabic voice
  const voices = window.speechSynthesis.getVoices();
  const arabicVoice = voices.find(v => v.lang.startsWith("ar"));
  if (arabicVoice) utterance.voice = arabicVoice;

  window.speechSynthesis.speak(utterance);
  setStatus("Reading aloud…");
  utterance.onend = () => setStatus("Done");
}

// --------------------------------------------------------------------------
// Settings
// --------------------------------------------------------------------------
function toggleSettings() {
  const isHidden = settingsPanel.classList.contains("hidden");
  settingsPanel.classList.toggle("hidden", !isHidden);
  mainPanel.classList.toggle("hidden", isHidden);
}

async function saveSettings() {
  const newSettings = {
    backendUrl: settingUrl.value.trim()      || DEFAULTS.backendUrl,
    username:   settingUsername.value.trim() || DEFAULTS.username,
    password:   settingPassword.value        || DEFAULTS.password
  };
  await chrome.storage.local.set(newSettings);
  settings = newSettings;

  settingsStatus.textContent = "Settings saved!";
  settingsStatus.classList.remove("hidden");
  setTimeout(() => settingsStatus.classList.add("hidden"), 2000);
}

// --------------------------------------------------------------------------
// UI helpers
// --------------------------------------------------------------------------
function setLoading(on) {
  loadingEl.classList.toggle("hidden", !on);
  btnTranslate.disabled = on;
}

function showError(msg) {
  errorBoxEl.textContent = "⚠ " + msg;
  errorBoxEl.classList.remove("hidden");
}

function hideError() {
  errorBoxEl.classList.add("hidden");
}

function showResult(text) {
  resultTextEl.textContent = text;
  resultSectionEl.classList.remove("hidden");
}

function hideResult() {
  resultSectionEl.classList.add("hidden");
}

function setStatus(msg) {
  statusBar.textContent = msg;
}

function clearAll() {
  sourceTextEl.value = "";
  hideError();
  hideResult();
  setStatus("Ready");
}

// --------------------------------------------------------------------------
// Event listeners
// --------------------------------------------------------------------------
btnTranslate.addEventListener("click", translate);
btnClear.addEventListener("click", clearAll);
btnCopy.addEventListener("click", copyTranslation);
btnSpeak.addEventListener("click", speakTranslation);
btnSettings.addEventListener("click", toggleSettings);
btnSaveSettings.addEventListener("click", saveSettings);

// Allow Ctrl+Enter to trigger translation
sourceTextEl.addEventListener("keydown", (e) => {
  if (e.ctrlKey && e.key === "Enter") translate();
});

// Listen for storage changes in case background updates pendingText while panel is open
chrome.storage.onChanged.addListener((changes, area) => {
  if (area === "session" && changes.pendingText?.newValue) {
    sourceTextEl.value = changes.pendingText.newValue;
    chrome.storage.session.remove("pendingText");
    setStatus("Text updated from selection.");
  }
});

// --------------------------------------------------------------------------
// Bootstrap
// --------------------------------------------------------------------------
init();
