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
}
