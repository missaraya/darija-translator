# PHP Client — Darija Translator

Simple PHP web application that calls the Java REST backend using cURL.

---

## Prerequisites

- PHP 7.4+ (with cURL extension enabled — enabled by default on most installations)
- The backend must be running at `http://localhost:9080`

---

## Running Locally

```bash
cd php-client
php -S localhost:9081
```

Then open your browser at: **http://localhost:9081**

---

## Configuration

Edit `config.php` or set environment variables:

| Variable         | Default                   | Description            |
|------------------|---------------------------|------------------------|
| `BACKEND_URL`    | `http://localhost:9080`   | Java backend base URL  |
| `AUTH_USERNAME`  | `student`                 | Basic Auth username    |
| `AUTH_PASSWORD`  | `student123`              | Basic Auth password    |

---

## How It Works

1. `index.php` — the main page, renders the form and handles POST submissions
2. `translator.php` — cURL functions that call the REST API with Basic Auth
3. `config.php` — configuration constants
4. `style.css` — clean, responsive styles

On form submit:
- Input is validated (empty check, length limit)
- `translator.php::translate()` sends a `POST /api/translator/translate` with Basic Auth header
- The response is decoded and displayed on the page
- Errors (network, auth, validation) are displayed clearly
