package org.egov.project.web.models;

import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.egov.common.models.project.ProjectSearchRequest;

import java.util.List;

@Getter
@Setter
public class ExtendedProjectSearchRequest extends ProjectSearchRequest {
    
    @JsonProperty("workflowStatus")
    private List<String> workflowStatus;
}
