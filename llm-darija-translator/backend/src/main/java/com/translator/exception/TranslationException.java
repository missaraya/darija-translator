package com.translator.exception;

/**
 * Thrown when the LLM provider call fails
 * (e.g. invalid API key, quota exceeded, network timeout, bad response).
 */
public class TranslationException extends RuntimeException {

    public TranslationException(String message) {
        super(message);
    }

    public TranslationException(String message, Throwable cause) {
        super(message, cause);
    }
}
