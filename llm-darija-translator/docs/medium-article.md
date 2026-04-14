# Building an LLM-Powered REST API for Moroccan Darija: A Full-Stack Deep Dive

*How I connected Google Gemini to a Jakarta EE backend, secured it with Basic Auth, and built five different clients — from a Chrome extension to a React Native app.*

---

## The Idea

Moroccan Darija is one of the most linguistically rich and underserved dialects in the Arabic-speaking world. Unlike Modern Standard Arabic (Fusha), Darija is a blend of Arabic, Berber, French, and Spanish — and most translation tools either ignore it or confuse it with Standard Arabic.

For my software engineering course, I decided to build something that addressed this gap: a full-stack LLM-powered translation service that specifically targets Darija, backed by a proper REST API, and consumed by five different client applications in four different languages.

The result: **llm-darija-translator** — a Jakarta EE REST backend running on Payara Micro, powered by Google Gemini, with clients in Chrome (MV3 extension), PHP, Python (Streamlit), React Native (Expo), and Postman.

---

## Architecture Overview

The system follows a classic hub-and-spoke model. One backend, multiple clients:

```
Chrome Extension ──┐
PHP Client        ──┤
Python Client     ──┼──▶ Jakarta REST API (Payara Micro) ──▶ Gemini API
React Native      ──┤         ↕ Basic Auth
Postman           ──┘
```

The backend exposes three endpoints:

| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| GET | `/api/translator/health` | Public | Health check |
| GET | `/api/translator/info` | Public | Service metadata |
| POST | `/api/translator/translate` | Basic Auth | Translate text |

Everything is JSON. Clients send a `text`, `sourceLanguage`, and `targetLanguage`, and get back the translated text, the provider name, and a status field.

---

## The Backend: Jakarta EE 10 on Payara Micro

I chose **Jakarta EE 10 + Payara Micro 6** deliberately. Payara Micro is a fat-jar runtime for Jakarta EE — you get the full CDI, JAX-RS, and MicroProfile stack without any application server setup. One Maven command starts the whole thing:

```bash
mvn package payara-micro:start
```

The backend layer architecture is clean and separated:

- `TranslatorResource` — JAX-RS resource, handles HTTP only
- `TranslationService` — business logic, validation, defaults
- `GeminiTranslationClient` — all Gemini API communication
- `BasicAuthFilter` — authentication, completely separate from the resource
- `AppConfig` — MicroProfile Config, reads from env vars

This separation meant I could swap the LLM provider (say, from Gemini to OpenAI) by only touching `GeminiTranslationClient` — nothing else in the stack would change.

---

## The Hardest Part: Prompt Engineering for Darija

Getting Gemini to return *authentic Darija* — not Fusha — was the biggest challenge of this project.

My first attempts were naive:

```
"Translate this to Moroccan Arabic: Hello, how are you?"
```

Gemini would return perfectly correct Modern Standard Arabic every single time. Understandable — it is what most training data has. But Darija speakers do not talk like a formal news broadcast.

After a lot of iteration, I landed on a strict system instruction block sent with every request:

```java
private static final String TRANSLATION_INSTRUCTIONS = """
    You are a professional translator specializing in Moroccan Arabic Dialect (Darija).

    Rules you MUST follow without exception:
    - Return ONLY the translated text. Nothing else.
    - Do NOT add explanations, notes, or commentary.
    - Do NOT add transliteration beside the Arabic text.
    - Do NOT add a prefix like "Translation:" or "Result:".
    - Use authentic Darija vocabulary, not Modern Standard Arabic (Fusha).
    - Preserve the natural meaning as a native speaker would say it.
    """;
```

Three things made a big difference:

1. **Saying "Darija" was not enough** — I had to explicitly say *not* Fusha. The model needed the negative constraint to actually shift registers.
2. **Stripping output decorations** — Without the "no prefix" rules, Gemini would wrap every translation in "Here is the translation: ..." which broke all my JSON parsing.
3. **Low temperature (0.3)** — Higher creativity settings produced inconsistent dialect choices. At 0.3, outputs were stable and consistent.

---

## Security: BasicAuthFilter as a Pre-Matching Filter

I implemented HTTP Basic Auth as a Jakarta `ContainerRequestFilter` with `@PreMatching` — meaning it runs before JAX-RS route matching. This keeps the resource class completely clean of security annotations.

The filter handles three cases:

**1. CORS preflight passes through unconditionally:**

```java
if ("OPTIONS".equalsIgnoreCase(method)) {
    return; // browsers never send credentials with preflight
}
```

This was a real gotcha. Without this, the Chrome extension, PHP client, and Streamlit app would all fail with cryptic CORS errors — because browsers send a preflight `OPTIONS` request *before* the real request, and they never attach `Authorization` headers to it.

**2. Public paths bypass auth:**

```java
for (String publicPath : PUBLIC_PATHS) {
    if (path.endsWith(publicPath)) {
        return;
    }
}
```

Health and info endpoints are unauthenticated — clients use health to check if the backend is running before attempting a translation.

**3. Credentials are validated and rejected with a proper 401:**

```java
ctx.abortWith(
    Response.status(Response.Status.UNAUTHORIZED)
        .header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"DarijaTranslator\"")
        .entity(error)
        .build()
);
```

The `WWW-Authenticate` header tells browsers and Postman that Basic Auth is expected, which triggers the correct prompting behavior.

---

## Five Clients, Four Languages

One of the assignment requirements was to build multiple clients. I treated this as an opportunity to show how the same clean REST API is consumed differently across ecosystems.

### Chrome Extension (Manifest V3)

