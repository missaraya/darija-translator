<?php
/**
 * index.php — Darija Translator PHP Client
 *
 * A clean single-page web application that calls the Java REST backend.
 * Run with: php -S localhost:9081
 */

require_once __DIR__ . '/translator.php';

$result         = null;
$errorMessage   = null;
$inputText      = '';
$sourceLanguage = 'English';
$targetLanguage = 'Moroccan Darija';

// Check backend health on page load
$health = checkHealth();

// Handle form submission
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $inputText      = trim($_POST['text'] ?? '');
    $sourceLanguage = trim($_POST['sourceLanguage'] ?? 'English');
    $targetLanguage = trim($_POST['targetLanguage'] ?? 'Moroccan Darija');

    if (empty($inputText)) {
        $errorMessage = 'Please enter some text to translate.';
    } elseif (mb_strlen($inputText) > MAX_TEXT_LENGTH) {
        $errorMessage = 'Text is too long. Maximum ' . MAX_TEXT_LENGTH . ' characters.';
    } else {
        $response = translate($inputText, $sourceLanguage, $targetLanguage);
        if ($response['success']) {
            $result = $response['data'];
        } else {
            $errorMessage = $response['error'];
        }
    }
}

// HTML escaping helper
function h(string $s): string { return htmlspecialchars($s, ENT_QUOTES | ENT_HTML5, 'UTF-8'); }
?>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Darija Translator</title>
  <link rel="stylesheet" href="style.css" />
</head>
<body>

  <div class="container">

    <!-- Header -->
    <header class="header">
      <div class="header-inner">
        <span class="logo">🌙</span>
        <div>
          <h1>Darija Translator</h1>
          <p class="subtitle">PHP Client — Powered by Google Gemini AI</p>
        </div>
        <!-- Backend status indicator -->
        <div class="status-badge <?= $health['up'] ? 'status-up' : 'status-down' ?>">
          <?= $health['up'] ? '● Online' : '● Offline' ?>
        </div>
      </div>
    </header>

    <!-- Main card -->
    <main class="card">

      <!-- Error message -->
      <?php if ($errorMessage): ?>
        <div class="alert alert-error">⚠ <?= h($errorMessage) ?></div>
      <?php endif; ?>

      <!-- Backend offline warning -->
      <?php if (!$health['up']): ?>
        <div class="alert alert-warning">
          ⚠ The backend is not reachable at <strong><?= h(BACKEND_URL) ?></strong>.
          Make sure Payara Micro is running (<code>mvn package payara-micro:start</code> in the backend folder).
        </div>
      <?php endif; ?>

      <!-- Translation form -->
      <form method="POST" action="">

        <!-- Source language selector -->
        <div class="form-row">
          <div class="form-group half">
            <label for="sourceLanguage">Source Language</label>
            <select id="sourceLanguage" name="sourceLanguage" class="form-select">
              <?php
              $languages = ['English', 'French', 'Spanish', 'Arabic', 'German', 'Italian'];
              foreach ($languages as $lang) {
                  $sel = ($lang === $sourceLanguage) ? 'selected' : '';
                  echo "<option value=\"" . h($lang) . "\" $sel>" . h($lang) . "</option>";
              }
              ?>
            </select>
          </div>
          <div class="form-group half">
            <label for="targetLanguage">Target Language</label>
            <select id="targetLanguage" name="targetLanguage" class="form-select">
              <?php
              $targets = ['Moroccan Darija', 'French', 'English', 'Spanish', 'Arabic'];
              foreach ($targets as $lang) {
                  $sel = ($lang === $targetLanguage) ? 'selected' : '';
                  echo "<option value=\"" . h($lang) . "\" $sel>" . h($lang) . "</option>";
              }
              ?>
            </select>
          </div>
        </div>

        <!-- Text input -->
        <div class="form-group">
          <label for="text">Text to Translate</label>
          <textarea id="text" name="text" class="form-textarea"
                    placeholder="Enter text here… e.g. How are you today?"
                    rows="5"><?= h($inputText) ?></textarea>
          <div class="char-count" id="charCount">0 / <?= MAX_TEXT_LENGTH ?></div>
        </div>

        <!-- Submit -->
        <button type="submit" class="btn-translate" <?= !$health['up'] ? 'disabled' : '' ?>>
          Translate →
        </button>

      </form>

      <!-- Result section -->
      <?php if ($result): ?>
        <div class="result-card">
          <div class="result-header">
            <span class="result-label">Translation (<?= h($result['targetLanguage']) ?>)</span>
            <span class="provider-badge"><?= h($result['provider']) ?></span>
          </div>
          <p class="result-text" dir="auto"><?= h($result['translatedText']) ?></p>
          <div class="result-meta">
            <span>Original: <em><?= h(mb_substr($result['originalText'], 0, 60)) ?><?= mb_strlen($result['originalText']) > 60 ? '…' : '' ?></em></span>
            <span class="status-success">✓ <?= h($result['status']) ?></span>
          </div>
        </div>
      <?php endif; ?>

    </main>

    <!-- Footer -->
    <footer class="footer">
      Backend: <code><?= h(BACKEND_URL) ?></code> &nbsp;|&nbsp;
      User: <code><?= h(AUTH_USERNAME) ?></code> &nbsp;|&nbsp;
      <a href="<?= h(BACKEND_URL) ?>/api/translator/health" target="_blank">Health Check</a>
    </footer>

  </div>

  <script>
    // Live character counter
    const textarea  = document.getElementById('text');
    const counter   = document.getElementById('charCount');
    const maxLength = <?= MAX_TEXT_LENGTH ?>;

    function updateCounter() {
      const len = textarea.value.length;
      counter.textContent = len + ' / ' + maxLength;
      counter.style.color = len > maxLength * 0.9 ? '#e53e3e' : '#718096';
    }

    textarea.addEventListener('input', updateCounter);
    updateCounter();
  </script>

</body>
</html>
