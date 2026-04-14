# 🇲🇦 English to Moroccan Darija Translator

A Java-based REST API service that translates English text into Moroccan Darija using a Large Language Model (LLM) backend.

## Overview

This project provides a translation service specifically designed for **Moroccan Darija** — the Arabic dialect spoken in Morocco. Unlike standard Arabic translation tools, this service targets the unique vocabulary, grammar, and expressions of Darija, making it more accessible and culturally relevant for Moroccan users.

The service is built as a **Java REST API** and can be consumed by multiple clients (web, mobile, desktop, etc.).

## Features

- 🌐 Translates English text to Moroccan Darija
- ⚡ RESTful API design for easy integration with multiple clients
- 🤖 LLM-powered translations for natural, context-aware output
- ☕ Built with Java for robust, scalable backend performance

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java (Jakarta EE / TomEE) |
| API | REST |
| Build Tool | Maven |
| AI Backend | LLM (via API) |

## Project Structure

```
darija-translator/
├── llm-darija-translator/   # Core translation service
├── UML/                     # Architecture diagrams
│   ├── class-diagram.png
│   ├── deployment-diagram.png
│   └── sequence-diagram.png
└── README.md
```

## Getting Started

### Prerequisites

- Java 11+
- Maven 3.x
- TomEE Plume (or compatible Jakarta EE server)

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/missaraya/darija-translator.git
   cd darija-translator
   ```

2. Build the project:
   ```bash
   mvn clean install
   ```

3. Deploy to TomEE and access the API at:
   ```
   http://localhost:8080/darija-translator/api/translate
   ```

## Author

**Aya** — [github.com/missaraya](https://github.com/missaraya)
