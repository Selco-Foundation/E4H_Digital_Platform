package org.egov.im.web.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocalizationResponse {

    private List<Message> messages;

    public String getMessageByCode(String code) {
        return messages.stream()
                .filter(m -> code.equals(m.getCode()))
                .map(Message::getMessage)
                .findFirst()
                .orElse(null); // returns null if not found
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Message {
        private String code;
        private String message;
        private String module;
        private String locale;
    }
}
