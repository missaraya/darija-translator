# LLM Darija Translator

An LLM-powered RESTful web service for translating text from English (or any language) into **Moroccan Arabic Dialect (Darija)** — backed by Google Gemini AI.

Built as a complete full-stack project with a Java/Jakarta EE REST backend, secured with HTTP Basic Authentication, and five different client applications.

---

## Features

- **Jakarta REST backend** — JAX-RS resource, CDI service layer, Gemini LLM client
- **HTTP Basic Authentication** — protects the translate endpoint; health/info are public
- **Google Gemini AI** — prompt-engineered to return authentic Darija (not Fusha/Standard Arabic)
- **Chrome Extension** — Manifest V3, right-click context menu, side panel, TTS read-aloud
- **PHP client** — cURL-based web app with a clean UI
- **Python client** — Streamlit app with language selectors
- **React Native mobile app** — Expo, works on Android, iOS, and web
- **Postman collection** — 9 test requests covering all scenarios
- **PlantUML diagrams** — class, deployment, and sequence diagrams
- **CORS support** — all clients can call the backend from any origin (local dev)

---

## Tech Stack

| Layer          | Technology                        |
|----------------|-----------------------------------|
| Backend        | Java 21, Jakarta EE 10, JAX-RS    |
| Runtime        | Payara Micro 6                    |
| Config         | MicroProfile Config               |
| LLM Provider   | Google Gemini (gemini-1.5-flash)  |
| Security       | HTTP Basic Auth (ContainerRequestFilter) |
| JSON           | Jakarta JSON-B (request/response) + JSON-P (Gemini parsing) |
| Chrome Ext     | Manifest V3, chrome.sidePanel, chrome.contextMenus |
| PHP Client     | PHP 7.4+, cURL                    |
| Python Client  | Streamlit, requests               |
| Mobile Client  | React Native, Expo, axios         |
| Build          | Maven 3.9+                        |
| API Testing    | Postman                           |
| Diagrams       | PlantUML                          |

---

## Monorepo Structure

```
llm-darija-translator/
├── README.md                          ← This file
├── .gitignore
├── .env.example                       ← Copy to .env and fill in your API key
│
├── backend/                           ← Java REST service (Payara Micro)
│   ├── pom.xml
│   ├── README.md
│   └── src/main/java/com/translator/
│       ├── TranslatorApplication.java
│       ├── config/AppConfig.java
│       ├── dto/{TranslationRequest,TranslationResponse,ErrorResponse}.java
│       ├── exception/{ValidationException,TranslationException,*Mapper}.java
│       ├── filter/CorsFilter.java
│       ├── llm/GeminiTranslationClient.java
│       ├── resource/TranslatorResource.java
│       ├── security/BasicAuthFilter.java
│       └── service/TranslationService.java
│
├── chrome-extension/                  ← Chrome MV3 side panel extension
│   ├── manifest.json
│   ├── background.js
│   ├── content.js
│   ├── sidepanel.html
│   ├── sidepanel.js
│   ├── styles.css
│   ├── icons/generate_icons.py
│   └── README.md
│
├── php-client/                        ← PHP web app (cURL)
│   ├── config.php
│   ├── index.php
│   ├── translator.php
│   ├── style.css
│   └── README.md
│
├── python-client/                     ← Streamlit web app
│   ├── app.py
│   ├── requirements.txt
│   ├── .env.example
│   └── README.md
│
├── mobile-client/                     ← React Native + Expo (single screen)
│   ├── App.js
│   ├── app.json
│   ├── package.json
│   ├── babel.config.js
│   ├── src/
│   │   ├── config/config.js           ← Backend URL + credentials
│   │   ├── api/translatorApi.js       ← axios translate + checkHealth
│   │   └── screens/TranslatorScreen.js
│   └── README.md
│
├── postman/
│   ├── TranslatorService.postman_collection.json
│   ├── TranslatorService.postman_environment.json
│   └── README.md
│
└── docs/
    ├── architecture/
    │   ├── class-diagram.puml
    │   ├── deployment-diagram.puml
    │   ├── sequence-diagram.puml
    │   └── README.md
    ├── demo/
    │   ├── demo-script.md
    │   └── presentation-notes.md
    └── test-checklist.md
```

---

## Prerequisites

| Tool            | Version | Notes                                    |
|-----------------|---------|------------------------------------------|
| Java            | 21+     | `java -version`                          |
| Maven           | 3.9+    | `mvn -version`                           |
| PHP             | 7.4+    | `php -v` — cURL extension must be enabled|
| Python          | 3.9+    | `python --version`                       |
| Node.js         | 18+     | `node --version`                         |
| Chrome          | 116+    | For Manifest V3 + sidePanel API          |
| Postman Desktop | any     | For API testing                          |
| Internet access | —       | First Payara Micro run downloads ~60 MB  |

---

## Environment Variables

Copy `.env.example` to `.env` and fill in your values:

```
cp .env.example .env
```

| Variable         | Required | Default      | How to set                      |
|------------------|----------|--------------|---------------------------------|
| `GEMINI_API_KEY` | **YES**  | —            | `export GEMINI_API_KEY=...`     |
| `AUTH_USERNAME`  | No       | `student`    | `export AUTH_USERNAME=...`      |
| `AUTH_PASSWORD`  | No       | `student123` | `export AUTH_PASSWORD=...`      |

