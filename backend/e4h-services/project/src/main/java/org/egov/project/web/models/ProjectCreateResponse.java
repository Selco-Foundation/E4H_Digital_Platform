package org.egov.project.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.egov.common.models.project.ProjectResponse;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectCreateResponse {
    @JsonProperty("projectResponse")
    private ProjectResponse projectResponse;

    @JsonProperty("isDuplicate")
    private Boolean isDuplicate;
}
