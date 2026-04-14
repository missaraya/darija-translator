<?php
/**
 * translator.php — cURL wrapper for the Darija Translator REST API.
 *
 * Provides two public functions:
 *   translate(string $text, string $source, string $target): array
 *   checkHealth(): array
 */

require_once __DIR__ . '/config.php';

/**
 * Sends a translation request to the backend REST service.
 *
 * @param string $text           Text to translate.
 * @param string $sourceLanguage Default "English".
 * @param string $targetLanguage Default "Moroccan Darija".
 * @return array  ['success' => bool, 'data' => array|null, 'error' => string|null]
 */
function translate(string $text, string $sourceLanguage = 'English', string $targetLanguage = 'Moroccan Darija'): array
{
    $payload = json_encode([
        'text'           => $text,
        'sourceLanguage' => $sourceLanguage,
        'targetLanguage' => $targetLanguage,
    ]);

    $ch = curl_init(API_TRANSLATE);
    curl_setopt_array($ch, [
        CURLOPT_POST           => true,
        CURLOPT_POSTFIELDS     => $payload,
        CURLOPT_RETURNTRANSFER => true,
        CURLOPT_TIMEOUT        => CURL_TIMEOUT,
        CURLOPT_HTTPHEADER     => [
            'Content-Type: application/json',
            'Accept: application/json',
            'Authorization: Basic ' . base64_encode(AUTH_USERNAME . ':' . AUTH_PASSWORD),
        ],
    ]);

    $response   = curl_exec($ch);
    $httpCode   = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    $curlError  = curl_error($ch);
    curl_close($ch);

    if ($curlError) {
        return ['success' => false, 'data' => null,
                'error'   => 'Connection failed: ' . $curlError];
    }

    $decoded = json_decode($response, true);

    if ($httpCode === 401) {
        return ['success' => false, 'data' => null,
                'error'   => 'Authentication failed. Check username and password in config.php.'];
    }

    if ($httpCode === 400) {
        $msg = $decoded['message'] ?? 'Validation error';
        return ['success' => false, 'data' => null, 'error' => $msg];
    }

    if ($httpCode !== 200) {
        $msg = $decoded['message'] ?? "Server error (HTTP $httpCode)";
        return ['success' => false, 'data' => null, 'error' => $msg];
    }

    return ['success' => true, 'data' => $decoded, 'error' => null];
}

/**
 * Calls the health endpoint to verify the backend is reachable.
 *
 * @return array ['up' => bool, 'message' => string]
 */
function checkHealth(): array
{
    $ch = curl_init(API_HEALTH);
    curl_setopt_array($ch, [
        CURLOPT_RETURNTRANSFER => true,
        CURLOPT_TIMEOUT        => 5,
    ]);

    $response  = curl_exec($ch);
    $httpCode  = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    $curlError = curl_error($ch);
    curl_close($ch);

    if ($curlError) {
        return ['up' => false, 'message' => 'Backend unreachable: ' . $curlError];
    }

    if ($httpCode === 200) {
        $data = json_decode($response, true);
        return ['up' => true, 'message' => ($data['status'] ?? 'UP')];
    }

    return ['up' => false, 'message' => "Backend returned HTTP $httpCode"];
}