**Get a free Gemini API key:** https://aistudio.google.com/app/apikey

---

## Running the Backend

```bash
# 1. Set your Gemini API key (required!)
export GEMINI_API_KEY=your-actual-key-here       # Linux / macOS
# set GEMINI_API_KEY=your-actual-key-here        # Windows CMD
# $env:GEMINI_API_KEY = "your-actual-key-here"   # Windows PowerShell

# 2. Build and start
cd backend
mvn package payara-micro:start
```

Wait for: `Payara Micro ... ready in ...ms`

The server runs at **http://localhost:9080**

**Quick verification:**
```bash
curl http://localhost:9080/api/translator/health
# {"status":"UP","service":"Darija Translator","version":"1.0.0"}
```

---

## API Endpoints

| Method | Path                        | Auth     | Description            |
|--------|-----------------------------|----------|------------------------|
| GET    | /api/translator/health      | Public   | Health check           |
| GET    | /api/translator/info        | Public   | Service metadata       |
| POST   | /api/translator/translate   | Basic    | Translate text to Darija |

**Translate request:**
```json
{
  "text": "How are you today?",
  "sourceLanguage": "English",
  "targetLanguage": "Moroccan Darija"
}
```

**Translate response:**
```json
{
  "originalText": "How are you today?",
  "translatedText": "كيداير اليوم؟",
  "sourceLanguage": "English",
  "targetLanguage": "Moroccan Darija",
  "provider": "Gemini",
  "status": "success"
}
```

---

## Postman Testing

1. Import `postman/TranslatorService.postman_collection.json`
2. Import `postman/TranslatorService.postman_environment.json`
3. Select environment **"Darija Translator — Local"**
4. Run all 9 requests — see `postman/README.md` for expected results

---

## Running Each Client

### Chrome Extension
```bash
cd chrome-extension/icons
pip install Pillow
python generate_icons.py       # creates icon16/48/128.png

# Then in Chrome:
# 1. chrome://extensions → Enable Developer Mode
# 2. Load Unpacked → select chrome-extension/ folder
```

### PHP Client
```bash
cd php-client
php -S localhost:9081
# Open http://localhost:9081
```

### Python Client
```bash
cd python-client
pip install -r requirements.txt
streamlit run app.py
# Opens http://localhost:8501 automatically
```

### React Native (Expo)
```bash
# Step 1 — generate app asset images (one time only)
cd mobile-client/assets
pip install Pillow
python generate_assets.py

# Step 2 — install and run
cd ..
npm install
npm start
# Press 'a' for Android emulator, 'i' for iOS, 'w' for web
# Or scan QR code with Expo Go app on a physical device
```

> **Note for Android emulator:** Edit `src/config/config.js` — change `BACKEND_URL` to `http://10.0.2.2:9080`
> **Note for physical device:** Use your machine's local IP: `http://192.168.x.x:9080`

---

## Architecture Diagrams

PlantUML source files are in `docs/architecture/`.

Render them online at https://www.plantuml.com/plantuml/uml/ or with the VS Code PlantUML extension.

See `docs/architecture/README.md` for full render instructions.

---

## Security Notes

- The `/translate` endpoint requires `Authorization: Basic <base64(user:pass)>` header
- Default credentials: `student` / `student123` (for demo only)
- Override with `AUTH_USERNAME` and `AUTH_PASSWORD` environment variables
- The Gemini API key is read from the `GEMINI_API_KEY` environment variable — never committed to source
- CORS is open (`*`) for local development — restrict origins in production

---

## Known Limitations

- Basic Auth transmits credentials on every request — use HTTPS in production
- No database — credentials are config-only
- No rate limiting — add throttling before exposing publicly
- Gemini free tier: 15 requests/minute — sufficient for a demo
- React Native web doesn't support `Clipboard` API fully on all browsers

---

## Future Improvements

- [ ] Add Jakarta Security with a proper Identity Store
- [ ] Support more Maghrebi dialects (Algerian Darija, Tunisian)
- [ ] Add request caching (MicroProfile Cache or Redis)
- [ ] Add structured logging with OpenTelemetry
- [ ] Add JWT-based authentication for the mobile/extension clients
- [ ] Add speech-to-text input in the Chrome extension
- [ ] Docker Compose setup for one-command startup of all services

---

## GitHub Submission Checklist

```
[ ] All source files committed (no missing files)
[ ] .gitignore covers target/, node_modules/, .env, __pycache__/
[ ] .env.example present (no real secrets)
[ ] README.md complete with run instructions
[ ] backend/README.md present
[ ] chrome-extension/README.md present
[ ] php-client/README.md present
[ ] python-client/README.md present
[ ] mobile-client/README.md present
[ ] postman/README.md present
[ ] Postman collection + environment committed
[ ] UML .puml files committed
[ ] docs/demo/ files committed
[ ] docs/test-checklist.md committed
[ ] No hardcoded API keys or passwords anywhere
[ ] All clients tested against live backend
```

---

*Built with Java 21 · Jakarta EE 10 · Payara Micro 6 · Google Gemini · Chrome MV3 · PHP · Streamlit · React Native*
