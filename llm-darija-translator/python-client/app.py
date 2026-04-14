"""
app.py — Darija Translator Python Client (Streamlit)

Runs a clean web UI that calls the Java REST backend.

Run:
    streamlit run app.py
"""

import os
import requests
import streamlit as st
from requests.auth import HTTPBasicAuth
from requests.exceptions import ConnectionError, Timeout, RequestException

# ── Page configuration ──────────────────────────────────────────────────────
st.set_page_config(
    page_title="Darija Translator",
    page_icon="🌙",
    layout="centered",
)

# ── Configuration ────────────────────────────────────────────────────────────
# These can be overridden by environment variables or Streamlit secrets.
BACKEND_URL    = os.getenv("BACKEND_URL",    "http://localhost:8080")
AUTH_USERNAME  = os.getenv("AUTH_USERNAME",  "student")
AUTH_PASSWORD  = os.getenv("AUTH_PASSWORD",  "student123")
MAX_TEXT_LEN   = 5000
REQUEST_TIMEOUT = 30

# ── Custom CSS ────────────────────────────────────────────────────────────────
st.markdown("""
<style>
  .main-header {
    text-align: center;
    padding: 1rem 0 0.5rem;
  }
  .translation-box {
    background: #f0fff4;
    border: 1.5px solid #9ae6b4;
    border-radius: 10px;
    padding: 1.2rem 1.4rem;
    font-size: 1.15rem;
    line-height: 1.7;
    color: #22543d;
    margin-top: 0.5rem;
    direction: auto;
    unicode-bidi: plaintext;
  }
  .meta-row {
    font-size: 0.78rem;
    color: #718096;
    margin-top: 0.4rem;
  }
  .status-ok  { color: #38a169; font-weight: bold; }
  .status-err { color: #e53e3e; font-weight: bold; }
  .provider-badge {
    background: #ebf8ff;
    color: #2b6cb0;
    border-radius: 999px;
    padding: 2px 10px;
    font-size: 0.72rem;
    font-weight: 700;
  }
</style>
""", unsafe_allow_html=True)


# ── Helpers ───────────────────────────────────────────────────────────────────

def check_backend_health() -> bool:
    """Returns True if the backend health endpoint responds with status UP."""
    try:
        r = requests.get(f"{BACKEND_URL}/api/translator/health", timeout=5)
        return r.status_code == 200 and r.json().get("status") == "UP"
    except Exception:
        return False


def call_translate(text: str, source_language: str, target_language: str) -> dict:
    """
    Calls POST /api/translator/translate with Basic Auth.

    Returns the parsed JSON response dict on success.
    Raises ValueError for client errors (4xx) and RuntimeError for server/network errors.
    """
    url     = f"{BACKEND_URL}/api/translator/translate"
    payload = {
        "text":           text,
        "sourceLanguage": source_language,
        "targetLanguage": target_language,
    }
    auth = HTTPBasicAuth(AUTH_USERNAME, AUTH_PASSWORD)

    try:
        response = requests.post(url, json=payload, auth=auth, timeout=REQUEST_TIMEOUT)
    except ConnectionError:
        raise RuntimeError(
            f"Cannot reach the backend at **{BACKEND_URL}**. "
            "Make sure Payara Micro is running: `mvn package payara-micro:start`"
        )
    except Timeout:
        raise RuntimeError("Request timed out. The translation service may be overloaded.")
    except RequestException as e:
        raise RuntimeError(f"Network error: {e}")

    data = response.json()

    if response.status_code == 401:
        raise ValueError("Authentication failed. Check AUTH_USERNAME and AUTH_PASSWORD.")
    if response.status_code == 400:
        raise ValueError(data.get("message", "Validation error"))
    if response.status_code != 200:
        msg = data.get("message", f"Server error (HTTP {response.status_code})")
        raise RuntimeError(msg)

    return data


# ── UI Layout ─────────────────────────────────────────────────────────────────

# Title
st.markdown('<div class="main-header"><h1>🌙 Darija Translator</h1>'
            '<p style="color:#718096;font-size:0.9rem">Python client — Powered by Google Gemini AI</p></div>',
            unsafe_allow_html=True)

# Backend health banner
with st.spinner("Checking backend…"):
    backend_up = check_backend_health()

if backend_up:
    st.success(f"Backend is online — {BACKEND_URL}", icon="✅")
else:
    st.error(
        f"Backend is **offline** at `{BACKEND_URL}`. "
        "Start the server: `cd backend && mvn package payara-micro:start`",
        icon="🔴"
    )

st.divider()

# Language selectors
col_src, col_tgt = st.columns(2)
with col_src:
    source_language = st.selectbox(
        "Source Language",
        ["English", "French", "Spanish", "German", "Italian", "Arabic"],
        index=0,
    )
with col_tgt:
    target_language = st.selectbox(
        "Target Language",
        ["Moroccan Darija", "English", "French", "Spanish", "Arabic"],
        index=0,
    )

# Text input
text_input = st.text_area(
    "Text to Translate",
    placeholder="Enter text here… e.g. How are you today?",
    height=140,
    max_chars=MAX_TEXT_LEN,
    help=f"Maximum {MAX_TEXT_LEN} characters.",
)

char_count = len(text_input)
st.caption(f"{char_count} / {MAX_TEXT_LEN} characters")

# Translate button
translate_clicked = st.button(
    "Translate →",
    type="primary",
    disabled=not backend_up,
    use_container_width=True,
)

# ── Handle translation ────────────────────────────────────────────────────────
if translate_clicked:
    if not text_input.strip():
        st.warning("Please enter some text to translate.", icon="⚠️")
    else:
        with st.spinner("Translating…"):
            try:
                result = call_translate(text_input.strip(), source_language, target_language)

                st.markdown("**Translation**")
                st.markdown(
                    f'<div class="translation-box">{result["translatedText"]}</div>',
                    unsafe_allow_html=True,
                )
                st.markdown(
                    f'<div class="meta-row">'
                    f'{source_language} → {target_language} &nbsp;|&nbsp; '
                    f'<span class="provider-badge">{result["provider"]}</span> &nbsp;|&nbsp; '
                    f'<span class="status-ok">✓ {result["status"]}</span>'
                    f'</div>',
                    unsafe_allow_html=True,
                )

            except ValueError as e:
                st.error(str(e), icon="⚠️")
            except RuntimeError as e:
                st.error(str(e), icon="🔴")

# ── Sidebar — configuration info ──────────────────────────────────────────────
with st.sidebar:
    st.header("Configuration")
    st.info(
        f"**Backend URL:** `{BACKEND_URL}`\n\n"
        f"**User:** `{AUTH_USERNAME}`\n\n"
        "Override using environment variables:\n"
        "`BACKEND_URL`, `AUTH_USERNAME`, `AUTH_PASSWORD`"
    )
    st.divider()
    if st.button("Check Health"):
        if check_backend_health():
            st.success("Backend is UP ✓")
        else:
            st.error("Backend is DOWN")
