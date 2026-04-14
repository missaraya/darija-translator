package com.translator.llm;

import com.translator.config.AppConfig;
import com.translator.exception.TranslationException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.logging.Logger;

/**
 * Client for the Google Gemini API.
 *
 * Responsible for:
 *  - Building the translation prompt with strict instructions.
 *  - Calling the Gemini REST API using Java 11+ HttpClient.
 *  - Parsing the response and extracting the translated text.
 *  - Handling API errors, quota limits, timeouts, and malformed responses.
 *
 * API reference: https://ai.google.dev/api/generate-content
 */
@ApplicationScoped
public class GeminiTranslationClient {

    private static final Logger LOGGER = Logger.getLogger(GeminiTranslationClient.class.getName());

    @Inject
    private AppConfig config;

    /**
     * Strict system-level instructions sent to the LLM before the user text.
     * These ensure the model ONLY returns the translated Darija text — no extras.
     */
    private static final String TRANSLATION_INSTRUCTIONS = """
            You are a professional translator specializing in Moroccan Arabic Dialect (Darija).

            Rules you MUST follow without exception:
            - Translate the given text from the specified source language to the specified target language.
            - Return ONLY the translated text. Nothing else.
            - Do NOT add explanations, notes, or commentary.
            - Do NOT wrap the output in quotes or asterisks.
            - Do NOT add transliteration beside the Arabic text.
            - Do NOT add a prefix like "Translation:" or "Result:" or "Here is the translation:".
            - Preserve the natural meaning in Darija as a native speaker would say it.
            - If the input is already in the target language, return it as-is without changes.
            - Keep proper names and technical/brand terms recognizable.
            - Use authentic Darija vocabulary, not Modern Standard Arabic (Fusha).
            """;

    /**
     * Main translation entry point.
     *
     * @param text           The text to translate.
     * @param sourceLanguage e.g. "English"
     * @param targetLanguage e.g. "Moroccan Darija"
     * @return The translated text.
     */
    public String translate(String text, String sourceLanguage, String targetLanguage) {
        String prompt = buildPrompt(text, sourceLanguage, targetLanguage);
        LOGGER.info("Calling Gemini API to translate " + text.length() + " characters");
        return callGemini(prompt);
    }

    private String buildPrompt(String text, String sourceLanguage, String targetLanguage) {
        return TRANSLATION_INSTRUCTIONS
                + "\n\nTranslate the following " + sourceLanguage + " text to " + targetLanguage + ":\n"
                + text;
    }

    private String callGemini(String prompt) {
        try {
            String apiKey = config.getGeminiApiKey();
            if (apiKey == null || apiKey.isBlank()
                    || apiKey.equals("not-configured")
                    || apiKey.equals("your-gemini-api-key-here")) {
                throw new TranslationException(
                        "Gemini API key is not configured. "
                        + "Set the GEMINI_API_KEY environment variable before starting the server.");
            }

            String url = config.getGeminiApiUrl() + "/"
                    + config.getGeminiModel()
                    + ":generateContent?key=" + apiKey;

            String requestBody = buildGeminiRequestJson(prompt);

            HttpClient httpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(15))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .timeout(Duration.ofSeconds(60))
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 400) {
                LOGGER.warning("Gemini API 400: " + response.body());
                throw new TranslationException("Bad request to Gemini API. Check your API key and request format.");
            }
            if (response.statusCode() == 429) {
                throw new TranslationException("Gemini API quota exceeded. Please try again later.");
            }
            if (response.statusCode() != 200) {
                LOGGER.warning("Gemini API returned HTTP " + response.statusCode() + ": " + response.body());
                throw new TranslationException("LLM provider returned an error (HTTP " + response.statusCode() + ")");
            }

            return parseTranslatedText(response.body());

        } catch (TranslationException e) {
            throw e;
        } catch (java.net.http.HttpTimeoutException e) {
            LOGGER.severe("Gemini API timed out: " + e.getMessage());
            throw new TranslationException("Translation timed out. Please try again.");
        } catch (Exception e) {
            LOGGER.severe("Unexpected error calling Gemini API: " + e.getMessage());
            throw new TranslationException("Translation service temporarily unavailable: " + e.getMessage());
        }
    }

    /**
     * Builds the Gemini API request JSON body.
     * Uses Jakarta JSON-P for safe, proper escaping.
     */
    private String buildGeminiRequestJson(String prompt) {
        JsonObject requestJson = Json.createObjectBuilder()
                .add("contents", Json.createArrayBuilder()
                        .add(Json.createObjectBuilder()
                                .add("parts", Json.createArrayBuilder()
                                        .add(Json.createObjectBuilder()
                                                .add("text", prompt)))))
                .add("generationConfig", Json.createObjectBuilder()
                        .add("temperature", 0.3)
                        .add("maxOutputTokens", 1024))
                .build();
        return requestJson.toString();
    }

    /**
     * Parses the Gemini API response and extracts the generated text.
     *
     * Expected response structure:
     * {
     *   "candidates": [{
     *     "content": {
     *       "parts": [{ "text": "<translated text>" }],
     *       "role": "model"
     *     }
     *   }]
     * }
     */
    private String parseTranslatedText(String responseBody) {
        try (JsonReader reader = Json.createReader(new StringReader(responseBody))) {
            JsonObject root = reader.readObject();
            String translatedText = root
                    .getJsonArray("candidates")
                    .getJsonObject(0)
                    .getJsonObject("content")
                    .getJsonArray("parts")
                    .getJsonObject(0)
                    .getString("text")
                    .trim();

            if (translatedText.isEmpty()) {
                throw new TranslationException("LLM returned an empty translation.");
            }
            LOGGER.info("Translation received, length: " + translatedText.length());
            return translatedText;

        } catch (TranslationException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.severe("Failed to parse Gemini response: " + e.getMessage() + " | Body: " + responseBody);
            throw new TranslationException("Failed to parse translation response from LLM provider.");
        }
    }
}
