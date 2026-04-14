/**
 * translatorApi.js — API functions for the Darija Translator backend.
 *
 * All functions return a promise that resolves to a plain object.
 * Errors are thrown as Error instances with a human-readable message.
 */

import axios from "axios";
import Config from "../config/config";

const apiClient = axios.create({
  headers: {
    "Content-Type": "application/json",
    Accept:         "application/json",
  },
  timeout: 30000, // 30 seconds
});

/**
 * Translates text using the backend REST service.
 *
 * @param {string} text           - Text to translate.
 * @param {string} sourceLanguage - e.g. "English"
 * @param {string} targetLanguage - e.g. "Moroccan Darija"
 * @returns {Promise<{originalText, translatedText, sourceLanguage, targetLanguage, provider, status}>}
 */
export async function translate(text, sourceLanguage = "English", targetLanguage = "Moroccan Darija") {
  try {
    const response = await apiClient.post(
      Config.TRANSLATE_URL,
      { text, sourceLanguage, targetLanguage },
      { headers: { Authorization: Config.AUTH_HEADER } }
    );
    return response.data;
  } catch (error) {
    throw parseApiError(error);
  }
}

/**
 * Checks if the backend is reachable.
 *
 * @returns {Promise<boolean>}
 */
export async function checkHealth() {
  try {
    const response = await apiClient.get(Config.HEALTH_URL, { timeout: 5000 });
    return response.status === 200 && response.data?.status === "UP";
  } catch {
    return false;
  }
}

// --------------------------------------------------------------------------
// Error parsing helper
// --------------------------------------------------------------------------
function parseApiError(error) {
  if (error.response) {
    const status = error.response.status;
    const data   = error.response.data;

    if (status === 401) {
      return new Error("Authentication failed. Check username and password in config.js.");
    }
    if (status === 400) {
      return new Error(data?.message || "Validation error — check your input.");
    }
    if (status === 502) {
      return new Error(data?.message || "Translation service unavailable. Check your Gemini API key.");
    }
    return new Error(data?.message || `Server error (HTTP ${status})`);
  }

  if (error.code === "ECONNABORTED") {
    return new Error("Request timed out. The server may be slow.");
  }
  if (error.message?.includes("Network Error") || error.code === "ERR_NETWORK") {
    return new Error(
      `Cannot reach the backend at ${Config.BACKEND_URL}.\n` +
      "Make sure the server is running and the IP address in config.js is correct."
    );
  }

  return new Error(error.message || "An unexpected error occurred.");
}
