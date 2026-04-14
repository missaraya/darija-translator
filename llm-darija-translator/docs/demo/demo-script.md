# Demo Script — LLM Darija Translator
**Target duration: 5 minutes**

---

## [0:00–0:30] Introduction

> "This project is an LLM-powered RESTful web service that translates text from English into Moroccan Arabic Dialect, known as Darija.
> The system is built with Java 21 and Jakarta EE on the backend, and has five different clients: a Chrome extension, a PHP web app, a Python Streamlit app, a React Native mobile app, and Postman for API testing.
> Let me walk you through it."

---

## [0:30–1:00] Backend — Quick Tour

**Show in terminal or IDE:**

1. Open `backend/src/main/java/com/translator/`
2. Point to the key classes:
   - `TranslatorApplication.java` — JAX-RS activation with `@ApplicationPath("/api")`
   - `resource/TranslatorResource.java` — Three endpoints: health, info, translate
   - `service/TranslationService.java` — Business logic and validation
   - `llm/GeminiTranslationClient.java` — Calls Google Gemini with a strict prompt

> "The backend is cleanly layered: resource → service → LLM client. Each layer has a single responsibility."

3. Show `security/BasicAuthFilter.java`:

> "Security is handled by a Jakarta REST `ContainerRequestFilter`. It intercepts every request, checks the Base64-decoded Authorization header against the configured credentials, and returns 401 if invalid. The health and info endpoints are public — only the translate endpoint is protected."

---

## [1:00–1:40] Postman Demo

**Switch to Postman:**

1. Select environment: **Darija Translator — Local**
2. Run **Health Check** → show 200 OK with `"status": "UP"`
3. Run **Translate — Success** → show translated Darija text in response
4. Run **Translate — No Auth** → show 401 Unauthorized
5. Run **Translate — Empty Text** → show 400 Bad Request with validation message

> "The API always returns clean JSON, even for errors — thanks to exception mappers."

---

## [1:40–2:10] Chrome Extension Demo

**Switch to Chrome:**

1. Open any article or webpage with English text
2. Select a sentence (e.g., "The weather today is beautiful.")
3. Right-click → **"Translate to Darija"**
4. Side panel opens — the selected text is already in the input box
5. Click **Translate** → show the result in green
6. Click **🔊 Read** to hear it read aloud
7. Click **📋 Copy**

> "The extension uses Manifest V3, chrome.sidePanel API, and chrome.contextMenus. The selected text flows from the context menu click through the background service worker into the side panel via chrome.storage.session."

---

## [2:10–2:35] PHP Client Demo

**Switch to browser tab: http://localhost:9081**

1. Select source: English, target: Moroccan Darija
2. Type: "Good morning! How can I help you?"
3. Click **Translate** → show result

> "This is a plain PHP web app running on PHP's built-in server. It calls the REST endpoint using cURL with HTTP Basic Auth. No framework needed."

---

## [2:35–3:00] Python Client Demo

**Switch to browser tab: http://localhost:8501**

1. Source: English, Target: Moroccan Darija
2. Type: "I love couscous and mint tea."
3. Click **Translate →** → show result

> "The Python client uses Streamlit — a Python web framework perfect for data/AI demos. It uses the `requests` library with HTTPBasicAuth. Three lines to call the API."

---

## [3:00–3:30] React Native Demo

**Switch to Expo Go or Android emulator:**

1. Show the app screen — language chip selectors visible
2. Select English → Moroccan Darija (already selected)
3. Type: "What time is it?"
4. Tap **Translate** → show spinner → show result

> "The mobile app is built with React Native and Expo. It uses axios for the HTTP call, handles all error states, and shows a backend status indicator."

---

## [3:30–4:15] Architecture Diagrams

**Switch to the UML diagrams (PlantUML rendered PNGs or online viewer):**

### Class Diagram
> "The class diagram shows the backend package structure — resource, service, LLM client, DTOs, security filter, exception mappers, and CORS filter — and their relationships."

### Deployment Diagram
> "The deployment diagram shows the full runtime: all five clients connect to Payara Micro on port 9080. The backend then makes an HTTPS call to Google's Gemini API in the cloud."

### Sequence Diagram
> "The sequence diagram shows the full request flow — from any client, through the auth filter, service validation, LLM client call, and back — including all error branches."

---

## [4:15–5:00] Conclusion

> "To summarize:
> - A Jakarta REST backend secured with HTTP Basic Authentication
> - LLM integration via Google Gemini for authentic Moroccan Darija translation
> - Five different client types demonstrating the API
> - Clean JSON error handling at every layer
> - The project is structured as a GitHub-ready monorepo
>
> Thank you. I'm happy to take any questions."

---

## Backup — Things to Know if Asked

| Question | Answer |
|---|---|
| Why Payara Micro? | Jakarta EE reference implementation, easy local run with `mvn package payara-micro:start`, no app server installation needed |
| Why Gemini? | Free API key, high quality, fast, good at dialect translation |
| Why MicroProfile Config? | Reads env vars automatically — GEMINI_API_KEY env var maps to gemini.api.key property |
| How is CORS handled? | A `ContainerResponseFilter` adds Access-Control headers to every response |
| What if Gemini is down? | `TranslationException` is caught, mapped to 502 by `TranslationExceptionMapper` |
| How to change credentials? | Set AUTH_USERNAME and AUTH_PASSWORD env vars before starting the server |
