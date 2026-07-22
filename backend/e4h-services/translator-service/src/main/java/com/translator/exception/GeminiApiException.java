package com.translator.exception;

/**
 * Raised when the HTTP call to the Gemini API fails (network error,
 * non-2xx status, or timeout).
 */
public class GeminiApiException extends TranslationException {

    public GeminiApiException(String message) {
        super(message);
    }

    public GeminiApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
