package org.egov.project.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.models.project.ProjectSearch;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjectBulkFilter {

    @JsonProperty("status")
    private List<String> status;

    @JsonProperty("projectSearch")
    private ProjectSearch projectSearch;
}
