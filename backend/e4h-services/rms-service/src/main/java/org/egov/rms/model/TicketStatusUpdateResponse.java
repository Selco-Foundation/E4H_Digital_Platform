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
public class TicketStatusUpdateResponse {

    @JsonProperty("success")
    private Boolean success;

    @JsonProperty("message")
    private String message;

    @JsonProperty("ticketId")
    private String ticketId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("alertsUpdated")
    private Integer alertsUpdated;

    @JsonProperty("isClosed")
    private Boolean isClosed;

    /**
     * Creates a success response
     */
    public static TicketStatusUpdateResponse success(String ticketId, String status, 
                                                     int alertsUpdated, boolean isClosed) {
        return TicketStatusUpdateResponse.builder()
                .success(true)
                .message("Ticket status update processed successfully")
                .ticketId(ticketId)
                .status(status)
                .alertsUpdated(alertsUpdated)
                .isClosed(isClosed)
                .build();
    }

    /**
     * Creates an error response
     */
    public static TicketStatusUpdateResponse error(String message) {
        return TicketStatusUpdateResponse.builder()
                .success(false)
                .message(message)
                .alertsUpdated(0)
                .isClosed(false)
                .build();
    }
}

