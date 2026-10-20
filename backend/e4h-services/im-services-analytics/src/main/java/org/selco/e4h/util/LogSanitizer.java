package org.selco.e4h.util;

/**
 * Strips line breaks and other control characters from values that originate outside this service
 * before they reach a log statement.
 *
 * <p>Without this, a value such as a {@code tenantId} taken from a request parameter can contain a
 * CRLF and forge an entire extra log line, which at best corrupts log parsing and at worst hides a
 * real event behind a fabricated one.
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
}
