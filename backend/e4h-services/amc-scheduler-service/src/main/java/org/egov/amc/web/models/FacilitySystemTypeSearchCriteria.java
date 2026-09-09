package org.egov.amc.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Criteria of the field-planner facility system type search. Only facilityId is sent: field-planner
 * rejects an empty list rather than scanning every installation plan facility in the tenant.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacilitySystemTypeSearchCriteria {

    @JsonProperty("facilityId")
    private List<String> facilityId;
}
