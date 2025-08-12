package org.egov.im.web.models;

import org.egov.tracer.model.CustomException;

public enum Priority {
    HIGH,
    MEDIUM,
    LOW;

    public static Priority fromString(String value) {
        try {
            return Priority.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new CustomException("INVALID_PRIORITY", "Priority value is invalid: " + value);
        }
    }

    public String toFormattedString() {
        String lower = this.name().toLowerCase();
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

}
