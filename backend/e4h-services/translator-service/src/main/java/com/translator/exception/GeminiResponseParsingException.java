package com.translator.exception;

/**
 * Raised when a successful Gemini HTTP response cannot be turned into
 * translation results: missing candidates, a blocked response, an empty
 * response, or JSON that doesn't match the requested structured schema.
 */
public class GeminiResponseParsingException extends TranslationException {

    public GeminiResponseParsingException(String message) {
        super(message);
    }

    public GeminiResponseParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
