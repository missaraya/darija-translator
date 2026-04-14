# Backend — Darija Translator REST Service

Java 21 · Jakarta EE 10 · Payara Micro 6 · MicroProfile Config · Google Gemini API

---

## Architecture

```
resource/TranslatorResource   ← JAX-RS REST endpoints
service/TranslationService    ← Business logic, validation
llm/GeminiTranslationClient   ← Gemini REST API integration
config/AppConfig              ← MicroProfile Config bean
security/BasicAuthFilter      ← HTTP Basic Auth filter
filter/CorsFilter             ← CORS headers for all clients
exception/                    ← Custom exceptions + mappers (clean JSON errors)
dto/                          ← Request / Response / Error data objects
```

---

## Prerequisites

| Tool    | Version      |
|---------|-------------|
| Java    | 21+          |
| Maven   | 3.9+         |
| Network | internet (to download Payara Micro on first run) |

---

## Environment Variables

| Variable         | Required | Default          | Description              |
|------------------|----------|------------------|--------------------------|
| `GEMINI_API_KEY` | **YES**  | —                | Google Gemini API key    |
| `AUTH_USERNAME`  | no       | `student`        | Basic Auth username      |
| `AUTH_PASSWORD`  | no       | `student123`     | Basic Auth password      |


---

## Running Locally

### 1. Set your API key

**Linux / macOS:**
```bash
export GEMINI_API_KEY=your-actual-api-key-here
```

**Windows CMD:**
```cmd
set GEMINI_API_KEY=your-actual-api-key-here
```

**Windows PowerShell:**
```powershell
$env:GEMINI_API_KEY = "AIzaSyBcL_Q5LiYeJKsUzto6v4vx_tJUVENQZOw"
```

### 2. Build and start

```bash
cd backend
mvn package payara-micro:start
```

Payara Micro downloads on first run (~60 MB, one time only) then starts.

> **Version note:** If Maven cannot resolve the plugin or Payara Micro version, update
> `<payara.version>` in `pom.xml` to the latest release listed at:
> https://mvnrepository.com/artifact/fish.payara.extras/payara-micro

### Alternative — run with the Payara Micro JAR directly

If the Maven plugin has issues, build the WAR then launch Payara Micro manually:

```bash
# Build the WAR
mvn package

# Download Payara Micro (one time) — replace version to match pom.xml <payara.version>
mvn dependency:get -Dartifact=fish.payara.extras:payara-micro:6.2024.6:jar

# Run it
java -jar ~/.m2/repository/fish/payara/extras/payara-micro/6.2024.6/payara-micro-6.2024.6.jar \
  --deploy target/darija-translator.war \
  --port 9080 \
  --contextroot /
```

**Windows (CMD):**
```cmd
java -jar %USERPROFILE%\.m2\repository\fish\payara\extras\payara-micro\6.2024.6\payara-micro-6.2024.6.jar ^
  --deploy target\darija-translator.war ^
  --port 9080 ^
  --contextroot /
```

The server starts on **http://localhost:8080**

### 3. Verify it's running

```bash
curl http://localhost:8080/api/translator/health
```

Expected response:
```json
{"status":"UP","service":"Darija Translator","version":"1.0.0"}
```

---

## Endpoints

### GET /api/translator/health
Public. No authentication required.
```json
{"status": "UP", "service": "Darija Translator", "version": "1.0.0"}
```

### GET /api/translator/info
Public. Returns service metadata.

### POST /api/translator/translate
**Protected — requires Basic Auth.**

Request body:
```json
{
  "text": "How are you today?",
  "sourceLanguage": "English",
  "targetLanguage": "Moroccan Darija"
}
```

Response:
```json
{
  "originalText": "How are you today?",
  "translatedText": "Kidayr lyoum?",
  "sourceLanguage": "English",
  "targetLanguage": "Moroccan Darija",
  "provider": "Gemini",
  "status": "success"
}
```

---

## Security

The `BasicAuthFilter` intercepts all requests **before** they reach any resource method (`@PreMatching`).

- `OPTIONS` requests (CORS preflight) are always passed through — browsers don't send credentials on preflight.
- `/health` and `/info` are **public** — no credentials needed.
- `/translate` requires **HTTP Basic Auth**.
- Default credentials: `student` / `student123`
- Override via environment variables `AUTH_USERNAME` and `AUTH_PASSWORD`.
- Invalid credentials → `401 Unauthorized` with `WWW-Authenticate: Basic realm="DarijaTranslator"`.

---

## How MicroProfile Config works

Properties in `src/main/resources/META-INF/microprofile-config.properties` have the lowest priority.
Environment variables override them automatically via MicroProfile Config's ordinal system:

| Priority | Source                   |
|----------|--------------------------|
| 400      | System properties        |
| 300      | Environment variables    |
| 100      | microprofile-config.properties |

MicroProfile maps environment variable names to config property names:
- `GEMINI_API_KEY` → `gemini.api.key`
- `AUTH_USERNAME` → `auth.username`
- `AUTH_PASSWORD` → `auth.password`

---

## Project Structure

```
backend/
├── pom.xml
└── src/
    └── main/
        ├── java/com/translator/
        │   ├── TranslatorApplication.java
        │   ├── config/AppConfig.java
        │   ├── dto/
        │   │   ├── TranslationRequest.java
        │   │   ├── TranslationResponse.java
        │   │   └── ErrorResponse.java
        │   ├── exception/
        │   │   ├── ValidationException.java
        │   │   ├── TranslationException.java
        │   │   ├── ValidationExceptionMapper.java
        │   │   ├── TranslationExceptionMapper.java
        │   │   └── GeneralExceptionMapper.java
        │   ├── filter/CorsFilter.java
        │   ├── llm/GeminiTranslationClient.java
        │   ├── resource/TranslatorResource.java
        │   ├── security/BasicAuthFilter.java
        │   └── service/TranslationService.java
        ├── resources/META-INF/microprofile-config.properties
        └── webapp/WEB-INF/beans.xml
```
