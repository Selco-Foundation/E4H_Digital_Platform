package org.selco.e4h.bulkindexer.util;

/**
 * Strips line breaks and other control characters from values that originate outside this service
 * before they reach a log statement.
 *
 * <p>Everything this service logs — index names, batch ids, document ids, ES error text — arrives
 * in a Kafka message and is therefore externally controlled. Without sanitizing, a CRLF in any of
 * them forges an extra log line, which corrupts log parsing and can hide a real failure behind a
 * fabricated entry.
 */
public final class LogSanitizer {

    private static final int MAX_LENGTH = 256;

    private LogSanitizer() {
    }

    public static String sanitize(Object value) {
        if (value == null) {
            return "null";
        }
        String cleaned = String.valueOf(value)
                .replaceAll("[\r\n]", "_")
                .replaceAll("\\p{Cntrl}", "_");
        return cleaned.length() <= MAX_LENGTH
                ? cleaned
                : cleaned.substring(0, MAX_LENGTH) + "...";
    }

    /** Sanitizes and truncates to {@code limit}, for large bodies such as a raw Kafka payload. */
    public static String sanitize(String value, int limit) {
        if (value == null) {
            return "null";
        }
        String cleaned = value.replaceAll("[\r\n]", "_").replaceAll("\\p{Cntrl}", "_");
        return cleaned.length() <= limit ? cleaned : cleaned.substring(0, limit) + "...";
    }
}
