package org.egov.amc.web.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload of the {@code facility-ticket-count} topic, published by im-services on every ticket create.
 *
 * <p>{@code totalTickets} is the facility's absolute total, not a delta, so applying the same event
 * twice is a no-op.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class FacilityTicketCountEvent {

    @JsonProperty("tenantId")
    private String tenantId;

    @JsonProperty("facilityId")
    private String facilityId;

    /** The ticket whose creation triggered this event - carried for traceability only. */
    @JsonProperty("incidentId")
    private String incidentId;

    /** Count of all tickets ever raised for the facility, in every status. */
    @JsonProperty("totalTickets")
    private Integer totalTickets;
}
