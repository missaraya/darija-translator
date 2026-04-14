<?php
/**
 * config.php — Configuration for the PHP Darija Translator client.
 *
 * To override for your environment, you can set these as environment variables
 * or edit this file directly (for local demo only — never commit real credentials).
 */

define('BACKEND_URL',  getenv('BACKEND_URL')  ?: 'http://localhost:8080');
define('AUTH_USERNAME', getenv('AUTH_USERNAME') ?: 'student');
define('AUTH_PASSWORD', getenv('AUTH_PASSWORD') ?: 'student123');

define('API_TRANSLATE', BACKEND_URL . '/api/translator/translate');
define('API_HEALTH',    BACKEND_URL . '/api/translator/health');
define('API_INFO',      BACKEND_URL . '/api/translator/info');

define('MAX_TEXT_LENGTH', 5000);
define('CURL_TIMEOUT',    30);
