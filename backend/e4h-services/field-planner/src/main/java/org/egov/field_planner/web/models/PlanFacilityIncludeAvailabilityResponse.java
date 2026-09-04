package org.egov.field_planner.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanFacilityIncludeAvailabilityResponse {

    @JsonProperty("ResponseInfo")
    private org.egov.common.contract.response.ResponseInfo responseInfo;

    @JsonProperty("availableFacilityIds")
    private List<String> availableFacilityIds;

<<<<<<<< HEAD:backend/e4h-services/field-planner/src/main/java/org/egov/field_planner/web/models/PlanFacilityIncludeAvailabilityResponse.java
    @JsonProperty("excluded")
    private List<PlanFacilityIncludeError> excluded;
========
    @JsonProperty("count")
    private Integer count;

    @JsonProperty("total")
    private Integer total;

    @JsonProperty("pagination")
    private Pagination pagination;
>>>>>>>> 491986f1a (added backend flow for assessment module (#2868) (#2927)):backend/e4h-services/field-planner/src/main/java/org/egov/field_planner/web/models/SubmissionQueueSearchResponse.java
}
