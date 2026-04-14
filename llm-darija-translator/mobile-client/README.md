# Mobile Client — Darija Translator

React Native + Expo — runs on Android, iOS, and web.

---

## Prerequisites

- Node.js 18+
- For physical device: **Expo Go** app (iOS App Store / Google Play)
- The backend running at `http://localhost:9080`

---

## Setup and Run

### Step 1 — Generate app assets (one time only)

```bash
cd mobile-client/assets
pip install Pillow
python generate_assets.py
```

This creates `icon.png`, `splash.png`, `adaptive-icon.png`, and `favicon.png` which are referenced in `app.json`.

### Step 2 — Install dependencies and start

```bash
cd mobile-client
npm install
npm start          # or: npx expo start
```

The Expo dev server starts and shows a QR code.

### Run options:
- **Android emulator:** Press `a` in the terminal
- **iOS simulator (macOS only):** Press `i`
- **Physical device:** Scan the QR code with Expo Go
- **Web browser:** Press `w`

---

## Configuration

Edit `src/config/config.js`:

```js
const Config = {
  BACKEND_URL:   "http://localhost:9080",   // ← change this
  AUTH_USERNAME: "student",
  AUTH_PASSWORD: "student123",
};
```

### Important — IP Address for Device Testing

| Platform                    | Use this URL                     |
|-----------------------------|----------------------------------|
| Android emulator            | `http://10.0.2.2:9080`           |
| iOS simulator               | `http://localhost:9080`          |
| Expo Go on real device      | `http://192.168.x.x:9080` (your machine's local IP) |
| Expo web                    | `http://localhost:9080`          |

Find your local IP:
- Windows: `ipconfig`
- macOS/Linux: `ifconfig` or `ip addr`

---

## Project Structure

```
mobile-client/
├── App.js                        — Root component (renders TranslatorScreen)
├── app.json                      — Expo config
├── package.json
├── babel.config.js
├── src/
│   ├── config/config.js          — Backend URL and credentials
│   ├── api/translatorApi.js      — axios API functions (translate, checkHealth)
│   └── screens/TranslatorScreen.js — Single screen UI
└── README.md
```
