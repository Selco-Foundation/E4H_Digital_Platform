package org.egov.im.web.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum WarrantyStatus {

    WITHIN_WARRANTY("WITHIN_WARRANTY"),
    OUT_OF_WARRANTY("OUT_OF_WARRANTY");

    private final String value;

    WarrantyStatus(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
        return value;
    }

    @JsonCreator
    public static WarrantyStatus fromValue(String text) {
        if (text == null) {
            return null;
        }
        for (WarrantyStatus status : WarrantyStatus.values()) {
            if (status.value.equalsIgnoreCase(text)) {
                return status;
            }
        }
        return null;
    }
}

