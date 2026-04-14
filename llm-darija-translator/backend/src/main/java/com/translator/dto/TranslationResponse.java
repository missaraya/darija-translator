package com.translator.dto;

/**
 * Response payload returned by the translate endpoint.
 *
 * Example JSON:
 * {
 *   "originalText": "How are you today?",
 *   "translatedText": "Kidayr lyoum?",
 *   "sourceLanguage": "English",
 *   "targetLanguage": "Moroccan Darija",
 *   "provider": "Gemini",
 *   "status": "success"
 * }
 */
public class TranslationResponse {

    private String originalText;
    private String translatedText;
    private String sourceLanguage;
    private String targetLanguage;
    private String provider;
    private String status;

    public TranslationResponse() {}

    public TranslationResponse(String originalText, String translatedText,
                               String sourceLanguage, String targetLanguage,
                               String provider, String status) {
        this.originalText   = originalText;
        this.translatedText = translatedText;
        this.sourceLanguage = sourceLanguage;
        this.targetLanguage = targetLanguage;
        this.provider       = provider;
        this.status         = status;
    }

    public String getOriginalText()   { return originalText; }
    public String getTranslatedText() { return translatedText; }
    public String getSourceLanguage() { return sourceLanguage; }
    public String getTargetLanguage() { return targetLanguage; }
    public String getProvider()       { return provider; }
    public String getStatus()         { return status; }

    public void setOriginalText(String originalText)     { this.originalText = originalText; }
    public void setTranslatedText(String translatedText) { this.translatedText = translatedText; }
    public void setSourceLanguage(String sourceLanguage) { this.sourceLanguage = sourceLanguage; }
    public void setTargetLanguage(String targetLanguage) { this.targetLanguage = targetLanguage; }
    public void setProvider(String provider)             { this.provider = provider; }
    public void setStatus(String status)                 { this.status = status; }
}
