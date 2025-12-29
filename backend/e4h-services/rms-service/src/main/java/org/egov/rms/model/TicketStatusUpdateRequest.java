package org.egov.rms.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.request.RequestInfo;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketStatusUpdateRequest {

    @JsonProperty("incidentId")
    private String incidentId;

    @JsonProperty("applicationStatus")
    private String applicationStatus;

    @JsonProperty("requestInfo")
    private RequestInfo requestInfo;

    /**
     * Checks if the ticket status indicates a closed/resolved ticket
     */
    public boolean isClosedStatus() {
        if (applicationStatus == null) {
            return false;
        }
        String status = applicationStatus.toUpperCase();
        return status.equals("RESOLVED") || 
               status.equals("CLOSEDAFTERRESOLUTION") || 
               status.equals("REJECTED") || 
               status.equals("CLOSEDAFTERREJECTION");
    }
}


