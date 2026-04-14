package com.translator.exception;

/**
 * Thrown when the incoming request fails validation
 * (e.g. empty text, text too long, missing required fields).
 */
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }
}
