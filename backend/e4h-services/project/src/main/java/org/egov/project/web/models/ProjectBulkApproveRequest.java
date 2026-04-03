package org.egov.project.web.models;

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
public class ProjectBulkApproveRequest {

    @JsonProperty("RequestInfo")
    @NotNull
    private RequestInfo requestInfo;

    @JsonProperty("isAllSelected")
    @NotNull
    private Boolean isAllSelected;

    @JsonProperty("projectIDs")
    private List<String> projectIDs;

    @JsonProperty("filters")
    private ProjectBulkFilter filters;

    @JsonProperty("workflow")
    private Workflow workflow;

}