The extension has three parts: a service worker (`background.js`), a content script (`content.js`), and a side panel (`sidepanel.js`).

The most interesting flow is the right-click context menu to side panel pipeline. When a user selects text on any webpage and right-clicks "Translate to Darija", the background service worker captures it and stores it in `chrome.storage.session`:

```js
chrome.contextMenus.onClicked.addListener(async (info) => {
    if (info.selectionText) {
        await chrome.storage.session.set({ pendingText: info.selectionText });
        chrome.sidePanel.open({ windowId: tab.windowId });
    }
});
```

The side panel then reads `chrome.storage.session` on init and auto-populates the input. This handoff between the background context and the panel context through session storage was a pattern I had to piece together from the MV3 migration guide — it is not well documented.

The extension also implements **Text-to-Speech** using the Web Speech API with Moroccan Arabic locale:

```js
const utterance = new SpeechSynthesisUtterance(text);
utterance.lang = "ar-MA";
utterance.rate = 0.85;
```

### Python Client (Streamlit)

Streamlit made the Python client surprisingly fast to build. The backend health check runs on app startup and disables the translate button if the server is down — a small UX detail that prevents confusing timeout errors.

The trickiest part was handling **right-to-left text rendering** for Darija output. I used `direction: auto` and `unicode-bidi: plaintext` in the CSS — this lets the browser detect text direction automatically, so Arabic text renders RTL while Latin text stays LTR, without any explicit logic.

### PHP Client

The PHP client uses raw `cURL` — no frameworks, no Composer packages. It is the most transparent demonstration of what is happening at the HTTP level:

```php
curl_setopt_array($ch, [
    CURLOPT_POST       => true,
    CURLOPT_POSTFIELDS => $payload,
    CURLOPT_HTTPHEADER => [
        'Content-Type: application/json',
        'Authorization: Basic ' . base64_encode(AUTH_USERNAME . ':' . AUTH_PASSWORD),
    ],
]);
```

Base64 encoding credentials manually, setting the content type, reading the response — this is exactly what every other client is doing under the hood, just more visibly.

### React Native (Expo)

The mobile client surfaced two platform-specific challenges:

**Network address routing.** On a physical device or Android emulator, `localhost` does not resolve to your development machine. Android emulator maps `10.0.2.2` to the host machine, and physical devices need the LAN IP. I handled this with a config file:

```js
// src/config/config.js
export default {
    BACKEND_URL: "http://localhost:9080", // change to 10.0.2.2 for emulator
};
```

**Keyboard avoidance.** On iOS, the software keyboard covers input fields if you do not handle it. `KeyboardAvoidingView` with `behavior="padding"` on iOS and `behavior="height"` on Android handles this — the behaviors differ between platforms because of how each OS manages window resizing.

---

## Technical Challenges

**The CORS maze.** Every client ran into CORS at some point. The solution was a `CorsFilter` that adds the appropriate headers to every response, combined with the unconditional OPTIONS passthrough in `BasicAuthFilter`. The order matters — if auth ran before CORS on a preflight, the 401 response would not have CORS headers, which the browser silently swallows.

**Gemini response parsing.** The Gemini API returns a deeply nested JSON structure. I used Jakarta JSON-P to navigate it:

```java
String translatedText = root
    .getJsonArray("candidates")
    .getJsonObject(0)
    .getJsonObject("content")
    .getJsonArray("parts")
    .getJsonObject(0)
    .getString("text")
    .trim();
```

This is brittle — if Gemini returns an empty candidates array (which happens when the safety filter blocks a request), it throws. I wrapped the entire parse block with exception handling that surfaces a meaningful error rather than a raw NullPointerException.

**MicroProfile Config and environment variables.** Reading config through MicroProfile Config meant I could override any setting with an environment variable without touching the code:

```properties
# microprofile-config.properties
gemini.api.key=not-configured
auth.username=student
auth.password=student123
```

Setting `GEMINI_API_KEY` in the environment overrides `gemini.api.key` automatically. This made the project portable across environments with zero code changes.

---

## What I Would Do Differently

**JWT instead of Basic Auth.** Basic Auth sends credentials on every request. For a demo this is fine, but for production, a short-lived JWT issued on login is far better — especially for the Chrome extension and mobile app, where you want token refresh without the user re-entering credentials.

**Request caching.** If someone translates "Hello" to Darija twice, it hits Gemini twice. A simple in-memory cache keyed on `(text, source, target)` would cut API calls dramatically. MicroProfile has a caching spec, or even a `ConcurrentHashMap` would work for a demo.

**Streaming responses.** Gemini supports server-sent events for streaming token output. For longer texts, this would make the UX feel much faster — the translation appears word-by-word rather than after a multi-second wait.

---

## Final Thoughts

This project reinforced something I believe about software: **clean API boundaries are worth the upfront investment**. Because the REST contract was well-defined from the start — consistent JSON, proper HTTP status codes, a health endpoint — each client was straightforward to write. The Chrome extension, PHP, Python, and React Native clients share almost no code, but they all interact with the same backend the same way.

The prompt engineering work was the most interesting part technically. Working with LLMs is genuinely different from traditional programming — you are writing constraints and examples in natural language and observing emergent behavior, rather than specifying logic precisely. Getting Darija right took more iteration than any other part of the project.

The full source code is on GitHub: **github.com/IlyassBartal/llm-darija-translator**

---

*Stack: Java 21 · Jakarta EE 10 · Payara Micro 6 · Google Gemini 1.5 Flash · Chrome MV3 · Streamlit · PHP · React Native Expo*
