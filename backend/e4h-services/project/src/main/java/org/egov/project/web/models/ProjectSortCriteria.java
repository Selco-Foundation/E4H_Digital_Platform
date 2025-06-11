package org.egov.project.web.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.egov.common.models.core.ProjectSearchURLParams;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class ProjectSortCriteria extends ProjectSearchURLParams {

    @JsonProperty("sort_by")
    private String sortBy;
    @JsonProperty("sort_direction")
    private SortDirection sortDirection = SortDirection.DESC;

    public enum SortDirection { ASC, DESC }

}
