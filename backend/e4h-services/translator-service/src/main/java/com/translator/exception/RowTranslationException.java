package com.translator.exception;

/**
 * Raised when a row-object translation request can't be processed: no
 * words found in any row, or a translation/word count mismatch.
 */
public class RowTranslationException extends TranslationException {

    public RowTranslationException(String message) {
        super(message);
    }
}
