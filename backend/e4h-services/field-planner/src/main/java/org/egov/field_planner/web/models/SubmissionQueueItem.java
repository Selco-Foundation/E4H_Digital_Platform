package org.egov.field_planner.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionQueueItem {

    @JsonProperty("planFacilityId")
    private String planFacilityId;

    @JsonProperty("planId")
    private String planId;

    @JsonProperty("facilityId")
    private String facilityId;

    @JsonProperty("facilityName")
    private String facilityName;

    @JsonProperty("facilityCategory")
    private String facilityCategory;

    @JsonProperty("phoneStatus")
    private String phoneStatus;

    @JsonProperty("fieldStatus")
    private String fieldStatus;
}
