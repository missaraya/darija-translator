# Test Checklist — LLM Darija Translator

Use this checklist before every demo or submission to verify everything works end-to-end.

---

## 1. Backend

```
[ ] mvn package completes without errors (cd backend && mvn package)
[ ] Server starts: mvn package payara-micro:start — shows "ready in ...ms"
[ ] GET /api/translator/health returns 200 {"status":"UP"}
[ ] GET /api/translator/info returns 200 with provider and model fields
[ ] POST /api/translator/translate with valid auth + text returns 200 with translatedText
[ ] POST /api/translator/translate with no auth header returns 401
[ ] POST /api/translator/translate with wrong password returns 401
[ ] POST /api/translator/translate with empty text returns 400
[ ] POST /api/translator/translate with missing text field returns 400
[ ] CORS headers present: Access-Control-Allow-Origin: * in all responses
[ ] Error responses always return JSON (not HTML)
```

---

## 2. Postman

```
[ ] Collection imported: "Darija Translator Service"
[ ] Environment selected: "Darija Translator — Local"
[ ] Request 1 (Health) returns 200
[ ] Request 2 (Info) returns 200
[ ] Request 3 (Translate Success) returns 200 with translatedText field
[ ] Request 4 (Longer Text) returns 200
[ ] Request 5 (French to Darija) returns 200
[ ] Request 6 (No Auth) returns 401
[ ] Request 7 (Wrong Password) returns 401
[ ] Request 8 (Empty Text) returns 400
[ ] Request 9 (Null Text) returns 400
```

---

## 3. Chrome Extension

```
[ ] Icons generated: icons/icon16.png, icon48.png, icon128.png
[ ] Extension loaded at chrome://extensions in Developer Mode
[ ] No errors shown on chrome://extensions page
[ ] Toolbar icon visible in Chrome
[ ] Clicking toolbar icon opens side panel
[ ] Right-clicking selected text shows "Translate to Darija" menu item
[ ] Clicking "Translate to Darija" opens side panel with text pre-filled
[ ] Translate button calls backend and shows result
[ ] Wrong credentials show an error message (not a crash)
[ ] 📋 Copy button copies text to clipboard
[ ] 🔊 Read button triggers speech synthesis
[ ] Settings panel saves and loads from chrome.storage.local
[ ] Ctrl+Enter shortcut triggers translation
```

---

## 4. PHP Client

```
[ ] Server starts: php -S localhost:9081 (in php-client/)
[ ] http://localhost:9081 loads the UI correctly
[ ] Backend status badge shows "● Online" (green)
[ ] Form submission with text returns translated result
[ ] French to Darija translation works
[ ] Empty text submission shows validation message (no PHP error)
[ ] Character counter updates as you type
```

---

## 5. Python Client (Streamlit)

```
[ ] pip install -r requirements.txt completes
[ ] streamlit run app.py starts on port 8501
[ ] http://localhost:8501 loads correctly
[ ] "Backend is online" banner shows green ✅
[ ] Language selectors work
[ ] Translate button calls backend and shows result in green box
[ ] Empty text submission shows warning (not a Python exception)
[ ] Sidebar shows correct backend URL
[ ] Sidebar "Check Health" button works
```

---

## 6. React Native (Expo)

```
[ ] npm install completes without errors (no navigation packages needed — single-screen app)
[ ] npm start launches Expo dev server (QR code shown)
[ ] App loads in Expo Go or emulator (adjust BACKEND_URL in src/config/config.js if needed)
[ ] Status dot is green (backend online) or yellow (checking) or red (offline)
[ ] Language chip selectors respond to taps (source and target rows)
[ ] Text input accepts multi-line text
[ ] Translate button calls backend and shows result in green card
[ ] Loading spinner shows while request is in progress
[ ] Error message shown when backend is offline (with URL hint)
[ ] Copy button triggers Alert "Copied!" confirmation (expo-clipboard)
[ ] Character counter updates as text is typed
[ ] Clear button resets input, result, and error
```

---

## 7. Architecture Diagrams

```
[ ] class-diagram.puml renders without syntax errors in PlantUML
[ ] deployment-diagram.puml renders correctly
[ ] sequence-diagram.puml renders correctly showing all alt blocks
[ ] PNG exports available in docs/architecture/images/ (for presentation)
```

---

## 8. GitHub Readiness

```
[ ] .gitignore present and covers: target/, node_modules/, .env, __pycache__/
[ ] .env.example present with all required variables documented
[ ] No real API keys or passwords committed in any file
[ ] README.md present at root with complete setup instructions
[ ] Each subproject has its own README.md
[ ] Postman collection and environment files committed
[ ] All source files committed
[ ] No TODO markers for core features
```

---

## Summary Score

Count your checked boxes:
- **56–60 ✓** — Ready to demo and submit
- **45–55 ✓** — Minor issues, fix before presentation
- **< 45 ✓** — Needs more work
