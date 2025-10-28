package org.egov.activity.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FacilityBulkApproveRequest {

    @JsonProperty("RequestInfo")
    @NotNull
    private RequestInfo requestInfo;

    @JsonProperty("isAllSelected")
    @NotNull
    private Boolean isAllSelected;

    @JsonProperty("activityFacilityIds")
    private List<String> activityFacilityIds;

    @JsonProperty("filters")
    private FacilityBulkFilter filters;

    @JsonProperty("workflow")
    private Workflow workflow;

}
