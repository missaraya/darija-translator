package com.translator.service;

import com.translator.config.AppConfig;
import com.translator.dto.TranslationRequest;
import com.translator.dto.TranslationResponse;
import com.translator.exception.ValidationException;
import com.translator.llm.GeminiTranslationClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.logging.Logger;

/**
 * Business logic layer for translation.
 *
 * Responsibilities:
 *  - Validate incoming requests.
 *  - Apply default values (e.g. default target language = Moroccan Darija).
 *  - Delegate to the LLM client (GeminiTranslationClient).
 *  - Return a complete TranslationResponse.
 *
 * This service is intentionally kept provider-agnostic — it could swap
 * GeminiTranslationClient for another LLM client without changing the resource layer.
 */
@ApplicationScoped
public class TranslationService {

    private static final Logger LOGGER = Logger.getLogger(TranslationService.class.getName());

    private static final String DEFAULT_SOURCE_LANGUAGE = "English";
    private static final String DEFAULT_TARGET_LANGUAGE = "Moroccan Darija";
    private static final String PROVIDER_NAME           = "Gemini";

    @Inject
    private AppConfig config;

    @Inject
    private GeminiTranslationClient geminiClient;

    /**
     * Validates the request and performs the translation.
     *
     * @param request Validated TranslationRequest from the resource layer.
     * @return TranslationResponse with the translated text.
     */
    public TranslationResponse translate(TranslationRequest request) {
        validate(request);

        String sourceLanguage = resolveSourceLanguage(request.getSourceLanguage());
        String targetLanguage = resolveTargetLanguage(request.getTargetLanguage());

        LOGGER.info(String.format("Translating [%d chars] from %s to %s",
                request.getText().length(), sourceLanguage, targetLanguage));

        String translatedText = geminiClient.translate(
                request.getText().trim(),
                sourceLanguage,
                targetLanguage
        );

        return new TranslationResponse(
                request.getText().trim(),
                translatedText,
                sourceLanguage,
                targetLanguage,
                PROVIDER_NAME,
                "success"
        );
    }

    private void validate(TranslationRequest request) {
        if (request == null) {
            throw new ValidationException("Request body is required.");
        }
        if (request.getText() == null || request.getText().isBlank()) {
            throw new ValidationException("The 'text' field must not be empty.");
        }
        if (request.getText().trim().length() > config.getMaxTextLength()) {
            throw new ValidationException(
                    "Text exceeds maximum allowed length of " + config.getMaxTextLength() + " characters.");
        }
    }

    private String resolveSourceLanguage(String sourceLanguage) {
        return (sourceLanguage == null || sourceLanguage.isBlank())
                ? DEFAULT_SOURCE_LANGUAGE
                : sourceLanguage.trim();
    }

    private String resolveTargetLanguage(String targetLanguage) {
        return (targetLanguage == null || targetLanguage.isBlank())
                ? DEFAULT_TARGET_LANGUAGE
                : targetLanguage.trim();
    }
}
