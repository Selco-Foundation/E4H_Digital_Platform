package org.egov.amc.web.models;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class LocalizationResponse {
    private List<Message> messages;

    @Data
    @NoArgsConstructor
    public static class Message {
        private String code;
        private String message;
        private String module;
        private String locale;
    }
}
