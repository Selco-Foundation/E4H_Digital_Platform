package org.selco.e4h.web.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Co2LocalizationResponse {

    private List<Co2LocalizationMessage> messages;

    public String getMessageByCode(String code) {
        if (messages == null || code == null) {
            return null;
        }
        return messages.stream()
                .filter(m -> code.equals(m.getCode()))
                .map(Co2LocalizationMessage::getMessage)
                .findFirst()
                .orElse(null);
    }

    public static Co2LocalizationResponse empty() {
        return Co2LocalizationResponse.builder().messages(Collections.emptyList()).build();
    }
}
