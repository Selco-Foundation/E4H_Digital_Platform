package org.egov.project.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.egov.common.models.project.ApiOperation;
import org.egov.common.models.project.ProjectRequest;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectCreateResult {

    @JsonProperty("projectRequest")
    private ProjectRequest projectRequest;
    @JsonProperty("isDuplicate")
    private @Valid Boolean isDuplicate;
}
