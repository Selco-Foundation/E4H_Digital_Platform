package org.egov.im.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Published on the {@code facility-ticket-count} topic every time a ticket is raised for a facility.
 *
 * <p>The event carries the facility's absolute ticket total rather than a "+1" nudge, so consumers
 * only ever assign the value they are given. That keeps them idempotent under Kafka redelivery and
 * lets any drift self-correct on the next ticket, since the total is recounted from the incident
 * table each time.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
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
