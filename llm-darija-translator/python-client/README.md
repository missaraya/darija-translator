# Python Client — Darija Translator

Streamlit web application that calls the Java REST backend.

---

## Prerequisites

- Python 3.9+
- The backend running at `http://localhost:8080`

---

## Setup and Run

```bash
cd python-client

# Create a virtual environment (recommended)
python -m venv venv
source venv/bin/activate      # Linux / macOS
venv\Scripts\activate         # Windows

# Install dependencies
pip install -r requirements.txt

# Run the Streamlit app
streamlit run app.py
```

Streamlit will open your browser automatically at **http://localhost:8501**

---

## Configuration

Set environment variables before running, or edit the defaults at the top of `app.py`:

```bash
export BACKEND_URL=http://localhost:9080
export AUTH_USERNAME=student
export AUTH_PASSWORD=student123
streamlit run app.py
```

**Windows PowerShell:**
```powershell
$env:BACKEND_URL = "http://localhost:9080"
$env:AUTH_USERNAME = "student"
$env:AUTH_PASSWORD = "student123"
streamlit run app.py
```

---

## Features

- Language selector (source and target)
- Text area with character counter
- Backend health check on startup
- Clear error messages for auth failures, validation errors, network errors
- Translation displayed in a styled result box
- Sidebar showing current configuration
