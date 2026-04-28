package org.egov.rms.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketPauseResponse {

    @JsonProperty("success")
    private boolean success;

    @JsonProperty("facilityId")
    private String facilityId;

    @JsonProperty("isPaused")
    private Boolean isPaused;

    @JsonProperty("pausedUntil")
    private Instant pausedUntil;

    @JsonProperty("daysLeft")
    private Long daysLeft;

    @JsonProperty("reason")
    private String reason;

    @JsonProperty("message")
    private String message;

    @JsonProperty("error")
    private ErrorPayload error;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorPayload {
        @JsonProperty("code")
        private String code;

        @JsonProperty("message")
        private String message;
    }

    public static TicketPauseResponse success(String facilityId, boolean isPaused, Instant pausedUntil,
                                              Long daysLeft, String reason, String message) {
        return TicketPauseResponse.builder()
                .success(true)
                .facilityId(facilityId)
                .isPaused(isPaused)
                .pausedUntil(pausedUntil)
                .daysLeft(daysLeft)
                .reason(reason)
                .message(message)
                .build();
    }

    public static TicketPauseResponse error(String code, String message) {
        return TicketPauseResponse.builder()
                .success(false)
                .error(ErrorPayload.builder().code(code).message(message).build())
                .message(message)
                .build();
    }
}

