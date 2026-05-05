package org.egov.rms.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketPauseExpiryResponse {

    @JsonProperty("success")
    private boolean success;

    @JsonProperty("processedCount")
    private int processedCount;

    @JsonProperty("resumedCount")
    private int resumedCount;

    @JsonProperty("skippedCount")
    private int skippedCount;

    @JsonProperty("message")
    private String message;

    @JsonProperty("error")
    private TicketPauseResponse.ErrorPayload error;

    public static TicketPauseExpiryResponse success(int processedCount, int resumedCount, int skippedCount, String message) {
        return TicketPauseExpiryResponse.builder()
                .success(true)
                .processedCount(processedCount)
                .resumedCount(resumedCount)
                .skippedCount(skippedCount)
                .message(message)
                .build();
    }

    public static TicketPauseExpiryResponse error(String code, String message) {
        return TicketPauseExpiryResponse.builder()
                .success(false)
                .processedCount(0)
                .resumedCount(0)
                .skippedCount(0)
                .message(message)
                .error(TicketPauseResponse.ErrorPayload.builder().code(code).message(message).build())
                .build();
    }
}

