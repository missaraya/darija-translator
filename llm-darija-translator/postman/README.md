# Postman Testing Guide

---

## Import Instructions

1. Open Postman Desktop
2. Click **Import** → drag and drop both files:
   - `TranslatorService.postman_collection.json`
   - `TranslatorService.postman_environment.json`
3. In the top-right, select environment: **"Darija Translator — Local"**

---

## Prerequisites

The Java backend must be running:
```bash
cd backend
export GEMINI_API_KEY=your-key-here
mvn package payara-micro:start
```

Wait until you see: `Payara Micro ... ready in ...ms`

---

## Test Requests (run in order)

| # | Request                          | Method | Auth     | Expected Status |
|---|----------------------------------|--------|----------|-----------------|
| 1 | Health Check                     | GET    | None     | 200 OK          |
| 2 | Service Info                     | GET    | None     | 200 OK          |
| 3 | Translate — Success              | POST   | Basic    | 200 OK          |
| 4 | Translate — Longer Text          | POST   | Basic    | 200 OK          |
| 5 | Translate — French to Darija     | POST   | Basic    | 200 OK          |
| 6 | Translate — No Auth              | POST   | None     | **401**         |
| 7 | Translate — Wrong Password       | POST   | Basic    | **401**         |
| 8 | Translate — Empty Text           | POST   | Basic    | **400**         |
| 9 | Translate — Missing Text Field   | POST   | Basic    | **400**         |

---

## Environment Variables

| Variable   | Value                   | Purpose                     |
|------------|-------------------------|-----------------------------|
| `baseUrl`  | `http://localhost:8080` | Backend base URL            |
| `username` | `student`               | Basic Auth username         |
| `password` | `student123`            | Basic Auth password         |

---

## Expected Response Examples

### 200 — Successful Translation
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

### 401 — Unauthorized
```json
{
  "error": "Unauthorized",
  "message": "Authorization header required",
  "status": 401
}
```

### 400 — Validation Error
```json
{
  "error": "Validation Error",
  "message": "The 'text' field must not be empty.",
  "status": 400
}
```
