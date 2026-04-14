# Presentation Notes — Darija Translator

Notes for the presenter to prepare and deliver the demo confidently.

---

## Before the Demo — Preparation Checklist

Do this **30 minutes before** the presentation:

```
[ ] GEMINI_API_KEY is set in your terminal/environment
[ ] Backend is running: mvn package payara-micro:start (wait for "ready" message)
[ ] PHP server is running: php -S localhost:9081 (in php-client/)
[ ] Python client is running: streamlit run app.py (in python-client/)
[ ] React Native is running: npm start (in mobile-client/)
[ ] Chrome extension is loaded at chrome://extensions in Developer Mode
[ ] Postman is open with the correct environment selected
[ ] Tested all 5 clients manually — confirmed translations work
[ ] UML diagrams open in a browser tab or PDF viewer
[ ] Close unnecessary browser tabs and notifications
```

---

## Key Talking Points Per Component

### Backend
- **Jakarta REST (JAX-RS)** — industry-standard Java REST framework, part of Jakarta EE 10
- **Payara Micro** — production-grade Jakarta EE runtime, runs as a single command
- **Layered architecture** — resource / service / LLM client separation for clean maintainability
- **MicroProfile Config** — configuration from env vars, properties file, system props (in that priority order)
- **JSON-P** — Jakarta JSON Processing for safe Gemini API request/response handling

### Security
- **`@PreMatching` filter** — runs before routing, protects all endpoints at infrastructure level
- **No hardcoded secrets** — credentials from config file, overridable by env vars
- **WWW-Authenticate header** — standards-compliant 401 response, triggers browser auth dialog
- **Public paths** — `/health` and `/info` explicitly whitelisted in the filter

### LLM Integration
- **Prompt engineering** — explicit instructions to return ONLY Darija, no explanations
- **Temperature 0.3** — low temperature for deterministic, consistent translations
- **Error handling** — quota, timeout, API key errors all produce clean 502 responses
- **Abstraction** — `GeminiTranslationClient` could be replaced by another provider without touching `TranslationService`

### Chrome Extension
- **Manifest V3** — current standard (V2 is deprecated by Google)
- **chrome.sidePanel** — available in Chrome 116+, persistent panel alongside web content
- **chrome.storage.session** — passes text from background worker to side panel without reload
- **Text-to-speech** — Web Speech API, requests Arabic locale (ar-MA)

### PHP Client
- **cURL** — standard PHP HTTP client, no framework dependency
- **PHP built-in server** — `php -S localhost:9081` — no Apache/Nginx needed for demo

### Python Client
- **Streamlit** — turns a Python script into a web app, perfect for AI/ML demos
- **requests library** — `HTTPBasicAuth` helper handles Base64 encoding automatically

### React Native
- **Expo** — development platform for React Native, enables hot reload and QR code deployment
- **axios** — popular HTTP client for React Native with clean error handling

---

## Common Mistakes to Avoid

| Mistake | Prevention |
|---|---|
| Forgetting to start the backend | Backend startup takes ~30 seconds — do it early |
| Wrong IP in mobile config | Use 10.0.2.2 for Android emulator, local IP for real device |
| API key quota exceeded | Test with a few requests only; use gemini-1.5-flash (higher quota) |
| Chrome extension not reloaded after code change | Hit "Reload" on chrome://extensions page |
| Streamlit port conflict | Kill other Streamlit sessions first |

---

## Anticipated Questions

**Q: Why not use Spring Boot?**
A: The assignment specifies Jakarta RESTful Web Services API. Payara Micro is a native Jakarta EE runtime — it's what JAX-RS was designed to run on. Spring Boot is an alternative but not the spec-native choice.

**Q: Is this production-ready?**
A: Not fully — production would need HTTPS, a more robust credential store, rate limiting, and logging infrastructure. But the architecture is sound and all the pieces are cleanly wired.

**Q: Why Darija specifically?**
A: Moroccan Darija is underrepresented in NLP tools — most translators target Modern Standard Arabic (Fusha), which sounds formal and unnatural to Moroccan speakers. Darija has unique vocabulary mixing Arabic, Berber, French, and Spanish influences. The LLM is prompted to use authentic Darija, not Fusha.

**Q: What happens if the Gemini API is down?**
A: `GeminiTranslationClient` catches all exceptions and throws a `TranslationException`. The `TranslationExceptionMapper` converts this to a 502 response with a clear JSON error message. No HTML error pages — always clean JSON.

**Q: How are the credentials stored securely?**
A: In `microprofile-config.properties` for local dev (demo defaults). In production, they would be environment variables (`AUTH_USERNAME`, `AUTH_PASSWORD`) injected at container startup — never committed to source control.
