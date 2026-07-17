package com.translator.exception;

/**
 * Raised when an uploaded spreadsheet can't be processed: unsupported file
 * type, an unreadable/corrupt workbook, a missing expected column, or an
 * empty word list.
 */
public class ExcelProcessingException extends TranslationException {

    public ExcelProcessingException(String message) {
        super(message);
    }

    public ExcelProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
