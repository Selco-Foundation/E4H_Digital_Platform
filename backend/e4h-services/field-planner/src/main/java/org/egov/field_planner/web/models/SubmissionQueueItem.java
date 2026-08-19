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

    @JsonProperty("planName")
    private String planName;

    @JsonProperty("facilityId")
    private String facilityId;

    @JsonProperty("facilityName")
    private String facilityName;

    @JsonProperty("facilityCategory")
    private String facilityCategory;

    @JsonProperty("facilityType")
    private String facilityType;

    @JsonProperty("state")
    private String state;

    @JsonProperty("district")
    private String district;

    @JsonProperty("block")
    private String block;

    @JsonProperty("phoneStatus")
    private String phoneStatus;

    @JsonProperty("fieldStatus")
    private String fieldStatus;

    @JsonProperty("lastActionTime")
    private Long lastActionTime;
}
