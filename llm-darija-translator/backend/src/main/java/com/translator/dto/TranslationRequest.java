package com.translator.dto;

/**
 * Incoming request payload for the translate endpoint.
 *
 * Example JSON:
 * {
 *   "text": "How are you today?",
 *   "sourceLanguage": "English",
 *   "targetLanguage": "Moroccan Darija"
 * }
 */
public class TranslationRequest {

    private String text;
    private String sourceLanguage;
    private String targetLanguage;

    // No-arg constructor required for JSON-B deserialization
    public TranslationRequest() {}

    public TranslationRequest(String text, String sourceLanguage, String targetLanguage) {
        this.text = text;
        this.sourceLanguage = sourceLanguage;
        this.targetLanguage = targetLanguage;
    }

    public String getText()           { return text; }
    public String getSourceLanguage() { return sourceLanguage; }
    public String getTargetLanguage() { return targetLanguage; }

    public void setText(String text)                     { this.text = text; }
    public void setSourceLanguage(String sourceLanguage) { this.sourceLanguage = sourceLanguage; }
    public void setTargetLanguage(String targetLanguage) { this.targetLanguage = targetLanguage; }
}
