package com.translator.service;

/**
 * Result of translating an uploaded spreadsheet: the file bytes ready for
 * download, the suggested filename, and the MIME type to serve it with.
 */
public record TranslatedSpreadsheet(byte[] content, String filename, String contentType) {
}
