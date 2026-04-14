/**
 * config.js — App configuration for the React Native client.
 *
 * IMPORTANT — which URL to use:
 *  - Expo web (browser):        http://localhost:9080
 *  - iOS simulator (macOS):     http://localhost:9080
 *  - Android emulator:          http://10.0.2.2:9080
 *  - Physical device (Expo Go): http://<YOUR_LOCAL_IP>:9080
 *    Find your IP: `ipconfig` (Windows) or `ifconfig` (Mac/Linux)
 *
 * Edit BACKEND_URL below to match your setup.
 */

const BACKEND_URL   = "http://localhost:9080";
const AUTH_USERNAME = "student";
const AUTH_PASSWORD = "student123";

/**
 * Encode Basic Auth credentials.
 * btoa() is available globally in React Native 0.70+ and Expo SDK 51+.
 */
function makeBasicAuthHeader(username, password) {
  const token = btoa(`${username}:${password}`);
  return `Basic ${token}`;
}

const Config = {
  BACKEND_URL,
  AUTH_USERNAME,
  AUTH_PASSWORD,

  TRANSLATE_URL: `${BACKEND_URL}/api/translator/translate`,
  HEALTH_URL:    `${BACKEND_URL}/api/translator/health`,
  AUTH_HEADER:   makeBasicAuthHeader(AUTH_USERNAME, AUTH_PASSWORD),
};

export default Config;
